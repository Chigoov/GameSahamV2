package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.*;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.multiasset.*;
import org.json.*;
import org.junit.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

public class FeatureCompletionMultiAssetTest {
    private static final long START = LocalDate.of(2026,10,19).atStartOfDay(ZoneOffset.ofHours(7)).toInstant().toEpochMilli();
    private static final class World {
        final FundBuilderService products = new FundBuilderService();
        MultiAssetTradingService service = new MultiAssetTradingService(products);
        final EconomyLedger ledger = new EconomyLedger();
        final Account owner = new Account("QA", "QA", AccountType.PERSONAL, null);
        final Account dealer = new Account("DEALER", "QA dealer", AccountType.FIRM, null);
        final Map<String,Account> accounts = new LinkedHashMap<>();
        World() {
            accounts.put(owner.getId(), owner); accounts.put(dealer.getId(), dealer);
            ledger.recordMutation("OWNER-SEED", owner, LedgerEntryType.INITIAL_CAPITAL, Money.of(100_000_000, Currency.IDR), true, START, "QA");
            ledger.recordMutation("DEALER-SEED", dealer, LedgerEntryType.INITIAL_CAPITAL, Money.of(100_000_000, Currency.IDR), true, START, "QA");
            service.initializeDealer(dealer);
        }
        JSONObject trade(String request, String asset, boolean buy, long quantity, long now) throws Exception {
            JSONObject q = service.quote(asset,buy,quantity,owner,dealer,now);
            return service.transact(request,asset,buy,quantity,q.getString("quoteVersion"),owner,dealer,ledger,now);
        }
        void invariants() { assertTrue(ledger.reconcile(owner,Currency.IDR)); assertTrue(ledger.reconcile(dealer,Currency.IDR)); }
    }
    @Test public void cashFeesHoldingsAndDuplicateReceiptsAreExact() throws Exception {
        World w = new World(); Money before = w.owner.getTotalCash(Currency.IDR);
        JSONObject buy = w.trade("BUY", "COMM:GOLD", true, 2, START);
        assertEquals(before.subtract(Money.of(2_902_900,Currency.IDR)),w.owner.getTotalCash(Currency.IDR));
        Money once = w.owner.getTotalCash(Currency.IDR);
        w.service.transact("BUY","COMM:GOLD",true,2,buy.getString("quoteVersion"),w.owner,w.dealer,w.ledger,START+1);
        assertEquals(once,w.owner.getTotalCash(Currency.IDR));
        assertEquals(Money.of(2900,Currency.IDR), w.owner.getPosition("COMM:XAU").getAcquisitionFeesRemaining());
        JSONObject sale = w.trade("SELL","COMM:GOLD",false,2,START+1000);
        assertEquals("-5800", sale.getString("realizedNetPnL"));
        assertTrue(w.owner.getPosition("COMM:XAU").getAcquisitionFeesRemaining().isZero());
        assertTrue(w.dealer.getPosition("COMM:XAU").getAcquisitionFeesRemaining().isZero());
        assertEquals(before.subtract(Money.of(5800,Currency.IDR)),w.owner.getTotalCash(Currency.IDR)); w.invariants();
    }
    @Test public void couponIsProportionalIssuerFundedAndMaturityIsAtomic() throws Exception {
        World w = new World(); w.trade("BOND","BOND:FR0091",true,10,START);
        long quarter = START+90L*86_400_000L;
        Money ownerBefore = w.owner.getTotalCash(Currency.IDR), dealerBefore = w.dealer.getTotalCash(Currency.IDR);
        w.service.settleCoupon(w.owner,w.dealer,w.ledger,quarter);
        Money paid = w.owner.getTotalCash(Currency.IDR).subtract(ownerBefore);
        assertEquals(Money.of(157191,Currency.IDR),paid);
        assertEquals(dealerBefore.subtract(paid),w.dealer.getTotalCash(Currency.IDR));
        int count = w.ledger.getEntries().size(); w.service.settleCoupon(w.owner,w.dealer,w.ledger,quarter);
        assertEquals(count,w.ledger.getEntries().size());
        w.ledger.recordMutation("ISSUER-LOSS",w.dealer,LedgerEntryType.TRADE_BUY,w.dealer.getAvailableCash(Currency.IDR),false,quarter,"QA shortage");
        long maturity = w.products.getBond("BOND:FR0091").getMaturityDate().atStartOfDay(ZoneOffset.ofHours(7)).toInstant().toEpochMilli();
        String state = w.service.exportState().toString(); Money cash = w.owner.getTotalCash(Currency.IDR);
        try { w.service.settleCoupon(w.owner,w.dealer,w.ledger,maturity); fail(); } catch(IllegalStateException expected) { }
        assertEquals(state,w.service.exportState().toString()); assertEquals(cash,w.owner.getTotalCash(Currency.IDR));
        assertEquals(10,w.owner.getPosition("BOND:FR0091").getQuantity());
        w.ledger.recordMutation("ISSUER-RESCUE",w.dealer,LedgerEntryType.INITIAL_CAPITAL,Money.of(20_000_000,Currency.IDR),true,maturity,"QA recapitalization");
        w.service.settleCoupon(w.owner,w.dealer,w.ledger,maturity);
        assertEquals(0,w.owner.getPosition("BOND:FR0091").getQuantity()); w.invariants();
    }
    @Test public void dealerCannotPayCouponThenFailTheSaleLeavingHalfTransaction() throws Exception {
        World w = new World(); w.trade("BOND","BOND:FR0091",true,10,START);
        w.trade("GOLD","COMM:GOLD",true,1,START);
        w.ledger.recordMutation("DRAIN",w.dealer,LedgerEntryType.TRADE_BUY,
                w.dealer.getAvailableCash(Currency.IDR).subtract(Money.of(1_500_000,Currency.IDR)),false,START,"QA tight liquidity");
        long later = START+90L*86_400_000L;
        JSONObject q = w.service.quote("COMM:GOLD",false,1,w.owner,w.dealer,later);
        String state = w.service.exportState().toString(); Money cash = w.owner.getTotalCash(Currency.IDR);
        try { w.service.transact("SALE","COMM:GOLD",false,1,q.getString("quoteVersion"),w.owner,w.dealer,w.ledger,later); fail(); }
        catch(IllegalStateException expected) { }
        assertEquals(state,w.service.exportState().toString()); assertEquals(cash,w.owner.getTotalCash(Currency.IDR)); w.invariants();
    }
    @Test public void pricesFollowObservedMarketsAndPersistWithStaleQuoteRejected() throws Exception {
        World w = new World(); InstrumentCatalog catalog = new InstrumentCatalog(); FxExchangeService fx = new FxExchangeService();
        Instrument stock = new Instrument("IDX:QA","QA","QA",MarketExchange.IDX,"QA",Currency.IDR,100,100000,true,Money.of(1000,Currency.IDR));
        catalog.registerInstrument(stock); w.service.updateValuations(catalog,fx,w.accounts,START);
        JSONObject quote = w.service.quote("COMM:GOLD",true,1,w.owner,w.dealer,START);
        stock.updatePrice(Money.of(1100,Currency.IDR),0); fx.setMidRate(new BigDecimal("17380"));
        w.service.updateValuations(catalog,fx,w.accounts,START+1000);
        assertTrue(w.products.getCommodity("COMM:GOLD").getSpotPricePerUnit().compareTo(Money.of(1_450_000,Currency.IDR))>0);
        try { w.service.transact("STALE","COMM:GOLD",true,1,quote.getString("quoteVersion"),w.owner,w.dealer,w.ledger,START+1000); fail(); }
        catch(IllegalStateException expected) { }
        JSONObject state = w.service.exportState(); FundBuilderService products = new FundBuilderService();
        MultiAssetTradingService loaded = new MultiAssetTradingService(products); loaded.restore(state);
        assertEquals(w.products.getCommodity("COMM:GOLD").getSpotPricePerUnit(),products.getCommodity("COMM:GOLD").getSpotPricePerUnit());
        assertEquals(state.toString(),loaded.exportState().toString()); w.invariants();
    }
    @Test public void tenSequencesOfOneHundredBuySellAccrualAndReloadActions() throws Exception {
        for(int seed=0;seed<10;seed++) {
            World w = new World(); Random random = new Random(seed);
            w.trade("BOND","BOND:FR0091",true,2,START);
            for(int action=0;action<100;action++) {
                long now=START+(action+1)*3_600_000L;
                switch(action%4) {
                    case 0: w.trade("BUY-"+action,"COMM:GOLD",true,1,now); break;
                    case 1: w.trade("SELL-"+action,"COMM:GOLD",false,1,now); break;
                    case 2: w.service.settleCoupon(w.owner,w.dealer,w.ledger,now+random.nextInt(1000)); break;
                    default:
                        MultiAssetTradingService loaded=new MultiAssetTradingService(w.products);
                        loaded.restore(w.service.exportState()); w.service=loaded;
                }
                w.invariants();
                for(Position position:w.owner.getPositions().values()) assertTrue(position.getQuantity()>=0);
            }
        }
    }
    @Test public void reviewedQuoteCanSettleLaterPersistAndExpireWithoutMutations() throws Exception {
        World w=new World(); JSONObject quote=w.service.quote("COMM:GOLD",true,1,w.owner,w.dealer,START);
        w.service.transact("LATER","COMM:GOLD",true,1,quote.getString("quoteVersion"),w.owner,w.dealer,w.ledger,START+60_000);
        JSONObject state=w.service.exportState(); MultiAssetTradingService loaded=new MultiAssetTradingService(w.products); loaded.restore(state);
        assertEquals(state.toString(),loaded.exportState().toString());
        Money before=w.owner.getTotalCash(Currency.IDR);
        try { loaded.transact("EXPIRED","COMM:GOLD",true,1,quote.getString("quoteVersion"),w.owner,w.dealer,w.ledger,START+7_200_001); fail(); }
        catch(IllegalStateException expected) { }
        assertEquals(before,w.owner.getTotalCash(Currency.IDR));
        JSONObject corrupted=new JSONObject(state.toString());
        corrupted.getJSONObject("receipts").getJSONObject("LATER").put("fee","0");
        try { loaded.restore(corrupted); fail(); } catch(IllegalArgumentException expected) { }
        assertEquals(state.toString(),loaded.exportState().toString()); w.invariants();
    }
}
