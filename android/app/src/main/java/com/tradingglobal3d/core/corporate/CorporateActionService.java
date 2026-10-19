package com.tradingglobal3d.core.corporate;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Layanan Aksi Korporasi, IPO Penjatahan & Refund, serta RUPS Voting.
 */
public final class CorporateActionService {
    private final List<CorporateAction> corporateActions = new ArrayList<>();
    private final Map<String, IpoOffering> ipoOfferings = new LinkedHashMap<>();
    private final List<IpoSubscription> ipoSubscriptions = new ArrayList<>();
    private final Map<String, ShareholderMeetingAgenda> agendas = new LinkedHashMap<>();

    public CorporateActionService() {
        initializeSampleActions();
    }

    private void initializeSampleActions() {
        // Sample IPO
        IpoOffering cloudIpo = new IpoOffering(
                "IPO-CLOUD", "NCLD", "Nusantara Cloud Tech Tbk", Currency.IDR,
                Money.of(500, Currency.IDR), 100, 20_000_000, LocalDate.now().plusDays(5)
        );
        ipoOfferings.put(cloudIpo.getId(), cloudIpo);

        // Sample RUPS Agenda
        ShareholderMeetingAgenda agenda1 = new ShareholderMeetingAgenda(
                "RUPS-BBCA-01", "BBCA", "Persetujuan Pembagian Dividen Final Tahun Buku 2026",
                "Menyetujui pembagian dividen tunai sebesar Rp 225 per saham kepada pemegang saham yang berhak.",
                LocalDate.now()
        );
        agendas.put(agenda1.getId(), agenda1);

        // Sample Corporate Action
        CorporateAction div1 = new CorporateAction(
                "ACT-DIV-BBCA-2026", "BBCA", CorporateActionType.CASH_DIVIDEND,
                BigDecimal.valueOf(225), Currency.IDR,
                LocalDate.now(), LocalDate.now().plusDays(3)
        );
        corporateActions.add(div1);
    }

    public List<CorporateAction> getCorporateActions() { return Collections.unmodifiableList(corporateActions); }
    public List<IpoOffering> getIpoOfferings() { return new ArrayList<>(ipoOfferings.values()); }
    public List<ShareholderMeetingAgenda> getAgendas() { return new ArrayList<>(agendas.values()); }
    public IpoOffering getIpo(String id) { return ipoOfferings.get(id); }
    public ShareholderMeetingAgenda getAgenda(String id) { return agendas.get(id); }

    public synchronized void scheduleCorporateAction(CorporateAction action) {
        corporateActions.add(Objects.requireNonNull(action));
    }

    public synchronized void registerIpoOffering(IpoOffering ipo) {
        ipoOfferings.put(ipo.getId(), Objects.requireNonNull(ipo));
    }

