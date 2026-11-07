import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderStatus;
import com.tradingglobal3d.core.trading.OrderType;
import com.tradingglobal3d.core.research.TerminalTier;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;

public class ColdResumeProbe {
    private static final Instant START = Instant.parse("2026-10-19T02:00:00Z");

    public static void main(String[] args) throws Exception {
        Path dir = Path.of(args[0]);
        int days = Integer.parseInt(args[1]);
        String mode = args[2];
        SaveManager storage = SaveManager.forDirectory(dir.toFile());
        TradingFacade game = new TradingFacade(storage);
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Path.of(args[3]))));
        game.setRealTimeClock(Clock.fixed(START.plus(Duration.ofDays(days)), ZoneOffset.UTC));
        game.setDynamicDailyNewsEnabled(true);
        long started = System.nanoTime();
        if (mode.equals("seed") || mode.equals("seed-traded") || mode.equals("seed-combined")) {
            require(Files.notExists(dir), "Seed path already exists");
            require(game.startOrResume(), "Seed creation failed");
            if (!mode.equals("seed")) {
                var account = game.getPersonalAccount();
                var maker = game.getAccount("ACC-NPC-MM-01");
                Order ask = Order.createLimit("SU-QA-ASK", "IDX:BBCA", maker.getId(), OrderSide.SELL,
                        Money.of(10250, Currency.IDR), 100, game.getGameTimestamp());
                require(game.submitOrder(ask).isSuccess, "Synthetic ask rejected");
                Order filled = Order.createLimit("SU-QA-FILL", "IDX:BBCA", account.getId(), OrderSide.BUY,
                        Money.of(10250, Currency.IDR), 100, game.getGameTimestamp());
                require(game.submitOrder(filled).isSuccess && filled.getStatus() == OrderStatus.FILLED,
                        "Synthetic player trade did not fill");
                Order order = Order.createLimit("SU-QA-OPEN", "IDX:BBCA", account.getId(), OrderSide.BUY,
                        Money.of(100, Currency.IDR), 100, game.getGameTimestamp());
                require(game.submitOrder(order).isSuccess && order.getStatus() == OrderStatus.OPEN,
                        "Synthetic limit order did not enter the book");
                require(account.getReservedCash(Currency.IDR).getAmount().signum() > 0, "Reservation missing");
                game.subscribeMutualFund("SU-QA-FUND", "FUND-MM-01", Money.of(100_000, Currency.IDR));
                if (mode.equals("seed-combined")) {
                    game.getCareerManager().restoreState(game.getCareerManager().exportState()
                            .put("rank", "CHIEF_INVESTMENT_OFFICER"));
                    game.deposit("SU-QA-IDR", account.getId(), Money.of(10_000_000_000L, Currency.IDR), "Synthetic QA capital");
                    game.deposit("SU-QA-USD", account.getId(), Money.of(20_000_000, Currency.USD), "Synthetic QA subscription budget");
                    game.acceptClientMandate("MANDATE-01");
                    game.foundCompany("SU-QA-COMPANY", "Synthetic QA company", "Jasa", Money.of(25_000_000, Currency.IDR));
                    game.takeMarginLoan(Money.of(1_000_000, Currency.IDR));
                    game.openShortPosition("BMRI", 100);
                    require(game.subscribeTerminal(TerminalTier.PRO), "Pro subscription rejected");
                    game.orderIpo("IPO-CLOUD", 2, "SU-QA-IPO-ORDER");
                    Order trigger = new Order("SU-QA-TRIGGER", "SU-QA-TRIGGER", account.getId(), "IDX:BBCA",
                            OrderSide.SELL, OrderType.TAKE_PROFIT, null, Money.of(1_000_000, Currency.IDR),
                            100, game.getGameTimestamp(), null);
                    require(game.submitOrder(trigger).isSuccess && trigger.getStatus() == OrderStatus.PENDING_TRIGGER,
                            "QA trigger missing");
                    game.save();
                }
            }
            System.out.println(game.getGameTimestamp());
            return;
        }
        require(storage.getSaveFile().isFile(), "Fixture missing");
        long expected = Long.parseLong(args[4]) + Duration.ofDays(days).toMillis() * 48;
        if (mode.equals("bridge") || mode.equals("interrupt") || mode.equals("restart")) {
            TradingWebBridge bridge = new TradingWebBridge(game);
            try {
                if (mode.equals("interrupt") || mode.equals("restart")) {
                    var attempts = new AtomicInteger();
                    storage.setWriteAuthority(() -> {
                        if (attempts.incrementAndGet() == 2) Runtime.getRuntime().halt(71);
                        return true;
                    });
                }
                require(new JSONObject(bridge.beginClockSync(true)).getString("status").equals("PENDING"),
                        "Missing pending acknowledgement");
                String raw;
                do {
                    require(System.nanoTime() - started < Duration.ofSeconds(120).toNanos(), "Worker timeout");
                    Thread.sleep(20);
                    raw = bridge.getClockSyncStatus();
                } while (raw.contains("\"status\":\"PENDING\""));
                JSONObject result = new JSONObject(raw);
                require(!mode.equals("interrupt"), "Expected process termination before second commit");
                require(result.getString("status").equals("COMPLETED"), raw);
                require(result.getJSONObject("snapshot").getJSONObject("clock").getLong("timestamp") == expected,
                        "Wrong snapshot timestamp");
            } finally {
                bridge.destroy();
            }
        } else if (mode.equals("reload")) {
            require(game.load(), "Committed primary reload failed");
        } else {
            require(game.startOrResume(), "Domain startup failed");
        }
        long elapsed = System.nanoTime() - started;
        require(game.getGameTimestamp() == expected, "Wrong game timestamp");
        require(game.reconcileAll(), "Ledger reconciliation failed");
        var entries = game.getLedger().getEntries();
        System.out.printf("mode=%s days=%d elapsedMs=%d maxHeap=%d primaryBytes=%d backupBytes=%d ledgerEntries=%d%n",
                mode, days, elapsed / 1_000_000, Runtime.getRuntime().maxMemory(), storage.getSaveFile().length(),
                storage.getBackupFile().length(), entries.size());
        MessageDigest fingerprint = MessageDigest.getInstance("SHA-256");
        MessageDigest economics = MessageDigest.getInstance("SHA-256");
        for (var entry : entries) {
            String row = String.join("\t", entry.getId(), Long.toString(entry.getTimestamp()), entry.getRequestId(),
                    entry.getAccountId(), entry.getType().name(), entry.getCurrency().name(),
                    entry.getAmount().getAmount().toPlainString(), Boolean.toString(entry.isCredit()),
                    entry.getBalanceAfter().getAmount().toPlainString(), entry.getDescription());
            fingerprint.update((row + "\n").getBytes(StandardCharsets.UTF_8));
            if (game.getAccount(entry.getAccountId()).getType() != com.tradingglobal3d.core.model.AccountType.FIRM) {
                String economicRow = String.join("\t", Long.toString(entry.getTimestamp()), entry.getAccountId(),
                        entry.getType().name(), entry.getCurrency().name(), entry.getAmount().getAmount().toPlainString(),
                        Boolean.toString(entry.isCredit()), entry.getBalanceAfter().getAmount().toPlainString(),
                        entry.getDescription());
                economics.update((economicRow + "\n").getBytes(StandardCharsets.UTF_8));
            }
        }
        var account = game.getPersonalAccount();
        var order = game.getOrderManager().getOrderByRequest("REQ-SU-QA-OPEN");
        var fill = game.getOrderManager().getOrderByRequest("REQ-SU-QA-FILL");
        var fund = game.getMutualFundManager().getFund("FUND-MM-01");
        var position = account.getPosition("IDX:BBCA");
        boolean combined = game.getCompanyManager().getCompany("SU-QA-COMPANY") != null;
        if (fill != null && days > 0) {
            var fundOrder = fund.getOrder("SU-QA-FUND");
            require(fill != null && fill.getStatus() == OrderStatus.FILLED && position != null
                            && (combined || position.getQuantity() == 100),
                    "Player fill or position missing");
            require(fundOrder != null && "SETTLED".equals(fundOrder.getString("status"))
                    && fund.getUnits(account.getId()).signum() > 0, "Fund subscription did not settle");
        }
        if (combined) {
            long settlements = Duration.ofDays(days).toDays() * 48;
            long salaryRows = entries.stream().filter(entry -> entry.getAccountId().equals(account.getId())
                    && entry.getType() == com.tradingglobal3d.core.economy.LedgerEntryType.SALARY).count();
            require(salaryRows == settlements, "Missing or duplicate salary settlements " + salaryRows);
            var company = game.getCompanyManager().getCompany("SU-QA-COMPANY");
            require(company.getLastOperationsDay() == game.getClock().getCurrentDate().minusDays(1).toEpochDay(),
                    "Company operations cursor behind");
            require(game.getCorporateService().getIpo("IPO-CLOUD").isAllotted(), "IPO not allotted");
            require(account.getPosition("IDX:NCLD") != null && account.getPosition("IDX:NCLD").getQuantity() == 200,
                    "IPO distribution missing");
            require(game.getResearchService().getCurrentTier() == TerminalTier.PRO, "Subscription renewal lost");
            long renewals = entries.stream().filter(entry -> entry.getRequestId().startsWith("AUTO-RENEW-PRO-")).count();
            require(renewals == settlements / 30, "Missing or duplicate renewals " + renewals);
            System.out.printf("combined salaryRows=%d renewals=%d companyDay=%d ipoShares=200 debt=%s short=%s%n",
                    salaryRows, renewals, company.getLastOperationsDay(),
                    game.getRiskService().getMarginDebt(account.getId(), Currency.IDR).getAmount(),
                    game.getRiskService().getShortPosition(account.getId(), "BMRI") != null);
        }
        System.out.printf("state clock=%d remainder=%d cashIDR=%s reservedIDR=%s order=%s shares=%d fundUnits=%s logicalLedgerEntries=%d economySha256=%s ledgerSha256=%s%n",
                game.getGameTimestamp(), game.getClock().getGameMsRemainder(), account.getTotalCash(Currency.IDR).getAmount(),
                account.getReservedCash(Currency.IDR).getAmount(), order == null ? "absent" : order.getStatus(),
                position == null ? 0 : position.getQuantity(), fund.getUnits(account.getId()),
                game.getLedger().getEntryCount(), HexFormat.of().formatHex(economics.digest()),
                HexFormat.of().formatHex(fingerprint.digest()));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
