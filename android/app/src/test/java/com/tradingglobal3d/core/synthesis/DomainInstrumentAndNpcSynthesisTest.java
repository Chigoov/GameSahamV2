package com.tradingglobal3d.core.synthesis;

import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.npc.NpcAgent;
import com.tradingglobal3d.core.npc.NpcStrategyType;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderBook;
import com.tradingglobal3d.core.trading.OrderSide;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class DomainInstrumentAndNpcSynthesisTest {

    @Test
    public void testInstrumentVirCalIntrinsicValue() {
        Money initial = Money.of(10000, Currency.IDR);
        Instrument inst = new Instrument("IDX:TEST", "TEST", "Test Emiten", MarketExchange.IDX,
                "MANUFAKTUR", Currency.IDR, 100, 10_000_000L, true, initial);

        // 1. Initial fair value equals price
        assertNotNull(inst.getIntrinsicValue());
        assertEquals(new BigDecimal("10000.00"), inst.getIntrinsicValue());

        // 2. Set metrics: EPS = 800, Sector PER = 15.0 -> Fair = 12.000
        inst.setFundamentalMetrics(new BigDecimal("800.00"), 15.0);
        assertEquals(new BigDecimal("12000.00"), inst.getIntrinsicValue());

        // 3. Clamping max bound: EPS = 2000, PER = 15.0 -> 30.000 -> clamped at 1.95x = 19.500
        inst.setFundamentalMetrics(new BigDecimal("2000.00"), 15.0);
        assertEquals(new BigDecimal("19500.00"), inst.getIntrinsicValue());

        // 4. Clamping min bound: EPS = 100, PER = 15.0 -> 1.500 -> clamped at 0.60x = 6.000
        inst.setFundamentalMetrics(new BigDecimal("100.00"), 15.0);
        assertEquals(new BigDecimal("6000.00"), inst.getIntrinsicValue());

        // 5. Test locked float ratio
        double ratio = inst.getLockedFloatRatio(6_000_000L);
        assertEquals(0.60, ratio, 0.001);
    }

    @Test
    public void testMarketMakerSupplySqueezeSpreadAdjustment() {
        Money initial = Money.of(10000, Currency.IDR);
        Instrument inst = new Instrument("IDX:SQZ", "SQZ", "Squeeze Emiten", MarketExchange.IDX,
                "ENERGIE", Currency.IDR, 100, 10_000_000L, true, initial);

        Account mmAccount = new Account("ACC-NPC-MM-01", "Market Maker", AccountType.PERSONAL, null);
        mmAccount.creditCash(Money.of(10_000_000_000L, Currency.IDR));

        // MM memegang hanya 1.000.000 lembar dari 10.000.000 (90% locked di luar MM -> lockedRatio = 0.90 -> multiplier 2.50x)
        inst.setLockedFloatRatio(0.90);
        mmAccount.addPosition(new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(), 1_000_000L, initial.getAmount(), initial.getAmount()));

        NpcAgent mm = new NpcAgent("NPC-MM", "Market Maker Bot", NpcStrategyType.MARKET_MAKER, mmAccount);
        OrderBook book = new OrderBook(inst.getId());

        List<Order> orders = mm.generateOrders(inst, book, 1000L, 0.0);
        assertFalse("MM harus menghasilkan order", orders.isEmpty());

        Order askOrder = orders.stream().filter(o -> o.getSide() == OrderSide.SELL).findFirst().orElse(null);
        assertNotNull(askOrder);

        // Pada tick IDX untuk harga 10.000 (tick = 50 IDR).
        // Squeeze multiplier 2.50x -> effective tick = ceil(50 * 2.50) = 125 IDR
        // Ask1 = 10000 + 125 = 10125
        assertTrue("Harga ask MM harus merefleksikan supply squeeze: " + askOrder.getLimitPrice(),
                askOrder.getLimitPrice().getAmount().compareTo(new BigDecimal("10050")) > 0);
    }

    @Test
    public void testResearchServiceConsensusFundamentalSignal() throws Exception {
        Money initial = Money.of(10000, Currency.IDR);
        Instrument inst = new Instrument("IDX:BBCA", "BBCA", "Bank Central Asia Tbk", MarketExchange.IDX,
                "FINANCIAL", Currency.IDR, 100, 10_000_000L, true, initial);
        // Fair value = 12.000 (Diskon +20%) -> STRONG_BUY
        inst.setFundamentalMetrics(new BigDecimal("800.00"), 15.0);

        com.tradingglobal3d.core.research.ResearchService research = new com.tradingglobal3d.core.research.ResearchService();
        com.tradingglobal3d.core.economy.EconomyLedger ledger = new com.tradingglobal3d.core.economy.EconomyLedger();
        Account player = new Account("ACC-PLAYER-01", "Player", AccountType.PERSONAL, null);
        player.creditCash(Money.of(100_000_000L, Currency.IDR));
        player.creditCash(Money.of(1_000_000L, Currency.USD));
        boolean subscribed = research.subscribeTerminal(player, com.tradingglobal3d.core.research.TerminalTier.PRO, ledger, 1000L);
        assertTrue("Langganan Pro harus berhasil", subscribed);

        org.json.JSONObject consensus = research.getConsensus(inst, 1000L);
        assertNotNull(consensus);
        assertTrue(consensus.getBoolean("success"));
        assertEquals("12000.00", consensus.getString("intrinsicValue"));
        assertEquals("20.00", consensus.getString("gapRatePercent"));
        assertEquals("STRONG_BUY", consensus.getString("fundamentalSignal"));
    }

    @Test
    public void testOperatingCompanyCompetitivenessAndMacroCycleEngine() throws Exception {
        // 1. Competitiveness score pada OperatingCompany
        com.tradingglobal3d.core.business.OperatingCompany comp = new com.tradingglobal3d.core.business.OperatingCompany(
                "COMP-01", "PT Pabrik Maju", "MANUFAKTUR", "ACC-COMP-01", 1
        );
        double baseScore = comp.getMarketCompetitivenessScore();
        assertTrue("Skor daya saing harus positif: " + baseScore, baseScore > 5.0);

        // Ubah kebijakan kerja ke 10 jam dan benefit Premium -> skor harus naik
        comp.setWorkPolicy(10, com.tradingglobal3d.core.business.OperatingCompany.BenefitTier.PREMIUM);
        double upgradedScore = comp.getMarketCompetitivenessScore();
        assertTrue("Skor setelah upgrade kebijakan kerja harus lebih tinggi", upgradedScore > baseScore);

        // 2. Makro Cycle pada TradingFacade
        java.io.File tempDir = java.nio.file.Files.createTempDirectory("test-macro-facade-").toFile();
        com.tradingglobal3d.core.storage.SaveManager saveManager = com.tradingglobal3d.core.storage.SaveManager.forDirectory(tempDir);
        com.tradingglobal3d.core.facade.TradingFacade facade = new com.tradingglobal3d.core.facade.TradingFacade(saveManager);

        java.io.File catFile = new java.io.File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new java.io.File("src/main/assets/data/market_universe.json");
        facade.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(java.nio.file.Files.readString(catFile.toPath())));
        facade.initializeNewGame(com.tradingglobal3d.core.model.GameMode.INTERNAL_MARKET, new BigDecimal("100000000"));

        assertEquals(5, facade.getMacroIndex());
        assertEquals(6.75, facade.getEffectiveDepositRate(), 0.01);
        assertEquals(10.75, facade.getEffectiveLoanRate(), 0.01);
        assertEquals(1.00, facade.getSectorPieMultiplier(), 0.01);
        assertEquals(1.10, facade.getIpoGainMultiplier(), 0.01);

        // Set ke Level 1 (Resesi)
        facade.setMacroIndex(1);
        assertEquals(1, facade.getMacroIndex());
        assertEquals(9.5, facade.getEffectiveDepositRate(), 0.01);
        assertEquals(13.5, facade.getEffectiveLoanRate(), 0.01);
        assertEquals(0.75, facade.getSectorPieMultiplier(), 0.01);
        assertEquals(0.80, facade.getIpoGainMultiplier(), 0.01);

        // Set ke Level 9 (Booming)
        facade.setMacroIndex(9);
        assertEquals(9, facade.getMacroIndex());
        assertEquals(4.0, facade.getEffectiveDepositRate(), 0.01);
        assertEquals(8.0, facade.getEffectiveLoanRate(), 0.01);
        assertEquals(1.30, facade.getSectorPieMultiplier(), 0.01);
        assertEquals(1.40, facade.getIpoGainMultiplier(), 0.01);

        // Test persistensi save & load macroIndex
        facade.save();
        com.tradingglobal3d.core.facade.TradingFacade restored = new com.tradingglobal3d.core.facade.TradingFacade(saveManager);
        restored.setCatalog(facade.getCatalog());
        restored.load();
        assertEquals("Macro index 9 harus bertahan setelah save/load", 9, restored.getMacroIndex());
    }
}