    public synchronized org.json.JSONObject exportState() throws org.json.JSONException {
        org.json.JSONObject root = new org.json.JSONObject();

        // 1. corporateActions
        org.json.JSONArray actionsArray = new org.json.JSONArray();
        for (CorporateAction ca : corporateActions) {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("id", ca.getId());
            obj.put("ticker", ca.getTicker());
            obj.put("type", ca.getType().name());
            obj.put("valueOrRatio", ca.getValueOrRatio().toPlainString());
            obj.put("currency", ca.getCurrency() != null ? ca.getCurrency().name() : org.json.JSONObject.NULL);
            obj.put("recordingDate", ca.getRecordingDate().toString());
            obj.put("executionDate", ca.getExecutionDate().toString());
            obj.put("isExecuted", ca.isExecuted());
            actionsArray.put(obj);
        }
        root.put("corporateActions", actionsArray);

        // 2. ipoOfferings
        org.json.JSONArray iposArray = new org.json.JSONArray();
        for (IpoOffering ipo : ipoOfferings.values()) {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("id", ipo.getId());
            obj.put("ticker", ipo.getTicker());
            obj.put("companyName", ipo.getCompanyName());
            obj.put("currency", ipo.getCurrency().name());
            obj.put("offeringPrice", ipo.getOfferingPrice().getAmount().toPlainString());
            obj.put("lotSize", ipo.getLotSize());
            obj.put("sharesOffered", ipo.getSharesOffered());
            obj.put("listingDate", ipo.getListingDate().toString());
            obj.put("isAllotted", ipo.isAllotted());
            iposArray.put(obj);
        }
        root.put("ipoOfferings", iposArray);

        // 3. ipoSubscriptions
        org.json.JSONArray subsArray = new org.json.JSONArray();
        for (IpoSubscription sub : ipoSubscriptions) {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("id", sub.getId());
            obj.put("ipoId", sub.getIpoId());
            obj.put("accountId", sub.getAccountId());
            obj.put("orderedLots", sub.getOrderedLots());
            obj.put("reservedAmount", sub.getReservedMoney().getAmount().toPlainString());
            obj.put("reservedCurrency", sub.getReservedMoney().getCurrency().name());
            obj.put("allottedLots", sub.getAllottedLots());
            obj.put("refundAmount", sub.getRefundMoney() != null ? sub.getRefundMoney().getAmount().toPlainString() : org.json.JSONObject.NULL);
            obj.put("status", sub.getStatus().name());
            subsArray.put(obj);
        }
        root.put("ipoSubscriptions", subsArray);

        // 4. agendas
        org.json.JSONArray agendasArray = new org.json.JSONArray();
        for (ShareholderMeetingAgenda a : agendas.values()) {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("id", a.getId());
            obj.put("ticker", a.getTicker());
            obj.put("title", a.getTitle());
            obj.put("resolutionText", a.getResolutionText());
            obj.put("recordingDate", a.getRecordingDate().toString());
            obj.put("votesFor", a.getTotalVotesFor());
            obj.put("votesAgainst", a.getTotalVotesAgainst());
            obj.put("votesAbstain", a.getTotalVotesAbstain());
            org.json.JSONArray voters = new org.json.JSONArray();
            for (String voter : a.getVotedAccountIds()) voters.put(voter);
            obj.put("votedAccountIds", voters);
            agendasArray.put(obj);
        }
        root.put("agendas", agendasArray);

        return root;
    }

    public synchronized void restoreState(org.json.JSONObject root) throws org.json.JSONException {
        corporateActions.clear();
        ipoOfferings.clear();
        ipoSubscriptions.clear();
        agendas.clear();

        if (root.has("corporateActions")) {
            org.json.JSONArray arr = root.getJSONArray("corporateActions");
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                Currency c = obj.isNull("currency") ? null : Currency.valueOf(obj.getString("currency"));
                CorporateAction ca = new CorporateAction(
                        obj.getString("id"), obj.getString("ticker"),
                        CorporateActionType.valueOf(obj.getString("type")),
                        new BigDecimal(obj.getString("valueOrRatio")),
                        c,
                        LocalDate.parse(obj.getString("recordingDate")),
                        LocalDate.parse(obj.getString("executionDate"))
                );
                if (obj.optBoolean("isExecuted", false)) ca.markExecuted();
                corporateActions.add(ca);
            }
        }

