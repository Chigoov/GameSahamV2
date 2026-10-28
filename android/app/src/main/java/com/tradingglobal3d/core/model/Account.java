package com.tradingglobal3d.core.model;

import java.math.BigDecimal;
import java.util.*;

/**
 * Akun keuangan entitas (Pemain Pribadi, Akun Klien, atau Firma).
 * Mengelola saldo kas multi-mata uang, kas tertahan (reserved), dan posisi aset.
 */
public final class Account {
    private final String id;
    private final String name;
    private final AccountType type;
    private final String clientMandateId; // Null untuk akun pribadi/firma

    private final Map<Currency, BigDecimal> totalCash = new EnumMap<>(Currency.class);
    private final Map<Currency, BigDecimal> reservedCash = new EnumMap<>(Currency.class);
    private final Map<String, Position> positions = new HashMap<>();

    public Account(String id, String name, AccountType type, String clientMandateId) {
        this.id = Objects.requireNonNull(id, "Account ID cannot be null");
        this.name = Objects.requireNonNull(name, "Account name cannot be null");
        this.type = Objects.requireNonNull(type, "Account type cannot be null");
        this.clientMandateId = clientMandateId;

        for (Currency c : Currency.values()) {
            totalCash.put(c, BigDecimal.ZERO);
            reservedCash.put(c, BigDecimal.ZERO);
        }
    }

    public Account copy() {
        Account copy = new Account(this.id, this.name, this.type, this.clientMandateId);
        for (Currency c : Currency.values()) {
            copy.totalCash.put(c, this.totalCash.getOrDefault(c, BigDecimal.ZERO));
            copy.reservedCash.put(c, this.reservedCash.getOrDefault(c, BigDecimal.ZERO));
        }
        for (Map.Entry<String, Position> entry : this.positions.entrySet()) {
            copy.positions.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public AccountType getType() { return type; }
    public String getClientMandateId() { return clientMandateId; }

    public Money getTotalCash(Currency currency) {
        return new Money(totalCash.getOrDefault(currency, BigDecimal.ZERO), currency);
    }

    public Money getReservedCash(Currency currency) {
        return new Money(reservedCash.getOrDefault(currency, BigDecimal.ZERO), currency);
    }

    public Money getAvailableCash(Currency currency) {
        BigDecimal total = totalCash.getOrDefault(currency, BigDecimal.ZERO);
        BigDecimal reserved = reservedCash.getOrDefault(currency, BigDecimal.ZERO);
        return new Money(total.subtract(reserved), currency);
    }

    public void creditCash(Money money) {
        if (money.isNegative()) throw new IllegalArgumentException("Nominal mutasi kas tidak boleh negatif");
        Currency c = money.getCurrency();
        BigDecimal current = totalCash.getOrDefault(c, BigDecimal.ZERO);
        totalCash.put(c, current.add(money.getAmount()));
    }

    public void debitCash(Money money) {
        if (money.isNegative()) throw new IllegalArgumentException("Nominal mutasi kas tidak boleh negatif");
        Currency c = money.getCurrency();
        Money available = getAvailableCash(c);
        if (available.isLessThan(money)) {
            throw new IllegalStateException("Saldo kas " + c + " tidak mencukupi: tersedia=" + available + ", diminta=" + money);
        }
        BigDecimal current = totalCash.getOrDefault(c, BigDecimal.ZERO);
        totalCash.put(c, current.subtract(money.getAmount()));
    }

    public void reserveCash(Money money) {
        if (money.isNegative()) throw new IllegalArgumentException("Nominal mutasi kas tidak boleh negatif");
        Currency c = money.getCurrency();
        Money available = getAvailableCash(c);
        if (available.isLessThan(money)) {
            throw new IllegalStateException("Saldo kas " + c + " tidak cukup untuk di-reserve: tersedia=" + available + ", diminta=" + money);
        }
        BigDecimal current = reservedCash.getOrDefault(c, BigDecimal.ZERO);
        reservedCash.put(c, current.add(money.getAmount()));
    }

    public void releaseReservedCash(Money money) {
        if (money.isNegative()) throw new IllegalArgumentException("Nominal mutasi kas tidak boleh negatif");
        Currency c = money.getCurrency();
        BigDecimal current = reservedCash.getOrDefault(c, BigDecimal.ZERO);
        if (current.compareTo(money.getAmount()) < 0) {
            throw new IllegalStateException("Kas tertahan yang di-release melebihi saldo reserved saat ini");
        }
        reservedCash.put(c, current.subtract(money.getAmount()));
    }

    public Map<String, Position> getPositions() {
        return Collections.unmodifiableMap(positions);
    }

    public Position getPosition(String instrumentIdOrTicker) {
        Position exact = positions.get(instrumentIdOrTicker);
        if (exact != null || instrumentIdOrTicker.indexOf(':') >= 0) return exact;
        Position found = null;
        for (Position position : positions.values()) {
            if (!position.getTicker().equals(instrumentIdOrTicker)) continue;
            if (found != null) throw new IllegalArgumentException("Ticker ambigu; gunakan identitas bursa: " + instrumentIdOrTicker);
            found = position;
        }
        return found;
    }

    public void addPosition(Position position) {
        positions.put(position.getExchange() + ":" + position.getTicker(), position);
    }

    public void removePosition(String instrumentIdOrTicker) {
        Position position = getPosition(instrumentIdOrTicker);
        if (position != null) positions.remove(position.getExchange() + ":" + position.getTicker());
    }

    public Money getTotalPortfolioValue(Currency valuationCurrency, BigDecimal fxRateUsdIdr) {
        if (fxRateUsdIdr == null || fxRateUsdIdr.signum() <= 0)
            throw new IllegalArgumentException("Kurs valuasi harus positif");
        Money total = getTotalCash(valuationCurrency);
        if (valuationCurrency == Currency.IDR) {
            total = total.add(new Money(getTotalCash(Currency.USD).getAmount().multiply(fxRateUsdIdr), Currency.IDR));
        } else {
            total = total.add(new Money(getTotalCash(Currency.IDR).getAmount()
                    .divide(fxRateUsdIdr, 2, java.math.RoundingMode.HALF_UP), Currency.USD));
        }
        for (Position p : positions.values()) {
            Money val = p.getMarketValue();
            if (val.getCurrency() == valuationCurrency) {
                total = total.add(val);
            } else if (valuationCurrency == Currency.IDR && val.getCurrency() == Currency.USD) {
                // Konversi USD ke IDR untuk valuasi
                BigDecimal idrAmount = val.getAmount().multiply(fxRateUsdIdr);
                total = total.add(new Money(idrAmount, Currency.IDR));
            } else if (valuationCurrency == Currency.USD && val.getCurrency() == Currency.IDR) {
                // Konversi IDR ke USD untuk valuasi
                if (fxRateUsdIdr.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal usdAmount = val.getAmount().divide(fxRateUsdIdr, 2, java.math.RoundingMode.HALF_UP);
                    total = total.add(new Money(usdAmount, Currency.USD));
                }
            }
        }
        return total;
    }
}
