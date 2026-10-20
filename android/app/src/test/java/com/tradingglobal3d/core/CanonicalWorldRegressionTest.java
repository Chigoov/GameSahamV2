package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import static org.junit.Assert.*;

public class CanonicalWorldRegressionTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private static String readFile(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
    private static void writeFile(Path path, String content) throws Exception {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private TradingFacade game() throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(temporary.newFolder()));
        game.setCatalog(InstrumentCatalog.fromJsonString(readFile(Paths.get("src/main/assets/data/market_universe.json"))));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        game.getClock().play();
        return game;
    }
    @Test public void elapsedPartitionsHaveTheSameMarketEvents() throws Exception {
        TradingFacade bulk = game(), split = game();
        bulk.advanceRealMs(137000, true);
        for (int i=0; i<1000; i++) {
            split.advanceRealMs(137, true);
            assertTrue(split.reconcileAll());
        }
        assertEquals(bulk.getClock().getGameTimestamp(), split.getClock().getGameTimestamp());
        assertEquals(bulk.getMarketEngine().getRestingOrders().size(), split.getMarketEngine().getRestingOrders().size());
        java.util.List<Long> bulkTimes = new java.util.ArrayList<>(), splitTimes = new java.util.ArrayList<>();
        for (com.tradingglobal3d.core.trading.Order o : bulk.getMarketEngine().getRestingOrders()) bulkTimes.add(o.getCreatedAt());
        for (com.tradingglobal3d.core.trading.Order o : split.getMarketEngine().getRestingOrders()) splitTimes.add(o.getCreatedAt());
        java.util.Collections.sort(bulkTimes); java.util.Collections.sort(splitTimes);
        assertEquals(bulkTimes, splitTimes);
        assertEquals(economyView(bulk), economyView(split));
    }
    private java.util.List<String> economyView(TradingFacade game) {
        java.util.List<String> values = new java.util.ArrayList<>();
        for (Account account : game.getAllAccounts()) {
            for (Currency currency : Currency.values()) values.add("cash:" + account.getId() + ":" + currency + ":" + account.getTotalCash(currency) + ":" + account.getReservedCash(currency));
            for (Map.Entry<String, Position> position : account.getPositions().entrySet()) values.add("position:" + account.getId() + ":" + position.getKey() + ":" + position.getValue().getQuantity() + ":" + position.getValue().getMarketValue());
        }
        for (com.tradingglobal3d.core.trading.Order order : game.getMarketEngine().getRestingOrders()) values.add("order:" + order.getAccountId() + ":" + order.getInstrumentId() + ":" + order.getSide() + ":" + order.getLimitPrice() + ":" + order.getTotalQuantity() + ":" + order.getFilledQuantity() + ":" + order.getStatus() + ":" + order.getCreatedAt());
        for (com.tradingglobal3d.core.economy.LedgerEntry entry : game.getLedger().getEntries()) if (entry.getType() != com.tradingglobal3d.core.economy.LedgerEntryType.DEPOSIT)
            values.add("ledger:" + entry.getAccountId() + ":" + entry.getType() + ":" + entry.getAmount() + ":" + entry.isCredit() + ":" + entry.getBalanceAfter() + ":" + entry.getTimestamp());
        java.util.Collections.sort(values);
        return values;
    }
    @Test public void malformedNewManagerCannotReplaceLiveAccounts() throws Exception {
        TradingFacade game = game(); game.save();
        Path file = game.getSaveManager().getSaveFile().toPath();
        JSONObject saved = new JSONObject(readFile(file));
        saved.getJSONObject("runtimeState").put("companies", new org.json.JSONArray().put(new JSONObject().put("id", "broken")));
        writeFile(file, saved.toString());
        Account live = game.getPersonalAccount();
        game.deposit("UNSAVED", live.getId(), Money.of(1000, Currency.IDR), "TEST ONLY");
        assertFalse(game.load());
        assertSame(live, game.getPersonalAccount());
        assertEquals(Money.of(50_001_000, Currency.IDR), live.getTotalCash(Currency.IDR));
    }

    @Test public void companyAccountReferencesAreValidatedBeforeCommit() throws Exception {
        for (String mode : new String[]{"missing", "personal", "duplicate"}) {
            TradingFacade game = game();
            game.foundCompany("COMP-TEST", "Usaha Uji", "Jasa", Money.of(25_000_000, Currency.IDR));
            game.save();
            Path file = game.getSaveManager().getSaveFile().toPath();
            JSONObject saved = new JSONObject(readFile(file));
            org.json.JSONArray companies = saved.getJSONObject("runtimeState").getJSONArray("companies");
            JSONObject company = companies.getJSONObject(0);
            if (mode.equals("duplicate")) companies.put(new JSONObject(company.toString()).put("id", "DUPLICATE"));
            else company.put("accountId", mode.equals("missing") ? "NONEXISTENT" : game.getPersonalAccount().getId());
            writeFile(file, saved.toString());
            Account live = game.getPersonalAccount();
            assertFalse(mode, game.load());
            assertSame(live, game.getPersonalAccount());
        }
    }
    @Test public void settlingRecognizedPayrollDoesNotLoseWealthAgain() throws Exception {
        TradingFacade game = game();
        var company = game.foundCompany("COMP-TEST", "Usaha Uji", "Jasa", Money.of(25_000_000, Currency.IDR));
        game.transferCompanyCapital(company.getId(), false, Money.of(25_000_000, Currency.IDR));
        game.getCompanyManager().processDailyOperations(game.getAccount(company.getAccountId()), company,
                game.getLedger(), game.getClock().getGameTimestamp());
        Money wealth = game.getPersonalNetWorth();
        assertEquals(Money.of(45_000_000, Currency.IDR), wealth);
        game.transferCompanyCapital(company.getId(), true, Money.of(5_000_000, Currency.IDR));
        assertEquals(wealth, game.getPersonalNetWorth());
        assertTrue(company.getPayrollArrears().isZero());
        assertEquals(wealth.getAmount().toPlainString(), game.getConsolidatedWealthBreakdown().getString("totalNetWorthRaw"));
        assertTrue(game.reconcileAll());
    }
    @Test public void bridgePaymentRetryPersistsOneReceiptAndRejectsInvalidRestructure() throws Exception {
        TradingFacade game = game();
        var bridge = new com.tradingglobal3d.mobile.TradingWebBridge(game);
        String loanId = game.disburseLoan("LOAN-PERS-01", true, null, Money.of(1_000_000, Currency.IDR), 7).getString("loanId");
        assertEquals(Money.of(50_995_000, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
        String request = new JSONObject().put("loanId", loanId).put("amount", "100000").put("requestId", "PAY-ONCE").toString();
        JSONObject receipt = new JSONObject(bridge.payLoan(request));
        assertTrue(receipt.toString(), receipt.getBoolean("success"));
        assertEquals(Money.of(50_895_000, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
        assertTrue(game.load());
        assertEquals(receipt.toString(), new JSONObject(bridge.payLoan(request)).toString());
        assertEquals(Money.of(50_895_000, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
        int reputation = game.getCareerManager().getReputationScore();
        try { game.restructureLoan(loanId, 0); fail("Invalid extension accepted"); } catch (IllegalArgumentException expected) { }
        assertEquals(reputation, game.getCareerManager().getReputationScore());
        assertTrue(game.reconcileAll());
    }
}


