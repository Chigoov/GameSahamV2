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
    private final Map<String, org.json.JSONObject> offers = new LinkedHashMap<>();
    private final Map<String, org.json.JSONObject> participations = new LinkedHashMap<>();
    private final Map<String, Map<String, Long>> recordedShareholdings = new LinkedHashMap<>();
    private com.tradingglobal3d.core.market.InstrumentCatalog shareSupplyCatalog;

    public synchronized void bindShareSupplyCatalog(com.tradingglobal3d.core.market.InstrumentCatalog catalog) {
        shareSupplyCatalog = Objects.requireNonNull(catalog);
    }

    public synchronized void reconcileShareSupply() {
        if (shareSupplyCatalog == null) return;
        // Persisted event IDs make legacy replay and ordinary reads idempotent.
        final class SupplyEvent {
            long timestamp;
            String id;
            Runnable apply;
            SupplyEvent(long timestamp, String id, Runnable apply) { this.timestamp = timestamp; this.id = id; this.apply = apply; }
        }
        List<SupplyEvent> events = new ArrayList<>();
        for (CorporateAction action : corporateActions) {
            if (!action.isExecuted() || action.isCancelled()
                    || (action.getType() != CorporateActionType.STOCK_SPLIT && action.getType() != CorporateActionType.REVERSE_SPLIT)) continue;
            String instrumentId = action.getTicker().contains(":") ? action.getTicker()
                    : (action.getCurrency() == Currency.USD ? "US:" : "IDX:") + action.getTicker();
            Instrument instrument = shareSupplyCatalog.getById(instrumentId);
            if (instrument == null) throw new IllegalStateException("Instrumen supply split tidak ditemukan");
            String eventId = "SPLIT:" + action.getId();
            events.add(new SupplyEvent(action.getExecutionDate().atStartOfDay().toInstant(java.time.ZoneOffset.ofHours(7)).toEpochMilli(), eventId,
                    () -> instrument.applyShareSupplyEvent(eventId, action.getValueOrRatio(), 0, 0)));
        }
        for (org.json.JSONObject request : participations.values()) {
            if (!"SETTLED".equals(request.optString("status"))) continue;
            org.json.JSONObject offer = offers.get(request.optString("offerId"));
            if (offer == null) throw new IllegalStateException("Penawaran supply tidak ditemukan");
            String kind = offer.optString("kind"), eventId = "CORP:" + request.optString("requestId");
            long shares = request.optLong("shares");
            if (shares <= 0) throw new IllegalStateException("Jumlah supply aksi tidak valid");
            String instrumentId = "MERGER_TENDER".equals(kind) ? offer.optString("targetInstrumentId") : offer.optString("instrumentId");
            Instrument instrument = shareSupplyCatalog.getById(instrumentId);
            if (instrument == null) throw new IllegalStateException("Instrumen supply tender tidak ditemukan");
            long delta = "MERGER_TENDER".equals(kind) ? BigDecimal.valueOf(shares).multiply(new BigDecimal(offer.optString("exchangeRatio"))).longValueExact() : shares;
            long time = request.optLong("settledTimestamp", 0);
            events.add(new SupplyEvent(time, eventId, () -> {
                if ("RIGHTS_ISSUE".equals(kind) || "MERGER_TENDER".equals(kind)) instrument.applyShareSupplyEvent(eventId, BigDecimal.ONE, delta, 0);
                else if ("BUYBACK_TENDER".equals(kind)) instrument.applyShareSupplyEvent(eventId, BigDecimal.ONE, 0, delta);
            }));
            if ("MERGER_TENDER".equals(kind)) {
                Instrument source = shareSupplyCatalog.getById(offer.optString("instrumentId"));
                if (source == null) throw new IllegalStateException("Instrumen sumber merger tidak ditemukan");
                String sourceEventId = eventId + ":SOURCE";
                events.add(new SupplyEvent(time, sourceEventId, () -> {
                    source.applyShareSupplyEvent(sourceEventId, BigDecimal.ONE, 0, shares);
                }));
            }
        }
        events.sort(Comparator.comparingLong((SupplyEvent event) -> event.timestamp).thenComparing(event -> event.id));
        for (SupplyEvent event : events) event.apply.run();
        shareSupplyCatalog.recalculateAllIndices();
    }

    private static final String[][] IPO_CATALOG_CANDIDATES = {
            {"NCLD", "PT Nusantara Cloud Tech Tbk", "IDR", "550", "100", "25000000"},
            {"SOLR", "PT Surya Mandiri Energi Tbk", "IDR", "420", "100", "30000000"},
            {"MEDX", "PT Medika Nusantara Prima Tbk", "IDR", "850", "100", "15000000"},
            {"LOGI", "PT Samudera Logistik Ekspres Tbk", "IDR", "380", "100", "20000000"},
            {"AGRI", "PT Sawit Makmur Lestari Tbk", "IDR", "650", "100", "18000000"},
            {"FOOD", "PT Rasa Boga Nusantara Tbk", "IDR", "920", "100", "12000000"},
            {"EVIN", "PT Elektrindo Kendaraan Hijau Tbk", "IDR", "750", "100", "22000000"},
            {"FINX", "PT Dompet Digital Prima Tbk", "IDR", "1250", "100", "16000000"},
            {"PROP", "PT Griya Metropolitan Prima Tbk", "IDR", "310", "100", "35000000"},
            {"MINE", "PT Mineral Tambang Abadi Tbk", "IDR", "1100", "100", "14000000"},
            {"RETL", "PT Mega Mart Ritelindo Tbk", "IDR", "480", "100", "28000000"},
            {"INFR", "PT Infrastruktur Nusa Raya Tbk", "IDR", "620", "100", "20000000"},
            {"BIOT", "PT Bio Farma Sentosa Tbk", "IDR", "1450", "100", "10000000"},
            {"AERO", "PT Dirgantara Aero Express Tbk", "IDR", "510", "100", "25000000"},
            {"CHIP", "PT Semikonduktor Daya Cipta Tbk", "IDR", "1800", "100", "8000000"},
            {"TELO", "PT Menara Telekomindo Tbk", "IDR", "890", "100", "15000000"},
            {"CHEM", "PT Polimer Kimia Lestari Tbk", "IDR", "440", "100", "30000000"},
            {"GAME", "PT Cipta Kreasi Digital Tbk", "IDR", "720", "100", "18000000"},
            {"COFF", "PT Warisan Kopi Nusantara Tbk", "IDR", "350", "100", "40000000"},
            {"PACK", "PT Kemasan Ramah Lingkungan Tbk", "IDR", "290", "100", "35000000"},
            {"WATE", "PT Tirta Bening Mandiri Tbk", "IDR", "410", "100", "25000000"},
            {"AUTO", "PT Suku Cadang Presisi Tbk", "IDR", "580", "100", "20000000"},
            {"FASH", "PT Tekstil Gaya Nusantara Tbk", "IDR", "260", "100", "45000000"},
            {"SECU", "PT Siber Pertahanan Unggul Tbk", "IDR", "1350", "100", "12000000"},
            {"EDUX", "PT Cendekia Edukasi Global Tbk", "IDR", "490", "100", "20000000"},
            {"HOSP", "PT Jaringan Rumah Sakit Kasih Tbk", "IDR", "980", "100", "14000000"},
            {"RECY", "PT Daur Ulang Sirkular Tbk", "IDR", "390", "100", "30000000"},
            {"PORT", "PT Pelabuhan Maritim Nusantara Tbk", "IDR", "820", "100", "16000000"},
            {"POWR", "PT Pembangkit Surya Terang Tbk", "IDR", "670", "100", "22000000"},
            {"AGRO", "PT Bibit Unggul Pertanian Tbk", "IDR", "530", "100", "25000000"},
            {"ROBO", "PT Mekatronika Otomasi Tbk", "IDR", "1600", "100", "10000000"},
            {"PHAR", "PT Generik Herbal Sejahtera Tbk", "IDR", "760", "100", "18000000"},
            {"DAIR", "PT Peternakan Susu Murni Tbk", "IDR", "430", "100", "28000000"},
            {"CERA", "PT Keramik Bangun Persada Tbk", "IDR", "320", "100", "35000000"},
            {"METL", "PT Baja Presisi Konstruksi Tbk", "IDR", "610", "100", "20000000"},
            {"CLTH", "PT Tenun Mega Indah Tbk", "IDR", "280", "100", "40000000"}
    };

    public synchronized void ensureMonthlyIpoSchedule(LocalDate currentDate) {
        if (currentDate == null) return;
        // Jadwalkan minimal 3 IPO per bulan (R06)
        for (int mOffset = 0; mOffset <= 2; mOffset++) {
            LocalDate targetMonth = currentDate.plusMonths(mOffset);
            int year = targetMonth.getYear();
            int month = targetMonth.getMonthValue();
            for (int slot = 1; slot <= 3; slot++) {
                String ipoId = String.format(Locale.ROOT, "IPO-%04d-%02d-%d", year, month, slot);
                if (!ipoOfferings.containsKey(ipoId)) {
                    int day = (slot == 1) ? 8 : (slot == 2) ? 18 : 28;
                    LocalDate listingDate = LocalDate.of(year, month, Math.min(day, targetMonth.lengthOfMonth()));
                    int candidateIdx = Math.floorMod((year - 2026) * 36 + (month - 1) * 3 + (slot - 1), IPO_CATALOG_CANDIDATES.length);
                    String[] cand = IPO_CATALOG_CANDIDATES[candidateIdx];
                    Currency cur = Currency.valueOf(cand[2]);
                    Money price = Money.of(Double.parseDouble(cand[3]), cur);
                    int lotSize = Integer.parseInt(cand[4]);
                    long shares = Long.parseLong(cand[5]);
                    IpoOffering ipo = new IpoOffering(ipoId, cand[0], cand[1], cur, price, lotSize, shares, listingDate);
                    ipoOfferings.put(ipoId, ipo);
                }
            }
        }
    }

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

    public synchronized void reset() {
        corporateActions.clear();
        ipoOfferings.clear();
        ipoSubscriptions.clear();
        agendas.clear();
        offers.clear(); participations.clear(); recordedShareholdings.clear();
        initializeSampleActions();
    }

    public synchronized void reset(LocalDate gameBase) {
        corporateActions.clear(); ipoOfferings.clear(); ipoSubscriptions.clear(); agendas.clear(); offers.clear(); participations.clear(); recordedShareholdings.clear();
        ensureMonthlyIpoSchedule(gameBase);
        ShareholderMeetingAgenda agenda = new ShareholderMeetingAgenda("RUPS-BBCA-01", "BBCA",
                "Persetujuan pembagian dividen", "Dividen Rp225 per saham mengikuti hak recording date.", gameBase.plusDays(1));
        agendas.put(agenda.getId(), agenda);
        corporateActions.add(new CorporateAction("ACT-DIV-BBCA-2026", "BBCA", CorporateActionType.CASH_DIVIDEND,
                BigDecimal.valueOf(225), Currency.IDR, gameBase.plusDays(1), gameBase.plusDays(6)));
        try {
            registerOffer("RIGHTS-BBCA", "RIGHTS_ISSUE", "IDX:BBCA", "IDX:BBCA", new BigDecimal("8400"), new BigDecimal("0.1"), gameBase.plusDays(2));
            registerOffer("BUYBACK-BMRI", "BUYBACK_TENDER", "IDX:BMRI", "IDX:BMRI", new BigDecimal("7400"), BigDecimal.ONE, gameBase.plusDays(3));
            registerOffer("MERGER-TLKM-ASII", "MERGER_TENDER", "IDX:TLKM", "IDX:ASII", BigDecimal.ZERO, new BigDecimal("0.5"), gameBase.plusDays(4));
        } catch (org.json.JSONException e) { throw new IllegalStateException(e); }
    }

    private void registerOffer(String id, String kind, String source, String target, BigDecimal cash, BigDecimal ratio, LocalDate recording) throws org.json.JSONException {
        offers.put(id, new org.json.JSONObject().put("id", id).put("kind", kind).put("instrumentId", source)
                .put("targetInstrumentId", target).put("currency", "IDR").put("cashPrice", cash.toPlainString())
                .put("exchangeRatio", ratio.toPlainString()).put("targetReferencePrice", "6800")
                .put("recordDate", recording.toString()).put("deadline", recording.plusDays(3).toString())
                .put("settlementDate", recording.plusDays(4).toString()).put("maxShares", 100000L)
                .put("recorded", false).put("entitlements", new org.json.JSONObject()));
    }

    public synchronized boolean isTreasuryHolder(String accountId, String instrumentId) {
        for (org.json.JSONObject offer : offers.values())
            if (("BUYBACK_TENDER".equals(offer.optString("kind")) || "MERGER_TENDER".equals(offer.optString("kind")))
                    && instrumentId.equals(offer.optString("instrumentId"))
                    && accountId.equals("ACC-CORP-" + offer.optString("id"))) return true;
        return false;
    }

    public synchronized long reservedCorporateShares(String accountId, String instrumentId) {
        long reserved = 0;
        for (org.json.JSONObject request : participations.values()) {
            if (accountId.equals(request.optString("accountId")) && instrumentId.equals(request.optString("instrumentId"))
                    && "RESERVED".equals(request.optString("status")) && !"RIGHTS_ISSUE".equals(request.optString("kind")))
                reserved = Math.addExact(reserved, request.optLong("shares"));
        }
        return reserved;
    }

    public synchronized org.json.JSONArray getOffers(Account account, LocalDate date) throws org.json.JSONException {
        org.json.JSONArray result = new org.json.JSONArray();
        for (org.json.JSONObject offer : offers.values()) {
            org.json.JSONObject view = new org.json.JSONObject(offer.toString());
            long entitled = offer.getJSONObject("entitlements").optLong(account.getId(), 0);
            long used = 0;
            org.json.JSONArray requests = new org.json.JSONArray();
            for (org.json.JSONObject request : participations.values()) if (account.getId().equals(request.getString("accountId")) && offer.getString("id").equals(request.getString("offerId"))) {
                requests.put(new org.json.JSONObject(request.toString()));
                if (!"CANCELLED".equals(request.getString("status"))) used = Math.addExact(used, request.getLong("shares"));
            }
            view.remove("entitlements");
            view.put("entitledShares", entitled).put("availableShares", Math.max(0, entitled - used)).put("participations", requests)
                    .put("open", offer.getBoolean("recorded") && !date.isAfter(LocalDate.parse(offer.getString("deadline"))))
                    .put("usedShares", used).put("expiredShares", date.isAfter(LocalDate.parse(offer.getString("deadline"))) ? Math.max(0, entitled - used) : 0)
                    .put("status", !offer.getBoolean("recorded") ? "UPCOMING" : date.isAfter(LocalDate.parse(offer.getString("deadline"))) ? "CLOSED" : "OPEN")
                    .put("fee", "0").put("note", "Ketentuan aksi korporasi game; partisipasi opsional, dana/saham ditahan sampai settlement, dapat dibatalkan sebelum tenggat.");
            result.put(view);
        }
        return result;
    }

    public synchronized org.json.JSONObject participateOffer(String offerId, Account account, long shares, String requestId,
            LocalDate date, EconomyLedger ledger, long timestamp) throws org.json.JSONException {
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 160 || shares <= 0) throw new IllegalArgumentException("Request dan jumlah saham harus sah");
        org.json.JSONObject existing = participations.get(requestId);
        if (existing != null) {
            if (!offerId.equals(existing.getString("offerId")) || !account.getId().equals(existing.getString("accountId")) || shares != existing.getLong("shares")) throw new IllegalArgumentException("Request aksi korporasi dipakai dengan parameter berbeda");
            return new org.json.JSONObject(existing.toString()).put("success", true);
        }
        org.json.JSONObject offer = offers.get(offerId);
        if (offer == null || !offer.getBoolean("recorded") || date.isBefore(LocalDate.parse(offer.getString("recordDate"))) || date.isAfter(LocalDate.parse(offer.getString("deadline")))) throw new IllegalStateException("Penawaran tidak aktif atau recording date belum diproses");
        long entitled = offer.getJSONObject("entitlements").optLong(account.getId(), 0);
        long used = 0, total = 0;
        for (org.json.JSONObject request : participations.values()) if (offerId.equals(request.getString("offerId")) && !"CANCELLED".equals(request.getString("status"))) {
            total = Math.addExact(total, request.getLong("shares"));
            if (account.getId().equals(request.getString("accountId"))) used = Math.addExact(used, request.getLong("shares"));
        }
        if (shares > entitled - used || shares > offer.getLong("maxShares") - total) throw new IllegalArgumentException("Jumlah melebihi hak atau kapasitas penawaran");
        String source = offer.getString("instrumentId"), kind = offer.getString("kind");
        if ("MERGER_TENDER".equals(kind) && BigDecimal.valueOf(shares).multiply(new BigDecimal(offer.getString("exchangeRatio"))).stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("Jumlah saham tender harus menghasilkan unit saham tujuan bulat");
        Money reservation = new Money(new BigDecimal(offer.getString("cashPrice")).multiply(BigDecimal.valueOf(shares)), Currency.IDR);
        if ("RIGHTS_ISSUE".equals(kind)) account.reserveCash(reservation);
        else {
            Position position = account.getPosition(source);
            if (position == null || shares > position.getQuantity() - reservedCorporateShares(account.getId(), source)) throw new IllegalStateException("Saham tersedia tidak cukup untuk tender");
        }
        org.json.JSONObject receipt = new org.json.JSONObject().put("offerId", offerId).put("requestId", requestId).put("accountId", account.getId())
                .put("shares", shares).put("instrumentId", source).put("kind", kind).put("reservedCash", "RIGHTS_ISSUE".equals(kind) ? reservation.getAmount().toPlainString() : "0")
                .put("status", "RESERVED").put("timestamp", timestamp).put("settlementDate", offer.getString("settlementDate"));
        participations.put(requestId, receipt);
        return new org.json.JSONObject(receipt.toString()).put("success", true);
    }

    public synchronized org.json.JSONObject cancelParticipation(String requestId, Account account, LocalDate date) throws org.json.JSONException {
        org.json.JSONObject request = participations.get(requestId);
        if (request == null || !account.getId().equals(request.getString("accountId"))) throw new IllegalArgumentException("Partisipasi tidak ditemukan");
        if ("CANCELLED".equals(request.getString("status"))) return new org.json.JSONObject(request.toString()).put("success", true);
        org.json.JSONObject offer = offers.get(request.getString("offerId"));
        if (!"RESERVED".equals(request.getString("status")) || date.isAfter(LocalDate.parse(offer.getString("deadline")))) throw new IllegalStateException("Pembatalan melewati tenggat atau sudah settled");
        Money cash = new Money(new BigDecimal(request.getString("reservedCash")), Currency.IDR);
        if (cash.isPositive()) account.releaseReservedCash(cash);
        request.put("status", "CANCELLED");
        return new org.json.JSONObject(request.toString()).put("success", true);
    }

    public synchronized void processDate(LocalDate date, Map<String, Account> accounts, EconomyLedger ledger, long timestamp) throws org.json.JSONException {
        for (CorporateAction action : corporateActions) if (!recordedShareholdings.containsKey(action.getId()) && !date.isBefore(action.getRecordingDate())) {
            Map<String, Long> recorded = new LinkedHashMap<>();
            for (Account account : accounts.values()) {
                Position position = account.getPosition(action.getTicker().contains(":") ? action.getTicker() : (action.getCurrency() == Currency.USD ? "US:" : "IDX:") + action.getTicker());
                recorded.put(account.getId(), position == null ? 0 : position.getQuantity());
            }
            recordedShareholdings.put(action.getId(), recorded);
        }
        for (ShareholderMeetingAgenda agenda : agendas.values()) {
            if (!date.isBefore(agenda.getRecordingDate())) agenda.captureEntitlements(accounts.values());
            if (date.isAfter(agenda.getDeadline())) agenda.close();
        }
        for (org.json.JSONObject offer : offers.values()) {
            if (!offer.getBoolean("recorded") && !date.isBefore(LocalDate.parse(offer.getString("recordDate")))) {
                org.json.JSONObject entitled = offer.getJSONObject("entitlements");
                for (Account account : accounts.values()) {
                    Position position = account.getPosition(offer.getString("instrumentId"));
                    long shares = position == null ? 0 : position.getQuantity();
                    if ("RIGHTS_ISSUE".equals(offer.getString("kind"))) shares = BigDecimal.valueOf(shares).multiply(new BigDecimal(offer.getString("exchangeRatio"))).longValue();
                    entitled.put(account.getId(), Math.max(0, shares));
                }
                offer.put("recorded", true);
            }
            if (date.isBefore(LocalDate.parse(offer.getString("settlementDate")))) continue;
            for (org.json.JSONObject request : participations.values()) if (offer.getString("id").equals(request.getString("offerId")) && "RESERVED".equals(request.getString("status"))) settleParticipation(offer, request, accounts, ledger, timestamp);
        }
    }

    private void settleParticipation(org.json.JSONObject offer, org.json.JSONObject request, Map<String, Account> accounts, EconomyLedger ledger, long timestamp) throws org.json.JSONException {
        Account account = accounts.get(request.getString("accountId"));
        if (account == null) throw new IllegalStateException("Akun partisipasi hilang; settlement ditunda");
        String sourceId = offer.getString("instrumentId"), kind = offer.getString("kind"), id = request.getString("requestId");
        long shares = request.getLong("shares");
        if (shareSupplyCatalog != null) {
            String eventId = "CORP:" + id;
            if ("MERGER_TENDER".equals(kind)) {
                long targetShares = BigDecimal.valueOf(shares).multiply(new BigDecimal(offer.getString("exchangeRatio"))).longValueExact();
                shareSupplyCatalog.getById(offer.getString("targetInstrumentId")).validateShareSupplyEvent(eventId, BigDecimal.ONE, targetShares, 0);
                shareSupplyCatalog.getById(sourceId).validateShareSupplyEvent(eventId + ":SOURCE", BigDecimal.ONE, 0, shares);
            } else shareSupplyCatalog.getById(sourceId).validateShareSupplyEvent(eventId, BigDecimal.ONE,
                    "RIGHTS_ISSUE".equals(kind) ? shares : 0, "BUYBACK_TENDER".equals(kind) ? shares : 0);
        }
        Money payment = new Money(new BigDecimal(offer.getString("cashPrice")).multiply(BigDecimal.valueOf(shares)), Currency.IDR);
        Position source = account.getPosition(sourceId);
        if ("RIGHTS_ISSUE".equals(kind)) {
            if (account.getReservedCash(Currency.IDR).isLessThan(payment)) throw new IllegalStateException("Reservasi rights tidak cocok");
        } else if (source == null || source.getQuantity() < shares) throw new IllegalStateException("Saham tender hilang; settlement memerlukan pemulihan");
        String issuerId = "ACC-CORP-" + offer.getString("id");
        Account issuer = accounts.get(issuerId);
        if (issuer == null) {
            issuer = new Account(issuerId, "Treasuri " + offer.getString("id"), com.tradingglobal3d.core.model.AccountType.COMPANY, null);
            accounts.put(issuerId, issuer);
            BigDecimal budget = new BigDecimal(offer.getString("cashPrice")).multiply(BigDecimal.valueOf(offer.getLong("maxShares")));
            if (budget.signum() > 0 && !"RIGHTS_ISSUE".equals(kind)) ledger.recordMutation("CORP-BUDGET-" + offer.getString("id"), issuer, LedgerEntryType.INITIAL_CAPITAL, new Money(budget, Currency.IDR), true, timestamp, "Anggaran treasuri terbatas untuk tender game");
        }
        if ("RIGHTS_ISSUE".equals(kind)) {
            account.releaseReservedCash(payment);
            ledger.recordMutation("RIGHTS-PAY-" + id, account, LedgerEntryType.TRADE_BUY, payment, false, timestamp, "Pelaksanaan hak " + offer.getString("id"));
            ledger.recordMutation("RIGHTS-ISSUER-" + id, issuer, LedgerEntryType.DEPOSIT, payment, true, timestamp, "Dana emisi rights dari " + account.getId());
            if (source == null) { source = new Position(sourceId.substring(sourceId.indexOf(':') + 1), "IDX", Currency.IDR, 0, BigDecimal.ZERO, new BigDecimal(offer.getString("cashPrice"))); account.addPosition(source); }
            BigDecimal marketMark = source.getCurrentPrice();
            source.addBuy(shares, new BigDecimal(offer.getString("cashPrice")));
            source.updateMarketPrice(marketMark);
        } else {
            if (issuer.getAvailableCash(Currency.IDR).isLessThan(payment)) throw new IllegalStateException("Anggaran tender tidak cukup");
            String targetId = offer.getString("targetInstrumentId");
            long targetShares = 0;
            BigDecimal targetPrice = new BigDecimal(offer.getString("targetReferencePrice"));
            if ("MERGER_TENDER".equals(kind)) {
                BigDecimal converted = BigDecimal.valueOf(shares).multiply(new BigDecimal(offer.getString("exchangeRatio")));
                targetShares = converted.setScale(0, RoundingMode.DOWN).longValueExact();
                BigDecimal fraction = converted.subtract(BigDecimal.valueOf(targetShares));
                payment = payment.add(new Money(fraction.multiply(targetPrice), Currency.IDR));
                // Fractional claims require a separately funded cash-in-lieu budget, never unbacked player credit.
                if (payment.isPositive() && issuer.getAvailableCash(Currency.IDR).isLessThan(payment)) throw new IllegalStateException("Anggaran cash-in-lieu tidak cukup; gunakan jumlah saham kelipatan rasio");
            }
            BigDecimal totalConsideration = payment.getAmount().add(targetPrice.multiply(BigDecimal.valueOf(targetShares)));
            BigDecimal marketMark = source.getCurrentPrice();
            source.addSell(shares, totalConsideration.divide(BigDecimal.valueOf(shares), 8, RoundingMode.HALF_UP));
            source.updateMarketPrice(marketMark);
            if (payment.isPositive()) {
                ledger.recordMutation("TENDER-ISSUER-" + id, issuer, LedgerEntryType.TRADE_BUY, payment, false, timestamp, "Pembayaran tender " + account.getId());
                ledger.recordMutation("TENDER-PAY-" + id, account, LedgerEntryType.TRADE_SELL, payment, true, timestamp, "Penerimaan tender " + offer.getString("id"));
            }
            if (targetShares > 0) {
                Position target = account.getPosition(targetId);
                if (target == null) { target = new Position(targetId.substring(targetId.indexOf(':') + 1), "IDX", Currency.IDR, 0, BigDecimal.ZERO, targetPrice); account.addPosition(target); }
                target.addBuy(targetShares, targetPrice);
            }
            Position treasury = issuer.getPosition(sourceId);
            if (treasury == null) { treasury = new Position(sourceId.substring(sourceId.indexOf(':') + 1), "IDX", Currency.IDR, 0, BigDecimal.ZERO, source.getCurrentPrice()); issuer.addPosition(treasury); }
            treasury.addBuy(shares, totalConsideration.divide(BigDecimal.valueOf(shares), 8, RoundingMode.HALF_UP));
        }
        request.put("status", "SETTLED").put("settledTimestamp", timestamp).put("cashSettled", payment.getAmount().toPlainString());
        reconcileShareSupply();
    }

    public List<CorporateAction> getCorporateActions() { return Collections.unmodifiableList(corporateActions); }
    public List<IpoOffering> getIpoOfferings() { return new ArrayList<>(ipoOfferings.values()); }
    public List<ShareholderMeetingAgenda> getAgendas() { return new ArrayList<>(agendas.values()); }
    public IpoOffering getIpo(String id) { return ipoOfferings.get(id); }
    public ShareholderMeetingAgenda getAgenda(String id) { return agendas.get(id); }

    public synchronized String getApprovalStatus(CorporateAction action) {
        if (!"ACT-DIV-BBCA-2026".equals(action.getId())) return "NOT_REQUIRED";
        ShareholderMeetingAgenda agenda = agendas.get("RUPS-BBCA-01");
        return agenda == null ? "NOT_REQUIRED" : !agenda.isClosed() ? "PENDING" : agenda.getResult();
    }

    public synchronized void scheduleCorporateAction(CorporateAction action) {
        corporateActions.add(Objects.requireNonNull(action));
    }

    public synchronized void registerIpoOffering(IpoOffering ipo) {
        ipoOfferings.put(ipo.getId(), Objects.requireNonNull(ipo));
    }

    public synchronized void registerAgenda(ShareholderMeetingAgenda agenda) {
        agendas.put(agenda.getId(), Objects.requireNonNull(agenda));
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
            obj.put("cancelled", ca.isCancelled());
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
            obj.put("eligibility", a.exportEligibility());
            obj.put("votesFor", a.getTotalVotesFor());
            obj.put("votesAgainst", a.getTotalVotesAgainst());
            obj.put("votesAbstain", a.getTotalVotesAbstain());
            org.json.JSONArray voters = new org.json.JSONArray();
            for (String voter : a.getVotedAccountIds()) voters.put(voter);
            obj.put("votedAccountIds", voters);
            agendasArray.put(obj);
        }
        root.put("agendas", agendasArray);
        root.put("offers", new org.json.JSONArray(offers.values()));
        root.put("participations", new org.json.JSONArray(participations.values()));
        root.put("recordedShareholdings", new org.json.JSONObject(recordedShareholdings));

        return root;
    }

    public synchronized void restoreState(org.json.JSONObject root) throws org.json.JSONException {
        corporateActions.clear();
        ipoOfferings.clear();
        ipoSubscriptions.clear();
        agendas.clear();
        offers.clear(); participations.clear(); recordedShareholdings.clear();

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
                if (obj.optBoolean("cancelled", false)) ca.cancel();
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
                agenda.restoreEligibility(obj.optJSONObject("eligibility"));
                agendas.put(agenda.getId(), agenda);
            }
        }
        org.json.JSONArray savedOffers = root.optJSONArray("offers");
        if (savedOffers != null) for (int i = 0; i < savedOffers.length(); i++) {
            org.json.JSONObject offer = new org.json.JSONObject(savedOffers.getJSONObject(i).toString());
            if (!Arrays.asList("RIGHTS_ISSUE", "BUYBACK_TENDER", "MERGER_TENDER").contains(offer.getString("kind")) || offer.getLong("maxShares") <= 0) throw new IllegalArgumentException("Penawaran save tidak valid");
            if (offers.put(offer.getString("id"), offer) != null) throw new IllegalArgumentException("Penawaran save duplikat");
        }
        org.json.JSONArray savedRequests = root.optJSONArray("participations");
        if (savedRequests != null) for (int i = 0; i < savedRequests.length(); i++) {
            org.json.JSONObject request = new org.json.JSONObject(savedRequests.getJSONObject(i).toString());
            if (!offers.containsKey(request.getString("offerId")) || request.getLong("shares") <= 0 || !Arrays.asList("RESERVED", "SETTLED", "CANCELLED").contains(request.getString("status"))) throw new IllegalArgumentException("Partisipasi save tidak valid");
            if (participations.put(request.getString("requestId"), request) != null) throw new IllegalArgumentException("Partisipasi save duplikat");
        }
        org.json.JSONObject savedRecordings = root.optJSONObject("recordedShareholdings");
        if (savedRecordings != null) { java.util.Iterator<String> _ak = savedRecordings.keys(); while (_ak.hasNext()) { String actionId = _ak.next();
            Map<String, Long> shares = new LinkedHashMap<>(); org.json.JSONObject values = savedRecordings.getJSONObject(actionId);
            java.util.Iterator<String> _vk = values.keys(); while (_vk.hasNext()) { String accountId = _vk.next(); long qty = values.getLong(accountId); if (qty < 0) throw new IllegalArgumentException("Recording shares negatif"); shares.put(accountId, qty); }
            recordedShareholdings.put(actionId, shares);
        } }
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
        if (action.isExecuted() || action.isCancelled()) return 0;
        BigDecimal divPerShare = action.getValueOrRatio();
        Currency cur = action.getCurrency();

        for (Account acc : accounts) {
            Position pos = acc.getPosition(instrument.getId());
            long entitled = recordedShareholdings.containsKey(action.getId()) ? recordedShareholdings.get(action.getId()).getOrDefault(acc.getId(), 0L) : pos == null ? 0 : pos.getQuantity();
            if (entitled > 0) {
                BigDecimal totalDivAmt = divPerShare.multiply(BigDecimal.valueOf(entitled));
                Money dividendMoney = new Money(totalDivAmt, cur);

                ledger.recordMutation(
                        "DIV-" + action.getId() + "-" + acc.getId(),
                        acc,
                        LedgerEntryType.DIVIDEND,
                        dividendMoney,
                        true,
                        gameTimestamp,
                        "Dividen tunai " + action.getTicker() + " @" + divPerShare + " x " + entitled + " lembar recording date"
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
        if (action.isExecuted() || action.isCancelled()) return;
        if (ratio.signum() <= 0) throw new IllegalArgumentException("Rasio split wajib positif");
        instrument.validateShareSupplyEvent("SPLIT:" + action.getId(), ratio, 0, 0);
        for (Account account : accounts) {
            Position position = account.getPosition(instrument.getId());
            if (position != null) {
                BigDecimal.valueOf(position.getQuantity()).multiply(ratio).longValueExact();
                BigDecimal.valueOf(position.getAcquisitionFeeQuantity()).multiply(ratio).longValueExact();
            }
        }

        for (Account acc : accounts) {
            Position pos = acc.getPosition(instrument.getId());
            if (pos != null && pos.getAcquisitionFeeQuantity() > 0) {
                long newQty = BigDecimal.valueOf(pos.getQuantity()).multiply(ratio).longValueExact();
                BigDecimal newAvgCost = pos.getAverageCost().divide(ratio, 4, RoundingMode.HALF_UP);
                BigDecimal newPrice = pos.getCurrentPrice().divide(ratio, 4, RoundingMode.HALF_UP);

                // Buat posisi baru hasil split
                Position splitPos = new Position(pos.getTicker(), pos.getExchange(), pos.getCurrency(), newQty, newAvgCost, newPrice,
                        pos.getRealizedPnL() == null ? null : pos.getRealizedPnL().getAmount(), pos.getAcquisitionFeesRemaining(),
                        BigDecimal.valueOf(pos.getAcquisitionFeeQuantity()).multiply(ratio).longValueExact());
                acc.addPosition(splitPos);
            }
        }

        instrument.applyShareSupplyEvent("SPLIT:" + action.getId(), ratio, 0, 0);
        instrument.adjustSplitReferencePrices(ratio);
        // Sesuaikan harga pasar instrumen di bursa
        Money newInstPrice = new Money(instrument.getCurrentPrice().getAmount().divide(ratio, instrument.getCurrency().getDecimalPlaces(), RoundingMode.HALF_UP), instrument.getCurrency());
        instrument.updatePrice(newInstPrice, 0);
        action.markExecuted();
    }

    public synchronized void executeStockSplit(CorporateAction action, Instrument instrument, Collection<Account> accounts,
            EconomyLedger ledger, long timestamp) {
        if (action.isExecuted() || action.isCancelled()) return;
        BigDecimal ratio = action.getValueOrRatio();
        if (ratio.signum() <= 0) throw new IllegalArgumentException("Rasio split wajib positif");
        instrument.validateShareSupplyEvent("SPLIT:" + action.getId(), ratio, 0, 0);
        BigDecimal splitPrice = instrument.getCurrentPrice().getAmount().divide(ratio, 8, RoundingMode.HALF_UP);
        for (Account account : accounts) {
            Position position = account.getPosition(instrument.getId());
            if (position != null) {
                BigDecimal.valueOf(position.getQuantity()).multiply(ratio).setScale(0, RoundingMode.DOWN).longValueExact();
                BigDecimal.valueOf(position.getAcquisitionFeeQuantity()).multiply(ratio).setScale(0, RoundingMode.DOWN).longValueExact();
            }
        }
        for (Account account : accounts) {
            Position position = account.getPosition(instrument.getId());
            if (position == null || position.getAcquisitionFeeQuantity() == 0) continue;
            BigDecimal exact = BigDecimal.valueOf(position.getQuantity()).multiply(ratio);
            long whole = exact.setScale(0, RoundingMode.DOWN).longValueExact();
            BigDecimal fractional = exact.subtract(BigDecimal.valueOf(whole));
            BigDecimal average = position.getAverageCost().divide(ratio, 8, RoundingMode.HALF_UP);
            BigDecimal fractionalPnl = fractional.multiply(splitPrice.subtract(average));
            BigDecimal realized = position.getRealizedPnL() == null ? null : position.getRealizedPnL().getAmount().add(fractionalPnl);
            BigDecimal exactFeeQuantity = BigDecimal.valueOf(position.getAcquisitionFeeQuantity()).multiply(ratio);
            long feeQuantity = exactFeeQuantity.setScale(0, RoundingMode.DOWN).longValueExact();
            Money remainingFees = feeQuantity == 0 ? Money.zero(position.getCurrency()) : new Money(
                    position.getAcquisitionFeesRemaining().getAmount().multiply(BigDecimal.valueOf(feeQuantity))
                            .divide(exactFeeQuantity, position.getCurrency().getDecimalPlaces(), RoundingMode.DOWN), position.getCurrency());
            account.addPosition(new Position(position.getTicker(), position.getExchange(), position.getCurrency(), whole, average, splitPrice,
                    realized, remainingFees, feeQuantity));
            Money cashInLieu = new Money(fractional.multiply(splitPrice), position.getCurrency());
            if (cashInLieu.isPositive()) ledger.recordMutation("SPLIT-FRACTION-" + action.getId() + "-" + account.getId(), account, LedgerEntryType.TRADE_SELL, cashInLieu, true, timestamp, "Cash-in-lieu fraksi reverse split " + action.getId());
        }
        instrument.applyShareSupplyEvent("SPLIT:" + action.getId(), ratio, 0, 0);
        instrument.adjustSplitReferencePrices(ratio);
        instrument.updatePrice(new Money(splitPrice, instrument.getCurrency()), 0);
        action.markExecuted();
        if (shareSupplyCatalog != null) shareSupplyCatalog.recalculateAllIndices();
    }

    /**
     * Pemesanan saham IPO dengan penahanan dana (reserve cash).
     */
    public synchronized IpoSubscription orderIpo(
            IpoOffering ipo,
            Account account,
            long orderedLots,
            long gameTimestamp) {
        return orderIpo(ipo, account, orderedLots, "SUB-IPO-" + ipo.getTicker() + "-" + System.nanoTime(), gameTimestamp);
    }

    public synchronized IpoSubscription orderIpo(IpoOffering ipo, Account account, long orderedLots, String requestId, long gameTimestamp) {
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 160) throw new IllegalArgumentException("Identitas permintaan IPO wajib");
        for (IpoSubscription existing : ipoSubscriptions) if (requestId.equals(existing.getId())) {
            if (!existing.getIpoId().equals(ipo.getId()) || !existing.getAccountId().equals(account.getId()) || existing.getOrderedLots() != orderedLots) throw new IllegalArgumentException("Request IPO dipakai dengan parameter berbeda");
            return existing;
        }

        if (ipo.isAllotted() || orderedLots <= 0) throw new IllegalArgumentException("IPO sudah dijatahkan atau jumlah lot tidak positif");
        LocalDate date = java.time.Instant.ofEpochMilli(gameTimestamp).atZone(java.time.ZoneId.of("Asia/Jakarta")).toLocalDate();
        if (date.isAfter(ipo.getListingDate())) throw new IllegalStateException("Masa pemesanan IPO sudah ditutup");
        long totalShares = Math.multiplyExact(orderedLots, ipo.getLotSize());
        BigDecimal nominal = ipo.getOfferingPrice().getAmount().multiply(BigDecimal.valueOf(totalShares));
        Money requiredMoney = new Money(nominal, ipo.getCurrency());

        Money available = account.getAvailableCash(ipo.getCurrency());
        if (available.isLessThan(requiredMoney)) {
            throw new IllegalStateException("Saldo kas " + ipo.getCurrency() + " tidak mencukupi untuk pemesanan IPO");
        }

        // Tahan dana pemesanan
        account.reserveCash(requiredMoney);

        String subId = requestId;
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

        if (ipo.isAllotted()) return;
        if (!Double.isFinite(allotmentRatio) || allotmentRatio < 0 || allotmentRatio > 1) throw new IllegalArgumentException("Rasio penjatahan harus 0..1");
        long requestedLots = 0;
        for (IpoSubscription sub : ipoSubscriptions) if (sub.getIpoId().equals(ipo.getId()) && sub.getStatus() == IpoSubscription.SubscriptionStatus.PENDING) requestedLots = Math.addExact(requestedLots, sub.getOrderedLots());
        double actualRatio = requestedLots == 0 ? 0 : Math.min(allotmentRatio, (double)(ipo.getSharesOffered() / ipo.getLotSize()) / requestedLots);
        Map<String, BigDecimal> requiredReservations = new HashMap<>();
        for (IpoSubscription sub : ipoSubscriptions) if (sub.getIpoId().equals(ipo.getId()) && sub.getStatus() == IpoSubscription.SubscriptionStatus.PENDING) {
            Account account = accounts.get(sub.getAccountId());
            if (account == null) throw new IllegalStateException("Akun pemesanan IPO hilang; penjatahan ditunda");
            requiredReservations.merge(account.getId(), sub.getReservedMoney().getAmount(), BigDecimal::add);
        }
        for (Map.Entry<String, BigDecimal> required : requiredReservations.entrySet()) if (accounts.get(required.getKey()).getReservedCash(ipo.getCurrency()).getAmount().compareTo(required.getValue()) < 0) throw new IllegalStateException("Reservasi IPO tidak cocok");
        for (IpoSubscription sub : ipoSubscriptions) {
            if (sub.getIpoId().equals(ipo.getId()) && sub.getStatus() == IpoSubscription.SubscriptionStatus.PENDING) {
                Account acc = accounts.get(sub.getAccountId());
                if (acc == null) continue;

                long allottedLots = BigDecimal.valueOf(sub.getOrderedLots()).multiply(BigDecimal.valueOf(actualRatio)).setScale(0, RoundingMode.DOWN).longValueExact();
                long allottedShares = Math.multiplyExact(allottedLots, ipo.getLotSize());
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
                    String issuerId = "ACC-IPO-" + ipo.getId();
                    Account issuer = accounts.get(issuerId);
                    if (issuer == null) { issuer = new Account(issuerId, "Emiten " + ipo.getCompanyName(), com.tradingglobal3d.core.model.AccountType.COMPANY, null); accounts.put(issuerId, issuer); }
                    ledger.recordMutation("IPO-ISSUER-" + sub.getId(), issuer, LedgerEntryType.DEPOSIT, usedMoney, true, gameTimestamp, "Penerimaan emisi IPO dari " + acc.getId());

                    // Tambahkan saham ke portofolio
                    String exchange = ipo.getCurrency() == Currency.USD ? "US" : "IDX";
                    Position p = acc.getPosition(exchange + ":" + ipo.getTicker());
                    if (p == null) {
                        p = new Position(ipo.getTicker(), exchange, ipo.getCurrency(), allottedShares, ipo.getOfferingPrice().getAmount(), ipo.getOfferingPrice().getAmount());
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

        LocalDate date = java.time.Instant.ofEpochMilli(gameTimestamp).atZone(java.time.ZoneId.of("Asia/Jakarta")).toLocalDate();
        if (date.isBefore(agenda.getRecordingDate()) || date.isAfter(agenda.getDeadline())) throw new IllegalStateException("Voting di luar jendela waktu");
        if (date.equals(agenda.getRecordingDate())) {
            agenda.captureAccountEntitlement(account);
        } else if (!agenda.isRecorded()) {
            throw new IllegalStateException("Snapshot hak suara tidak tersedia; kepemilikan setelah recording date tidak dapat dipakai");
        }
        long shares = agenda.getEntitledShares(account.getId());
        agenda.castVote(account.getId(), choice, shares);
    }
}
