package com.tradingglobal3d.core;

import com.tradingglobal3d.core.corporate.*;
import com.tradingglobal3d.core.economy.*;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import org.json.JSONObject;
import org.junit.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class FeatureCompletionCorporateTest {
    private static final LocalDate BASE = LocalDate.of(2026, 1, 1);
    private static long timestamp(LocalDate date) { return date.atTime(10, 0).atZone(ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli(); }
    private Account personal(EconomyLedger ledger) {
        Account account = new Account("QA", "QA", AccountType.PERSONAL, null);
        ledger.recordMutation("INIT", account, LedgerEntryType.INITIAL_CAPITAL, Money.of(50_000_000, Currency.IDR), true, timestamp(BASE), "QA initial");
        for (String ticker : new String[]{"BBCA", "BMRI", "TLKM"}) account.addPosition(new Position(ticker, "IDX", Currency.IDR, 1000, new BigDecimal("6000"), new BigDecimal("10000")));
        return account;
    }
    private Map<String, Account> accounts(Account account) { Map<String, Account> accounts = new LinkedHashMap<>(); accounts.put(account.getId(), account); return accounts; }
    private CorporateActionService service() { CorporateActionService service = new CorporateActionService(); service.reset(BASE); return service; }

    @Test public void rightsFreezeRecordOwnershipReserveCancelAndSettleOnce() throws Exception {
        EconomyLedger ledger = new EconomyLedger(); Account account = personal(ledger); Map<String, Account> accounts = accounts(account); CorporateActionService service = service();
        LocalDate record = BASE.plusDays(2); service.processDate(record, accounts, ledger, timestamp(record));
        account.getPosition("IDX:BBCA").addBuy(1000, new BigDecimal("10000"));
        assertEquals(100, service.getOffers(account, record).getJSONObject(0).getLong("entitledShares"));
        JSONObject receipt = service.participateOffer("RIGHTS-BBCA", account, 50, "RIGHTS-1", record, ledger, timestamp(record));
        assertEquals("RESERVED", receipt.getString("status")); assertEquals(420000, account.getReservedCash(Currency.IDR).getAmount().longValueExact());
        service.participateOffer("RIGHTS-BBCA", account, 50, "RIGHTS-1", record, ledger, timestamp(record));
        assertEquals(420000, account.getReservedCash(Currency.IDR).getAmount().longValueExact());
        assertThrows(IllegalArgumentException.class, () -> service.participateOffer("RIGHTS-BBCA", account, 51, "RIGHTS-1", record, ledger, timestamp(record)));
        service.cancelParticipation("RIGHTS-1", account, record); assertTrue(account.getReservedCash(Currency.IDR).isZero());
        service.participateOffer("RIGHTS-BBCA", account, 100, "RIGHTS-2", record, ledger, timestamp(record));
        LocalDate settle = record.plusDays(4); service.processDate(settle, accounts, ledger, timestamp(settle));
        assertEquals(2100, account.getPosition("IDX:BBCA").getQuantity()); assertEquals(49160000, account.getTotalCash(Currency.IDR).getAmount().longValueExact());
        assertEquals(new BigDecimal("10000"), account.getPosition("IDX:BBCA").getCurrentPrice());
        assertTrue(account.getReservedCash(Currency.IDR).isZero()); service.processDate(settle, accounts, ledger, timestamp(settle));
        assertEquals(2100, account.getPosition("IDX:BBCA").getQuantity());
        for (Account item : accounts.values()) assertTrue(ledger.reconcile(item, Currency.IDR));
    }

    @Test public void buybackAndMergerLockSharesThenSettleAgainstTreasury() throws Exception {
        EconomyLedger ledger = new EconomyLedger(); Account account = personal(ledger); Map<String, Account> accounts = accounts(account); CorporateActionService service = service();
        LocalDate date = BASE.plusDays(4); service.processDate(date, accounts, ledger, timestamp(date));
        service.participateOffer("BUYBACK-BMRI", account, 100, "BUYBACK-1", date, ledger, timestamp(date));
        assertEquals(100, service.reservedCorporateShares("QA", "IDX:BMRI"));
        assertThrows(IllegalArgumentException.class, () -> service.participateOffer("MERGER-TLKM-ASII", account, 1, "ODD", date, ledger, timestamp(date)));
        service.participateOffer("MERGER-TLKM-ASII", account, 200, "MERGER-1", date, ledger, timestamp(date));
        CorporateActionService loaded = new CorporateActionService(); loaded.restoreState(service.exportState());
        assertEquals(200, loaded.reservedCorporateShares("QA", "IDX:TLKM"));
        LocalDate settle = BASE.plusDays(8); loaded.processDate(settle, accounts, ledger, timestamp(settle));
        assertEquals(900, account.getPosition("IDX:BMRI").getQuantity()); assertEquals(800, account.getPosition("IDX:TLKM").getQuantity());
        assertEquals(100, account.getPosition("IDX:ASII").getQuantity()); assertEquals(50740000, account.getTotalCash(Currency.IDR).getAmount().longValueExact());
        assertEquals(0, loaded.reservedCorporateShares("QA", "IDX:TLKM"));
        for (Account item : accounts.values()) assertTrue(ledger.reconcile(item, Currency.IDR));
    }

    @Test public void dividendAndVotingUseSnapshotAndVoteClosesWithActualConsequence() throws Exception {
        EconomyLedger ledger = new EconomyLedger(); Account account = personal(ledger); Map<String, Account> accounts = accounts(account); CorporateActionService service = service();
        LocalDate record = BASE.plusDays(1); service.processDate(record, accounts, ledger, timestamp(record));
        account.getPosition("IDX:BBCA").addSell(1000, new BigDecimal("10000"));
        ShareholderMeetingAgenda agenda = service.getAgenda("RUPS-BBCA-01");
        service.castVote(agenda, account, ShareholderMeetingAgenda.VoteChoice.FOR, timestamp(record.plusDays(1)));
        assertEquals(1000, agenda.getTotalVotesFor());
        assertThrows(IllegalStateException.class, () -> service.castVote(agenda, account, ShareholderMeetingAgenda.VoteChoice.AGAINST, timestamp(record.plusDays(1))));
        service.processDate(BASE.plusDays(5), accounts, ledger, timestamp(BASE.plusDays(5)));
        assertEquals("APPROVED", agenda.getResult());
        CorporateAction dividend = service.getCorporateActions().get(0); assertEquals("APPROVED", service.getApprovalStatus(dividend));
        Instrument bbca = new Instrument("IDX:BBCA", "BBCA", "BBCA", MarketExchange.IDX, "Bank", Currency.IDR, 100, 1000000, true, Money.of(10000, Currency.IDR));
        assertEquals(1, service.executeCashDividend(dividend, bbca, accounts.values(), ledger, timestamp(BASE.plusDays(6))));
        assertEquals(50225000, account.getTotalCash(Currency.IDR).getAmount().longValueExact());
        assertEquals(0, service.executeCashDividend(dividend, bbca, accounts.values(), ledger, timestamp(BASE.plusDays(6))));
        CorporateActionService loaded = new CorporateActionService(); loaded.restoreState(service.exportState()); assertEquals("APPROVED", loaded.getAgenda(agenda.getId()).getResult());
    }

    @Test public void reverseSplitPaysFractionWithoutDuplicatingOnRetry() {
        EconomyLedger ledger = new EconomyLedger(); Account account = personal(ledger); account.addPosition(new Position("BBCA", "IDX", Currency.IDR, 101, BigDecimal.valueOf(9000), BigDecimal.valueOf(10000)));
        CorporateActionService service = service(); Instrument inst = new Instrument("IDX:BBCA", "BBCA", "BBCA", MarketExchange.IDX, "Bank", Currency.IDR, 100, 1000000, true, Money.of(10000, Currency.IDR));
        CorporateAction split = new CorporateAction("REVERSE-QA", "BBCA", CorporateActionType.REVERSE_SPLIT, new BigDecimal("0.5"), Currency.IDR, BASE, BASE);
        service.executeStockSplit(split, inst, accounts(account).values(), ledger, timestamp(BASE));
        assertEquals(50, account.getPosition("IDX:BBCA").getQuantity()); assertEquals(50010000, account.getTotalCash(Currency.IDR).getAmount().longValueExact());
        service.executeStockSplit(split, inst, accounts(account).values(), ledger, timestamp(BASE)); assertEquals(50, account.getPosition("IDX:BBCA").getQuantity()); assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void ipoRejectsInvalidAndOverflowAmountsAndNeverOverallocatesSupply() {
        EconomyLedger ledger = new EconomyLedger(); Account account = personal(ledger); CorporateActionService service = service();
        IpoOffering ipo = new IpoOffering("QA-IPO", "NCLD", "QA", Currency.IDR, Money.of(100, Currency.IDR), 100, 100, BASE.plusDays(5));
        assertThrows(IllegalArgumentException.class, () -> service.orderIpo(ipo, account, 0, timestamp(BASE)));
        assertThrows(ArithmeticException.class, () -> service.orderIpo(ipo, account, Long.MAX_VALUE, timestamp(BASE)));
        IpoSubscription subscription = service.orderIpo(ipo, account, 10, "IPO-REQ", timestamp(BASE));
        assertSame(subscription, service.orderIpo(ipo, account, 10, "IPO-REQ", timestamp(BASE)));
        assertEquals(100000, account.getReservedCash(Currency.IDR).getAmount().longValueExact());
        assertThrows(IllegalArgumentException.class, () -> service.orderIpo(ipo, account, 11, "IPO-REQ", timestamp(BASE)));
        assertThrows(IllegalArgumentException.class, () -> service.allotIpo(ipo, Double.NaN, accounts(account), ledger, timestamp(BASE)));
        service.allotIpo(ipo, 1, accounts(account), ledger, timestamp(BASE));
        assertEquals(1, subscription.getAllottedLots()); assertEquals(100, account.getPosition("IDX:NCLD").getQuantity());
        assertTrue(account.getReservedCash(Currency.IDR).isZero()); assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void tenHundredActionSequencesPreserveReservationsAndLedgerThroughReload() throws Exception {
        for (int seed = 0; seed < 10; seed++) {
            EconomyLedger ledger = new EconomyLedger(); Account account = personal(ledger); Map<String, Account> accounts = accounts(account); CorporateActionService service = service();
            LocalDate date = BASE.plusDays(2); service.processDate(date, accounts, ledger, timestamp(date));
            for (int action = 0; action < 100; action++) {
                String id = "QA-" + seed + "-" + action / 5;
                switch(action % 5) {
                    case 0: service.participateOffer("RIGHTS-BBCA", account, 1 + seed, id, date, ledger, timestamp(date)); break;
                    case 1: service.cancelParticipation(id, account, date); break;
                    case 2: CorporateActionService loaded = new CorporateActionService(); loaded.restoreState(service.exportState()); service = loaded; break;
                    case 3: service.processDate(date, accounts, ledger, timestamp(date)); break;
                    default: assertEquals(100, service.getOffers(account, date).getJSONObject(0).getLong("availableShares")); break;
                }
                assertTrue(account.getAvailableCash(Currency.IDR).getAmount().signum() >= 0); assertTrue(ledger.reconcile(account, Currency.IDR));
                assertEquals(account.getTotalCash(Currency.IDR), account.getAvailableCash(Currency.IDR).add(account.getReservedCash(Currency.IDR)));
            }
        }
    }
}
