package com.tradingglobal3d.core.replay;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Lab Strategi & Simulasi What-If Terisolasi (FR-17.03, FR-17.04).
 * Salinan keadaan terisolasi: eksperimen di sini TIDAK AKAN MENGUBAH akun permainan utama.
 */
public final class StrategyLabSandbox {
    private final String sandboxId;
    private final Account clonedAccount;
    private final EconomyLedger sandboxLedger;
    private int simulatedTradesCount = 0;
    private BigDecimal simulatedRealizedPnL = BigDecimal.ZERO;

    public StrategyLabSandbox(String sandboxId, Account sourceAccount) {
        this.sandboxId = Objects.requireNonNull(sandboxId);
        Objects.requireNonNull(sourceAccount);

        // Kloning akun ke sandbox terpisah (Deep Copy)
        this.clonedAccount = new Account("SANDBOX-" + sourceAccount.getId(), "Lab: " + sourceAccount.getName(), AccountType.PERSONAL, null);
        this.clonedAccount.creditCash(sourceAccount.getTotalCash(Currency.IDR));
        this.clonedAccount.creditCash(sourceAccount.getTotalCash(Currency.USD));

        for (Position p : sourceAccount.getPositions().values()) {
            Position cloneP = new Position(p.getTicker(), p.getExchange(), p.getCurrency(), p.getQuantity(), p.getAverageCost(), p.getCurrentPrice(),
                    p.getRealizedPnL() == null ? null : p.getRealizedPnL().getAmount());

            this.clonedAccount.addPosition(cloneP);
        }

        this.sandboxLedger = new EconomyLedger();
    }

    public String getSandboxId() { return sandboxId; }
    public Account getClonedAccount() { return clonedAccount; }
    public int getSimulatedTradesCount() { return simulatedTradesCount; }
    public BigDecimal getSimulatedRealizedPnL() { return simulatedRealizedPnL; }

    /**
     * Menjalankan simulasi transaksi beli di lab tanpa menyentuh saldo akun asli.
     */
    public synchronized boolean simulateBuy(Instrument instrument, long shares, BigDecimal price) {
        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(shares));
        BigDecimal fee = totalCost.multiply(new BigDecimal("0.0015"));
        Money required = new Money(totalCost.add(fee), instrument.getCurrency());

        if (clonedAccount.getAvailableCash(instrument.getCurrency()).isLessThan(required)) {
            return false;
        }

        sandboxLedger.recordMutation(
                "LAB-BUY-" + System.nanoTime(),
                clonedAccount,
                LedgerEntryType.TRADE_BUY,
                required,
                false,
                System.currentTimeMillis(),
                "Lab simulasi beli " + instrument.getTicker()
        );

        Position p = clonedAccount.getPosition(instrument.getId());
        if (p == null) {
            p = new Position(instrument.getTicker(), instrument.getExchange().name(), instrument.getCurrency(), shares, price, price);
            clonedAccount.addPosition(p);
        } else {
            p.addBuy(shares, price);
        }

        simulatedTradesCount++;
        return true;
    }

    /**
     * Menjalankan simulasi transaksi jual di lab tanpa menyentuh saldo akun asli.
     */
    public synchronized boolean simulateSell(Instrument instrument, long shares, BigDecimal price) {
        Position p = clonedAccount.getPosition(instrument.getId());
        if (p == null || p.getQuantity() < shares) {
            return false;
        }

        Money realized = p.addSell(shares, price);
        BigDecimal gross = price.multiply(BigDecimal.valueOf(shares));
        BigDecimal fee = gross.multiply(new BigDecimal("0.0025"));
        Money netProceeds = new Money(gross.subtract(fee), instrument.getCurrency());

        sandboxLedger.recordMutation(
                "LAB-SELL-" + System.nanoTime(),
                clonedAccount,
                LedgerEntryType.TRADE_SELL,
                netProceeds,
                true,
                System.currentTimeMillis(),
                "Lab simulasi jual " + instrument.getTicker()
        );

        simulatedRealizedPnL = simulatedRealizedPnL.add(realized.getAmount()).subtract(fee);
        simulatedTradesCount++;
        return true;
    }

    /**
     * Menghitung total nilai aset sandbox pada harga pasar tertentu.
     */
    public synchronized Money calculateSandboxTotalEquity(Map<String, BigDecimal> marketPrices, BigDecimal usdRate) {
        BigDecimal totalIdr = clonedAccount.getTotalCash(Currency.IDR).getAmount();
        BigDecimal usdVal = clonedAccount.getTotalCash(Currency.USD).getAmount().multiply(usdRate);
        totalIdr = totalIdr.add(usdVal);

        for (Position p : clonedAccount.getPositions().values()) {
            if (p.getQuantity() > 0) {
                BigDecimal curPx = marketPrices.getOrDefault(p.getTicker(), p.getCurrentPrice());
                BigDecimal posVal = curPx.multiply(BigDecimal.valueOf(p.getQuantity()));
                if (p.getCurrency() == Currency.USD) posVal = posVal.multiply(usdRate);
                totalIdr = totalIdr.add(posVal);
            }
        }

        return new Money(totalIdr.setScale(0, RoundingMode.HALF_UP), Currency.IDR);
    }
}
