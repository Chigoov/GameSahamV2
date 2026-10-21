package com.tradingglobal3d.core;

import com.tradingglobal3d.core.research.*;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class FeatureCompletionResearchTest {
    private final long now = 1_790_000_000_000L;
    private Instrument instrument(String ticker) {
        return new Instrument("IDX:" + ticker, ticker, ticker, MarketExchange.IDX, "QA", Currency.IDR,
                100, 1_000_000, true, Money.of(1000, Currency.IDR));
    }
    private ResearchService service(TerminalTier tier) {
        ResearchService service = new ResearchService(); service.resetForGame(now);
        Account personal = new Account("QA", "QA", AccountType.PERSONAL, null);
        EconomyLedger ledger = new EconomyLedger();
        ledger.recordMutation("QA", personal, LedgerEntryType.INITIAL_CAPITAL, Money.of(10_000, Currency.USD), true, now, "QA");
        assertTrue(service.subscribeTerminal(personal, tier, ledger, now));
        return service;
    }
    private AnalystReport report(String id, String analyst, long time, long target) {
        return new AnalystReport(id, analyst, "Firma QA", "TEST", AnalystReport.Recommendation.HOLD,
                Money.of(target, Currency.IDR), 50, "Opini NPC", time);
    }

    @Test public void premiumQueriesAreEnforcedInsideServiceAndExpire() throws Exception {
        ResearchService service = service(TerminalTier.BASIC);
        try { service.getConsensus(instrument("TEST"), now); fail(); } catch (IllegalStateException expected) { }
        try { service.getAlternativeData(instrument("TEST"), now); fail(); } catch (IllegalStateException expected) { }
        try { service.compareInstruments(Arrays.asList(instrument("TEST"), instrument("TWO")), now); fail(); }
        catch (IllegalStateException expected) { }
        service = service(TerminalTier.PRO);
        assertTrue(service.getConsensus(instrument("TEST"), now).getBoolean("success"));
        try { service.getConsensus(instrument("TEST"), now + ResearchService.THIRTY_DAYS_MS); fail(); }
        catch (IllegalStateException expected) { }
    }

    @Test public void consensusUsesLatestPublishedOpinionPerAnalystAndNeverFuture() throws Exception {
        ResearchService service = service(TerminalTier.PRO);
        service.addAnalystReport(report("OLD", "A", now - 100, 100));
        service.addAnalystReport(report("LATEST", "A", now - 10, 1200));
        service.addAnalystReport(report("SECOND", "B", now, 1800));
        service.addAnalystReport(report("FUTURE", "C", now + 1, 900000));
        JSONObject result = service.getConsensus(instrument("TEST"), now);
        assertEquals(2, result.getInt("sampleCount"));
        assertEquals("1500", result.getString("meanTarget"));
        assertEquals("1500", result.getString("medianTarget"));
        assertFalse(result.toString().contains("FUTURE")); assertFalse(result.toString().contains("OLD"));
        assertEquals(2, result.getInt("hold"));
        assertEquals("EMPTY", service.getConsensus(instrument("OTHER"), now).getString("status"));
    }

    @Test public void inboxAndAlternativeDataExposeProvenanceAndIgnoreFuture() throws Exception {
        ResearchService service = service(TerminalTier.ENTERPRISE);
        service.addAnalystReport(report("VISIBLE", "A", now, 1200));
        service.addAnalystReport(report("HIDDEN", "B", now + 1, 1200));
        service.addNews(new NewsArticle("VISIBLE-NEWS", "Game news", "Game context", NewsArticle.Category.COMPANY, "TEST", -.5, now));
        service.addNews(new NewsArticle("HIDDEN-NEWS", "Future news", "Future", NewsArticle.Category.COMPANY, "TEST", .9, now + 1));
        JSONObject inbox = service.getAnalystInbox(now);
        assertTrue(inbox.toString().contains("VISIBLE")); assertFalse(inbox.toString().contains("HIDDEN"));
        JSONObject data = service.getAlternativeData(instrument("TEST"), now);
        assertTrue(data.toString().contains("VISIBLE-NEWS")); assertFalse(data.toString().contains("HIDDEN-NEWS"));
        assertTrue(data.has("source")); assertTrue(data.has("asOf")); assertTrue(data.has("uncertainty"));
        assertTrue(data.getInt("sampleCount") >= 1);
    }

    @Test public void comparisonIsReadOnlyRequiresDistinctCanonicalInputsAndShowsNativeCurrency() throws Exception {
        ResearchService service = service(TerminalTier.PRO);
        String state = service.exportState().toString();
        JSONObject result = service.compareInstruments(Arrays.asList(instrument("TEST"), instrument("TWO")), now);
        JSONArray rows = result.getJSONArray("instruments");
        assertEquals(2, rows.length()); assertEquals("IDR", rows.getJSONObject(0).getString("currency"));
        assertEquals(state, service.exportState().toString());
        try { service.compareInstruments(Arrays.asList(instrument("TEST"), instrument("TEST")), now); fail(); }
        catch (IllegalArgumentException expected) { }
        try { service.compareInstruments(Arrays.asList(instrument("TEST")), now); fail(); }
        catch (IllegalArgumentException expected) { }
    }
}
