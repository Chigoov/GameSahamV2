package com.tradingglobal3d.core.business;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Pengelola Portofolio Perusahaan Operasional & Eksekutif (Empire Management).
 * Menegakkan pemisahan kas personal, transfer modal eksplisit, pembayaran payroll otomatis,
 * dan pembukuan pendapatan harian.
 */
public final class CompanyManager {
    private final Map<String, OperatingCompany> companies = new LinkedHashMap<>();

    public Collection<OperatingCompany> getAllCompanies() {
        return Collections.unmodifiableCollection(companies.values());
    }

    public OperatingCompany getCompany(String companyId) {
        return companies.get(companyId);
    }

    public synchronized void reset() {
        companies.clear();
    }

    public synchronized OperatingCompany foundCompany(
            String companyId,
            String name,
            String sector,
            Account personalAccount,
            Money initialCapital,
            EconomyLedger ledger,
            Map<String, Account> allAccounts,
            long timestamp) {

        if (companies.containsKey(companyId)) {
            throw new IllegalArgumentException("Perusahaan dengan ID " + companyId + " sudah ada");
        }
        if (initialCapital.isLessThan(Money.of(25_000_000, Currency.IDR))) {
            throw new IllegalArgumentException("Modal pendirian perusahaan operasional minimal Rp 25.000.000");
        }
        if (personalAccount.getAvailableCash(Currency.IDR).isLessThan(initialCapital)) {
            throw new IllegalStateException("Kas pribadi tidak mencukupi untuk modal pendirian perusahaan");
        }

        // 1. Buat akun perusahaan terpisah
        String accountId = "ACC-" + companyId;
        Account companyAccount = new Account(accountId, "Perusahaan: " + name, AccountType.COMPANY, null);
        allAccounts.put(accountId, companyAccount);

        // 2. Transfer modal awal dari personal ke perusahaan
        ledger.recordMutation(
                "CAP-INJECT-" + companyId + "-" + timestamp,
                personalAccount,
                LedgerEntryType.COMPANY_CAPITAL_INJECTION,
                initialCapital,
                false,
                timestamp,
                "Penyertaan modal pendirian perusahaan " + name
        );
        ledger.recordMutation(
                "CAP-RECEIVE-" + companyId + "-" + timestamp,
                companyAccount,
                LedgerEntryType.COMPANY_CAPITAL_INJECTION,
                initialCapital,
                true,
                timestamp,
                "Penerimaan modal awal perusahaan " + name
        );

        // 3. Buat entitas perusahaan dan rekrut CEO awal
        OperatingCompany company = new OperatingCompany(companyId, name, sector, accountId, 1);
        company.addEmployee(new Employee("EMP-" + companyId + "-CEO", "Direktur Utama " + name, Employee.Role.CEO, 1));
        companies.put(companyId, company);

        return company;
    }

    public synchronized void transferCapital(
            String companyId,
            boolean fromPersonalToCompany,
            Account personalAccount,
            Account companyAccount,
            Money amount,
            EconomyLedger ledger,
            long timestamp) {

        if (!amount.isPositive()) throw new IllegalArgumentException("Nominal transfer harus positif");

        if (fromPersonalToCompany) {
            if (personalAccount.getAvailableCash(amount.getCurrency()).isLessThan(amount)) {
                throw new IllegalStateException("Kas pribadi tidak mencukupi untuk transfer ke perusahaan");
            }
            ledger.recordMutation(
                    "TRF-TO-COMP-" + System.nanoTime(),
                    personalAccount,
                    LedgerEntryType.COMPANY_CAPITAL_INJECTION,
                    amount,
                    false,
                    timestamp,
                    "Injeksi modal ke perusahaan " + companyId
            );
            ledger.recordMutation(
                    "TRF-REC-COMP-" + System.nanoTime(),
                    companyAccount,
                    LedgerEntryType.COMPANY_CAPITAL_INJECTION,
                    amount,
                    true,
                    timestamp,
                    "Penerimaan injeksi modal perusahaan " + companyId
            );
        } else {
            if (companyAccount.getAvailableCash(amount.getCurrency()).isLessThan(amount)) {
                throw new IllegalStateException("Kas perusahaan tidak mencukupi untuk penarikan dividen modal");
            }
            ledger.recordMutation(
                    "DIV-FROM-COMP-" + System.nanoTime(),
                    companyAccount,
                    LedgerEntryType.COMPANY_CAPITAL_WITHDRAWAL,
                    amount,
                    false,
                    timestamp,
                    "Penarikan hasil usaha perusahaan " + companyId
            );
            ledger.recordMutation(
                    "DIV-REC-PERS-" + System.nanoTime(),
                    personalAccount,
                    LedgerEntryType.COMPANY_CAPITAL_WITHDRAWAL,
                    amount,
                    true,
                    timestamp,
                    "Penerimaan dividen usaha dari " + companyId
            );
        }
    }

    public synchronized void processDailyOperations(
            Account companyAccount,
            OperatingCompany company,
            EconomyLedger ledger,
            long timestamp) {

        Money payroll = company.calculateDailyPayroll();
        Money revenue = company.calculateDailyRevenue();

        // 1. Bayar payroll jika kas cukup
        if (companyAccount.getAvailableCash(Currency.IDR).isGreaterThanOrEqual(payroll)) {
            ledger.recordMutation(
                    "PAYROLL-" + company.getId() + "-" + timestamp,
                    companyAccount,
                    LedgerEntryType.COMPANY_PAYROLL,
                    payroll,
                    false,
                    timestamp,
                    "Pembayaran payroll harian pegawai " + company.getName()
            );
        }

        // 2. Kreditkan pendapatan usaha
        if (revenue.isPositive()) {
            ledger.recordMutation(
                    "REV-" + company.getId() + "-" + timestamp,
                    companyAccount,
                    LedgerEntryType.COMPANY_REVENUE,
                    revenue,
                    true,
                    timestamp,
                    "Pendapatan operasional harian " + company.getName()
            );
        }
    }

    public JSONArray exportState() throws JSONException {
        JSONArray arr = new JSONArray();
        for (OperatingCompany c : companies.values()) {
            arr.put(c.toJsonObject());
        }
        return arr;
    }

    public void restoreState(JSONArray arr) throws JSONException {
        companies.clear();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                OperatingCompany c = OperatingCompany.fromJsonObject(arr.getJSONObject(i));
                companies.put(c.getId(), c);
            }
        }
    }
}
