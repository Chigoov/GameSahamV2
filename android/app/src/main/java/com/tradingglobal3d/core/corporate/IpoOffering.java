package com.tradingglobal3d.core.corporate;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Penawaran Saham Perdana (Initial Public Offering - IPO).
 */
public final class IpoOffering {
    private final String id;
    private final String ticker;
    private final String companyName;
    private final Currency currency;
    private final Money offeringPrice;
    private final int lotSize;
    private final long sharesOffered;
    private final LocalDate offeringStartDate;
    private final LocalDate offeringEndDate;
    private final LocalDate allotmentDate;
    private final LocalDate listingDate;

    private boolean isAllotted;

    public IpoOffering(String id, String ticker, String companyName, Currency currency,
                       Money offeringPrice, int lotSize, long sharesOffered, LocalDate listingDate) {
        // Durasi penawaran 3 hari game: Hari -3 s.d. -1, Penjatahan: Hari -1, Pencatatan Perdana: Hari Listing (Bug #388)
        this(id, ticker, companyName, currency, offeringPrice, lotSize, sharesOffered,
                listingDate.minusDays(3), listingDate.minusDays(1), listingDate.minusDays(1), listingDate);
    }

    public IpoOffering(String id, String ticker, String companyName, Currency currency,
                       Money offeringPrice, int lotSize, long sharesOffered,
                       LocalDate offeringStartDate, LocalDate offeringEndDate,
                       LocalDate allotmentDate, LocalDate listingDate) {
        this.id = Objects.requireNonNull(id);
        this.ticker = Objects.requireNonNull(ticker);
        this.companyName = Objects.requireNonNull(companyName);
        this.currency = Objects.requireNonNull(currency);
        this.offeringPrice = Objects.requireNonNull(offeringPrice);
        this.lotSize = lotSize;
        this.sharesOffered = sharesOffered;
        this.offeringStartDate = Objects.requireNonNull(offeringStartDate);
        this.offeringEndDate = Objects.requireNonNull(offeringEndDate);
        this.allotmentDate = Objects.requireNonNull(allotmentDate);
        this.listingDate = Objects.requireNonNull(listingDate);
        if (lotSize <= 0 || sharesOffered <= 0 || sharesOffered % lotSize != 0 || !offeringPrice.isPositive() || offeringPrice.getCurrency() != currency)
            throw new IllegalArgumentException("Harga, mata uang, jumlah saham dan lot IPO tidak valid");
        this.isAllotted = false;
    }

    public String getId() { return id; }
    public String getTicker() { return ticker; }
    public String getCompanyName() { return companyName; }
    public Currency getCurrency() { return currency; }
    public Money getOfferingPrice() { return offeringPrice; }
    public int getLotSize() { return lotSize; }
    public long getSharesOffered() { return sharesOffered; }
    public LocalDate getOfferingStartDate() { return offeringStartDate; }
    public LocalDate getOfferingEndDate() { return offeringEndDate; }
    public LocalDate getAllotmentDate() { return allotmentDate; }
    public LocalDate getListingDate() { return listingDate; }
    public boolean isAllotted() { return isAllotted; }

    public boolean isOfferingPeriod(LocalDate date) {
        if (date == null) return false;
        return !date.isBefore(offeringStartDate) && !date.isAfter(offeringEndDate);
    }

    public void markAllotted() {
        this.isAllotted = true;
    }
}
