package com.tradingglobal3d.core;

import com.tradingglobal3d.core.corporate.*;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveState;
import org.json.JSONObject;
import org.junit.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import static org.junit.Assert.*;

public class PositionAcquisitionFeeRegressionTest {
    private static final LocalDate DATE = LocalDate.of(2026, 1, 1);
    private Position position(long quantity, long fee) {
        return new Position("BBCA", "IDX", Currency.IDR, quantity, BigDecimal.valueOf(9000), BigDecimal.valueOf(10000),
                BigDecimal.valueOf(25), Money.of(fee, Currency.IDR));
    }
    private Account account(Position position) {
        Account account = new Account("QA-FEE", "Fee QA", AccountType.PERSONAL, null);
        account.addPosition(position);
        return account;
    }
    private Instrument instrument() {
        return new Instrument("IDX:BBCA", "BBCA", "BBCA", MarketExchange.IDX, "Bank", Currency.IDR,
                100, 1000000, true, Money.of(10000, Currency.IDR));
    }
    private CorporateAction split(String ratio) {
        return new CorporateAction("QA-SPLIT", "BBCA", CorporateActionType.STOCK_SPLIT, new BigDecimal(ratio), Currency.IDR, DATE, DATE);
    }
    private Position reload(Account account) throws Exception {
        SaveState saved = new SaveState(2, GameMode.INTERNAL_MARKET, 1, DATE, LocalTime.of(10, 0), 1,
                Collections.singletonList(account), Collections.emptyList());
        return SaveState.fromJsonString(saved.toJsonString()).getAccounts().get(0).getPosition("IDX:BBCA");
    }
    @Test public void partialSalesAllocateActualFeeAndLastSaleConsumesRoundingRemainder() {
        Position position = position(3, 10);
        assertEquals(3, position.previewAcquisitionFeeAllocation(1).getAmount().longValueExact());
        position.addSell(1, BigDecimal.valueOf(10000));
        assertEquals(7, position.getAcquisitionFeesRemaining().getAmount().longValueExact());
        assertEquals(7, position.previewAcquisitionFeeAllocation(2).getAmount().longValueExact());
        position.addSell(2, BigDecimal.valueOf(10000));
        assertTrue(position.getAcquisitionFeesRemaining().isZero());
        assertEquals(3025, position.getRealizedPnL().getAmount().longValueExact());
        Position legacy = position(100, 0);
        assertTrue(legacy.previewAcquisitionFeeAllocation(100).isZero());
    }
    @Test public void forwardSplitPreservesFeeAndGrossRealizedThroughSaveReload() throws Exception {
        for (boolean ledgerOverload : new boolean[]{false, true}) {
            Account account = account(position(100, 1500));
            CorporateActionService service = new CorporateActionService();
            if (ledgerOverload) service.executeStockSplit(split("2"), instrument(), Collections.singletonList(account), new EconomyLedger(), 1);
            else service.executeStockSplit(split("2"), instrument(), Collections.singletonList(account));
            Position restored = reload(account);
            assertEquals(200, restored.getQuantity());
            assertEquals(200, restored.getAcquisitionFeeQuantity());
            assertEquals(1500, restored.getAcquisitionFeesRemaining().getAmount().longValueExact());
            assertEquals(750, restored.previewAcquisitionFeeAllocation(100).getAmount().longValueExact());
            assertEquals(25, restored.getRealizedPnL().getAmount().longValueExact());
        }
    }
    @Test public void reverseSplitAllocatesFractionalFeeAndRetryDoesNotResetBasis() throws Exception {
        Account account = account(position(101, 101)); CorporateActionService service = new CorporateActionService();
        CorporateAction split = split("0.5"); Instrument instrument = instrument(); EconomyLedger ledger = new EconomyLedger();
        service.executeStockSplit(split, instrument, Collections.singletonList(account), ledger, 1);
        Position restored = reload(account);
        assertEquals(50, restored.getQuantity());
        assertEquals(100, restored.getAcquisitionFeesRemaining().getAmount().longValueExact());
        service.executeStockSplit(split, instrument, Collections.singletonList(account), ledger, 1);
        assertEquals(100, account.getPosition("IDX:BBCA").getAcquisitionFeesRemaining().getAmount().longValueExact());
        assertEquals(10000, account.getTotalCash(Currency.IDR).getAmount().longValueExact());
    }
    @Test public void lentInventoryPreservesFeeBasisAndLegacyReturnsRemainSaveable() throws Exception {
        Position position = position(100, 1500); position.borrowShares(40);
        Position restored = reload(account(position));
        assertEquals(100, restored.getAcquisitionFeeQuantity());
        assertEquals(900, restored.previewAcquisitionFeeAllocation(60).getAmount().longValueExact());
        restored.addSell(60, BigDecimal.valueOf(10000));
        restored.returnShares(40);
        assertEquals(600, restored.previewAcquisitionFeeAllocation(40).getAmount().longValueExact());
        Position legacy = position(60, 0); legacy.returnShares(40);
        assertEquals(100, reload(account(legacy)).getAcquisitionFeeQuantity());
        assertTrue(legacy.getAcquisitionFeesRemaining().isZero());
        Position max = position(Long.MAX_VALUE, 0);
        assertThrows(ArithmeticException.class, () -> max.returnShares(1));
        assertEquals(Long.MAX_VALUE, max.getQuantity());
    }
    @Test public void legacySaveDoesNotInventFeesAndInvalidFeeBasisIsRejected() throws Exception {
        SaveState saved = new SaveState(2, GameMode.INTERNAL_MARKET, 1, DATE, LocalTime.NOON, 1,
                Collections.singletonList(account(position(100, 0))), Collections.emptyList());
        JSONObject json = new JSONObject(saved.toJsonString());
        JSONObject position = json.getJSONArray("accounts").getJSONObject(0).getJSONArray("positions").getJSONObject(0);
        position.remove("acquisitionFeesRemaining"); position.remove("acquisitionFeeQuantity");
        assertTrue(SaveState.fromJsonString(json.toString()).getAccounts().get(0).getPosition("IDX:BBCA").getAcquisitionFeesRemaining().isZero());
        position.put("acquisitionFeesRemaining", "100").put("acquisitionFeeQuantity", 99);
        assertThrows(IllegalArgumentException.class, () -> SaveState.fromJsonString(json.toString()));
    }
}