        if (root.has("ipoOfferings")) {
            org.json.JSONArray arr = root.getJSONArray("ipoOfferings");
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                Currency c = Currency.valueOf(obj.getString("currency"));
                Money price = new Money(new BigDecimal(obj.getString("offeringPrice")), c);
                IpoOffering ipo = new IpoOffering(
                        obj.getString("id"), obj.getString("ticker"), obj.getString("companyName"), c,
                        price, obj.getInt("lotSize"), obj.getLong("sharesOffered"),
                        LocalDate.parse(obj.getString("listingDate"))
                );
                if (obj.optBoolean("isAllotted", false)) ipo.markAllotted();
                ipoOfferings.put(ipo.getId(), ipo);
            }
        }

        if (root.has("ipoSubscriptions")) {
            org.json.JSONArray arr = root.getJSONArray("ipoSubscriptions");
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                Currency cur = Currency.valueOf(obj.getString("reservedCurrency"));
                Money reserved = new Money(new BigDecimal(obj.getString("reservedAmount")), cur);
                IpoSubscription sub = new IpoSubscription(
                        obj.getString("id"), obj.getString("ipoId"), obj.getString("accountId"),
                        obj.getLong("orderedLots"), reserved
                );
                long allotted = obj.optLong("allottedLots", 0);
                Money refund = obj.isNull("refundAmount") ? null : new Money(new BigDecimal(obj.getString("refundAmount")), cur);
                sub.setAllottedLots(allotted);
                sub.setRefundMoney(refund);
                sub.setStatus(IpoSubscription.SubscriptionStatus.valueOf(obj.getString("status")));
                ipoSubscriptions.add(sub);
            }
        }

        if (root.has("agendas")) {
            org.json.JSONArray arr = root.getJSONArray("agendas");
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                ShareholderMeetingAgenda agenda = new ShareholderMeetingAgenda(
                        obj.getString("id"), obj.getString("ticker"), obj.getString("title"),
                        obj.getString("resolutionText"), LocalDate.parse(obj.getString("recordingDate"))
                );
                long forVotes = obj.optLong("votesFor", 0);
                long againstVotes = obj.optLong("votesAgainst", 0);
                long abstainVotes = obj.optLong("votesAbstain", 0);
                Set<String> voters = new HashSet<>();
                org.json.JSONArray vArr = obj.optJSONArray("votedAccountIds");
                if (vArr != null) {
                    for (int j = 0; j < vArr.length(); j++) voters.add(vArr.getString(j));
                }
                agenda.restoreVotes(forVotes, againstVotes, abstainVotes, voters);
                agendas.put(agenda.getId(), agenda);
            }
        }
    }

    /**
     * Mengeksekusi pembagian dividen tunai ke seluruh akun yang memegang saham.
     */
    public synchronized int executeCashDividend(
            CorporateAction action,
            Instrument instrument,
            Collection<Account> accounts,
            EconomyLedger ledger,
            long gameTimestamp) {

        int beneficiaries = 0;
        BigDecimal divPerShare = action.getValueOrRatio();
        Currency cur = action.getCurrency();

        for (Account acc : accounts) {
            Position pos = acc.getPosition(instrument.getId());
            if (pos != null && pos.getQuantity() > 0) {
                BigDecimal totalDivAmt = divPerShare.multiply(BigDecimal.valueOf(pos.getQuantity()));
                Money dividendMoney = new Money(totalDivAmt, cur);

                ledger.recordMutation(
                        "DIV-" + action.getId() + "-" + acc.getId(),
                        acc,
                        LedgerEntryType.DIVIDEND,
                        dividendMoney,
                        true,
                        gameTimestamp,
                        "Dividen tunai " + action.getTicker() + " @" + divPerShare + " x " + pos.getQuantity() + " lembar"
                );
                beneficiaries++;
            }
        }
        action.markExecuted();
        return beneficiaries;
    }

    /**
     * Mengeksekusi Stock Split (pemecahan saham) secara matematis presisi.
     */
    public synchronized void executeStockSplit(
            CorporateAction action,
            Instrument instrument,
            Collection<Account> accounts) {

        BigDecimal ratio = action.getValueOrRatio(); // e.g. 5.0 untuk 1:5 split

        for (Account acc : accounts) {
            Position pos = acc.getPosition(instrument.getId());
            if (pos != null && pos.getQuantity() > 0) {
                long newQty = (long) (pos.getQuantity() * ratio.doubleValue());
                BigDecimal newAvgCost = pos.getAverageCost().divide(ratio, 4, RoundingMode.HALF_UP);
                BigDecimal newPrice = pos.getCurrentPrice().divide(ratio, 4, RoundingMode.HALF_UP);

                // Buat posisi baru hasil split
                Position splitPos = new Position(pos.getTicker(), pos.getExchange(), pos.getCurrency(), newQty, newAvgCost, newPrice,
                        pos.getRealizedPnL() == null ? null : pos.getRealizedPnL().getAmount());
                acc.addPosition(splitPos);
            }
        }

        // Sesuaikan harga pasar instrumen di bursa
        Money newInstPrice = new Money(instrument.getCurrentPrice().getAmount().divide(ratio, instrument.getCurrency().getDecimalPlaces(), RoundingMode.HALF_UP), instrument.getCurrency());
        instrument.updatePrice(newInstPrice, 0);
        action.markExecuted();
    }

    /**
     * Pemesanan saham IPO dengan penahanan dana (reserve cash).
     */
    public synchronized IpoSubscription orderIpo(
            IpoOffering ipo,
            Account account,
            long orderedLots,
            long gameTimestamp) {

        long totalShares = orderedLots * ipo.getLotSize();
        BigDecimal nominal = ipo.getOfferingPrice().getAmount().multiply(BigDecimal.valueOf(totalShares));
        Money requiredMoney = new Money(nominal, ipo.getCurrency());

        Money available = account.getAvailableCash(ipo.getCurrency());
        if (available.isLessThan(requiredMoney)) {
            throw new IllegalStateException("Saldo kas " + ipo.getCurrency() + " tidak mencukupi untuk pemesanan IPO");
        }

        // Tahan dana pemesanan
        account.reserveCash(requiredMoney);

        String subId = "SUB-IPO-" + ipo.getTicker() + "-" + System.nanoTime();
        IpoSubscription sub = new IpoSubscription(subId, ipo.getId(), account.getId(), orderedLots, requiredMoney);
        ipoSubscriptions.add(sub);
        return sub;
    }

    /**
     * Penjatahan IPO (Allotment) dan pengembalian sisa dana pesanan (Refund).
     */
    public synchronized void allotIpo(
            IpoOffering ipo,
            double allotmentRatio,
            Map<String, Account> accounts,
            EconomyLedger ledger,
            long gameTimestamp) {

        for (IpoSubscription sub : ipoSubscriptions) {
            if (sub.getIpoId().equals(ipo.getId()) && sub.getStatus() == IpoSubscription.SubscriptionStatus.PENDING) {
                Account acc = accounts.get(sub.getAccountId());
                if (acc == null) continue;

                long allottedLots = (long) Math.floor(sub.getOrderedLots() * allotmentRatio);
                long allottedShares = allottedLots * ipo.getLotSize();
                BigDecimal usedNominal = ipo.getOfferingPrice().getAmount().multiply(BigDecimal.valueOf(allottedShares));
                Money usedMoney = new Money(usedNominal, ipo.getCurrency());
                Money refundMoney = sub.getReservedMoney().subtract(usedMoney);

                // 1. Rilis seluruh dana tertahan
                acc.releaseReservedCash(sub.getReservedMoney());

                // 2. Debit nilai yang terpakai untuk saham yang didapat via ledger
                if (allottedShares > 0) {
                    ledger.recordMutation(
                            "IPO-PAY-" + sub.getId(),
                            acc,
                            LedgerEntryType.TRADE_BUY,
                            usedMoney,
                            false,
                            gameTimestamp,
                            "Pembayaran Penjatahan IPO " + ipo.getTicker() + " (" + allottedLots + " lot)"
                    );

                    // Tambahkan saham ke portofolio
                    Position p = acc.getPosition("IDX:" + ipo.getTicker());
                    if (p == null) {
                        p = new Position(ipo.getTicker(), "IDX", ipo.getCurrency(), allottedShares, ipo.getOfferingPrice().getAmount(), ipo.getOfferingPrice().getAmount());
                        acc.addPosition(p);
                    } else {
                        p.addBuy(allottedShares, ipo.getOfferingPrice().getAmount());
                    }
                }

                sub.applyAllotment(allottedLots, refundMoney);
            }
        }
        ipo.markAllotted();
    }

    /**
     * Menyampaikan hak suara pada RUPS secara idempoten.
     */
    public synchronized void castVote(
            ShareholderMeetingAgenda agenda,
            Account account,
            ShareholderMeetingAgenda.VoteChoice choice,
            long gameTimestamp) {

        Position pos = account.getPosition(agenda.getTicker());
        long shares = (pos != null) ? pos.getQuantity() : 0;
        agenda.castVote(account.getId(), choice, shares);
    }
}
