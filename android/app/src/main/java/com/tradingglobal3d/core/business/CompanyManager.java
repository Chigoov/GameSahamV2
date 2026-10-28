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
    private static final Set<String> HOLDING_SECTORS = new LinkedHashSet<>(Arrays.asList("FINANCIAL", "ENERGY", "CONSUMER", "TECHNOLOGY"));
    private static final class HoldingGroup {
        final String id, name;
        final Map<String, String> subsidiaries;
        HoldingGroup(String id, String name, Map<String, String> subsidiaries) {
            this.id = id; this.name = name; this.subsidiaries = new LinkedHashMap<>(subsidiaries);
        }
    }
    private final Map<String, HoldingGroup> holdings = new LinkedHashMap<>();
    private final Map<String, String> holdingByCompany = new HashMap<>();

    public Collection<OperatingCompany> getAllCompanies() {
        return Collections.unmodifiableCollection(companies.values());
    }

    public OperatingCompany getCompany(String companyId) {
        return companies.get(companyId);
    }

    public synchronized int applySupplyChainCrisis() {
        int affected = 0;
        for (OperatingCompany company : companies.values()) {
            String sector = company.getSectorModel();
            if (sector.equals("MANUFAKTUR") || sector.equals("RITEL") || sector.equals("ENERGY")) {
                company.applySupplyChainCrisis();
                affected++;
            }
        }
        return affected;
    }

    public synchronized void reset() {
        companies.clear();
        holdings.clear();
        holdingByCompany.clear();
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

        if (companyId == null || companyId.trim().isEmpty() || name == null || name.trim().isEmpty()
                || sector == null || sector.trim().isEmpty()) throw new IllegalArgumentException("Identitas perusahaan wajib diisi");
        if (initialCapital.getCurrency() != Currency.IDR) throw new IllegalArgumentException("Modal perusahaan wajib IDR");
        if (allAccounts.containsKey("ACC-" + companyId)) throw new IllegalArgumentException("Akun perusahaan sudah ada");
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

        OperatingCompany company = companies.get(companyId);
        if (company == null || !company.getAccountId().equals(companyAccount.getId())
                || companyAccount.getType() != AccountType.COMPANY || personalAccount.getType() != AccountType.PERSONAL)
            throw new IllegalArgumentException("Akun transfer tidak sesuai perusahaan");
        if (amount.getCurrency() != Currency.IDR || !amount.isPositive()) throw new IllegalArgumentException("Nominal transfer wajib IDR positif");

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
            settlePayrollArrears(companyAccount, company, ledger, timestamp);
        } else {
            if (companyAccount.getAvailableCash(Currency.IDR).subtract(amount).isLessThan(company.getPayrollArrears()))
                throw new IllegalStateException("Kas perusahaan terkunci untuk tunggakan gaji");
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

    public synchronized JSONObject createHoldingCompany(String holdingId, String holdingName, JSONArray subsidiaries)
            throws JSONException {
        if (holdingId == null || holdingId.trim().isEmpty() || holdingName == null || holdingName.trim().isEmpty())
            throw new IllegalArgumentException("Identitas holding wajib diisi");
        if (holdings.containsKey(holdingId)) throw new IllegalArgumentException("Holding sudah ada");
        if (subsidiaries == null || subsidiaries.length() != HOLDING_SECTORS.size())
            throw new IllegalArgumentException("Holding wajib membawahi empat anak usaha dari sektor yang berbeda");
        Map<String, String> staged = new LinkedHashMap<>();
        Set<String> seenCompanies = new HashSet<>();
        for (int i = 0; i < subsidiaries.length(); i++) {
            JSONObject entry = subsidiaries.getJSONObject(i);
            String sector = entry.getString("sector").trim().toUpperCase(Locale.ROOT);
            String companyId = entry.getString("companyId").trim();
            if (!HOLDING_SECTORS.contains(sector) || staged.containsKey(sector))
                throw new IllegalArgumentException("Sektor holding tidak valid atau berulang: " + sector);
            if (!companies.containsKey(companyId) || !seenCompanies.add(companyId) || holdingByCompany.containsKey(companyId))
                throw new IllegalArgumentException("Anak usaha holding tidak dikenal, duplikat, atau sudah berada dalam holding");
            staged.put(sector, companyId);
        }
        HoldingGroup group = new HoldingGroup(holdingId, holdingName.trim(), staged);
        holdings.put(holdingId, group);
        for (String companyId : staged.values()) holdingByCompany.put(companyId, holdingId);
        return toHoldingJson(group);
    }

    public synchronized JSONObject transferHoldingCapital(String holdingId, String fromCompanyId, String toCompanyId,
            Money amount, String transferType, Map<String, Account> allAccounts, EconomyLedger ledger,
            long timestamp, String requestId) throws JSONException {
        HoldingGroup group = holdings.get(holdingId);
        if (group == null) throw new IllegalArgumentException("Holding tidak ditemukan");
        if (fromCompanyId == null || toCompanyId == null || fromCompanyId.equals(toCompanyId)
                || !group.subsidiaries.containsValue(fromCompanyId) || !group.subsidiaries.containsValue(toCompanyId))
            throw new IllegalArgumentException("Kedua perusahaan harus anak usaha berbeda dalam holding yang sama");
        if (!"CAPITAL_INJECTION".equals(transferType) && !"DIVIDEND".equals(transferType))
            throw new IllegalArgumentException("Jenis transfer holding tidak dikenal");
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 120)
            throw new IllegalArgumentException("ID transfer holding tidak valid");
        if (amount == null || amount.getCurrency() != Currency.IDR || !amount.isPositive())
            throw new IllegalArgumentException("Transfer holding wajib IDR positif");
        String debitId = "HOLDING-" + requestId + "-DEBIT";
        String creditId = "HOLDING-" + requestId + "-CREDIT";
        boolean debitDone = ledger.isRequestProcessed(debitId);
        boolean creditDone = ledger.isRequestProcessed(creditId);
        if (debitDone || creditDone) {
            if (debitDone && creditDone) return new JSONObject().put("success", true).put("alreadyProcessed", true)
                    .put("holdingId", holdingId).put("amount", amount.format()).put("transferType", transferType);
            throw new IllegalStateException("Receipt transfer holding tidak lengkap; perlu rekonsiliasi sebelum retry");
        }
        OperatingCompany from = companies.get(fromCompanyId), to = companies.get(toCompanyId);
        Account fromAccount = allAccounts.get(from.getAccountId()), toAccount = allAccounts.get(to.getAccountId());
        if (fromAccount == null || toAccount == null) throw new IllegalStateException("Akun anak usaha holding tidak tersedia");
        Money reserve = from.getPayrollArrears().add(from.calculateDailyPayroll()).add(from.calculateDailyOperatingCost());
        if (fromAccount.getAvailableCash(Currency.IDR).subtract(amount).isLessThan(reserve))
            throw new IllegalStateException("Transfer akan mengurangi cadangan payroll atau biaya operasi satu hari");
        LedgerEntryType type = "DIVIDEND".equals(transferType)
                ? LedgerEntryType.COMPANY_CAPITAL_WITHDRAWAL : LedgerEntryType.COMPANY_CAPITAL_INJECTION;
        String movement = "DIVIDEND".equals(transferType) ? "dividen" : "injeksi modal";
        ledger.recordMutation(debitId, fromAccount, type, amount, false, timestamp,
                movement + " internal dari " + from.getName() + " ke " + to.getName() + " tanpa pajak bursa");
        ledger.recordMutation(creditId, toAccount, type, amount, true, timestamp,
                "Penerimaan " + movement + " internal ke " + to.getName() + " dari " + from.getName() + " tanpa pajak bursa");
        return new JSONObject().put("success", true).put("alreadyProcessed", false).put("holdingId", holdingId)
                .put("amount", amount.format()).put("transferType", transferType)
                .put("fromCompanyId", fromCompanyId).put("toCompanyId", toCompanyId);
    }

    public synchronized JSONArray getHoldingCompaniesJson() throws JSONException {
        JSONArray result = new JSONArray();
        for (HoldingGroup group : holdings.values()) result.put(toHoldingJson(group));
        return result;
    }

    public synchronized JSONArray exportHoldingState() throws JSONException {
        JSONArray result = new JSONArray();
        for (HoldingGroup group : holdings.values()) result.put(toHoldingJson(group));
        return result;
    }

    public synchronized void restoreHoldingState(JSONArray saved) throws JSONException {
        Map<String, HoldingGroup> stagedHoldings = new LinkedHashMap<>();
        Map<String, String> stagedMembership = new HashMap<>();
        if (saved != null) for (int i = 0; i < saved.length(); i++) {
            JSONObject item = saved.getJSONObject(i);
            String id = item.getString("holdingId"), name = item.getString("holdingName");
            JSONArray subs = item.getJSONArray("subsidiaries");
            if (id.trim().isEmpty() || stagedHoldings.containsKey(id) || subs.length() != HOLDING_SECTORS.size())
                throw new IllegalArgumentException("State holding tidak valid atau duplikat");
            Map<String, String> members = new LinkedHashMap<>();
            for (int j = 0; j < subs.length(); j++) {
                JSONObject sub = subs.getJSONObject(j);
                String sector = sub.getString("sector"), companyId = sub.getString("companyId");
                if (!HOLDING_SECTORS.contains(sector) || !companies.containsKey(companyId)
                        || members.put(sector, companyId) != null || stagedMembership.put(companyId, id) != null)
                    throw new IllegalArgumentException("Keanggotaan anak usaha holding save tidak valid");
            }
            if (members.size() != HOLDING_SECTORS.size()) throw new IllegalArgumentException("Holding tidak terdiversifikasi");
            stagedHoldings.put(id, new HoldingGroup(id, name, members));
        }
        holdings.clear(); holdings.putAll(stagedHoldings);
        holdingByCompany.clear(); holdingByCompany.putAll(stagedMembership);
    }

    private JSONObject toHoldingJson(HoldingGroup group) throws JSONException {
        JSONArray subsidiaries = new JSONArray();
        for (Map.Entry<String, String> entry : group.subsidiaries.entrySet()) {
            OperatingCompany company = companies.get(entry.getValue());
            subsidiaries.put(new JSONObject().put("sector", entry.getKey()).put("companyId", company.getId())
                    .put("companyName", company.getName()).put("accountId", company.getAccountId()));
        }
        return new JSONObject().put("holdingId", group.id).put("holdingName", group.name).put("subsidiaries", subsidiaries);
    }

    public synchronized void processDailyOperations(
            Account companyAccount,
            OperatingCompany company,
            EconomyLedger ledger,
            long timestamp) {

        if (companies.get(company.getId()) != company || !company.getAccountId().equals(companyAccount.getId()))
            throw new IllegalArgumentException("Perusahaan/akun operasional tidak sesuai");
        long day = java.time.Instant.ofEpochMilli(timestamp).atOffset(java.time.ZoneOffset.ofHours(7)).toLocalDate().toEpochDay();
        if (day <= company.getLastOperationsDay()) return;

        Money payroll = company.calculateDailyPayroll();
        company.setPayrollArrears(company.getPayrollArrears().add(payroll));
        Money payrollPaid = settlePayrollArrears(companyAccount, company, ledger, timestamp);
        Money requiredCost = company.calculateDailyOperatingCost();
        Money paidCost = minimum(companyAccount.getAvailableCash(Currency.IDR), requiredCost);
        if (paidCost.isPositive()) ledger.recordMutation("COMP-OPS-" + company.getId() + "-" + day,
                companyAccount, LedgerEntryType.TRANSACTION_FEE, paidCost, false, timestamp,
                "Biaya operasional perusahaan " + company.getName());
        Money revenue = company.calculateDailyRevenue(day);
        if (company.getPayrollArrears().isPositive()) revenue = revenue.multiply(0.5);
        // Unfunded operations stop; partial funding only produces its paid fraction, without hidden free costs.
        revenue = revenue.multiply(paidCost.getAmount().divide(requiredCost.getAmount(), 12, java.math.RoundingMode.DOWN));
        if (revenue.isPositive()) ledger.recordMutation("REV-" + company.getId() + "-" + day,
                companyAccount, LedgerEntryType.COMPANY_REVENUE, revenue, true, timestamp,
                "Pendapatan operasional harian " + company.getName());
        company.completeOperations(day, revenue, paidCost, payrollPaid);
    }

    private static Money minimum(Money first, Money second) { return first.isLessThan(second) ? first : second; }

    private Money settlePayrollArrears(Account account, OperatingCompany company, EconomyLedger ledger, long timestamp) {
        Money payment = minimum(account.getAvailableCash(Currency.IDR), company.getPayrollArrears());
        if (payment.isPositive()) {
            ledger.recordMutation("PAYROLL-" + company.getId() + "-" + timestamp + "-" + ledger.getEntries().size(),
                    account, LedgerEntryType.COMPANY_PAYROLL, payment, false, timestamp,
                    "Pembayaran gaji dan tunggakan " + company.getName());
            company.setPayrollArrears(company.getPayrollArrears().subtract(payment));
        }
        return payment;
    }

    private OperatingCompany requireCompany(String companyId) {
        OperatingCompany company = companies.get(companyId);
        if (company == null) throw new IllegalArgumentException("Perusahaan tidak ditemukan");
        return company;
    }

    private List<Employee.Role> staffRoles(OperatingCompany company) {
        List<Employee.Role> roles = new ArrayList<>(Arrays.asList(Employee.Role.CEO,
                Employee.Role.OPERATIONS_MANAGER, Employee.Role.SALES_MANAGER, Employee.Role.HR_DIRECTOR));
        if (company.getSectorModel().equals("MANUFAKTUR")) roles.add(Employee.Role.PRODUCTION_MANAGER);
        return roles;
    }

    private Employee candidate(OperatingCompany company, Employee.Role role) {
        if (!staffRoles(company).contains(role)) throw new IllegalArgumentException("Jabatan tidak sesuai sektor perusahaan");
        // ponytail: a fixed candidate per role keeps recruitment deterministic without a second roster system.
        String base = "CAND-" + company.getId() + "-" + role.name();
        String employeeId = base;
        for (int suffix = 2; company.getEmployee(employeeId) != null; suffix++) employeeId = base + "-" + suffix;
        return new Employee(employeeId, "Kandidat " + role.getLabel(), role, 1);
    }

    private String department(Employee.Role role) {
        switch (role) {
            case CEO: return "Direksi";
            case HR_DIRECTOR: return "SDM";
            case SALES_MANAGER: return "Penjualan";
            case PRODUCTION_MANAGER: return "Produksi";
            default: return "Operasional";
        }
    }

    public synchronized JSONObject getStaff(String companyId) throws JSONException {
        OperatingCompany company = requireCompany(companyId);
        JSONArray employees = new JSONArray();
        JSONArray candidates = new JSONArray();
        for (Employee employee : company.getEmployees()) employees.put(employee.toJsonObject()
                .put("roleLabel", employee.getRole().getLabel()).put("department", department(employee.getRole())));
        for (Employee.Role role : staffRoles(company)) {
            boolean occupied = company.getEmployees().stream().anyMatch(employee -> employee.getRole() == role);
            candidates.put(candidate(company, role).toJsonObject().put("roleLabel", role.getLabel())
                    .put("department", department(role)).put("available", !occupied));
        }
        return new JSONObject().put("success", true).put("companyId", companyId).put("employees", employees)
                .put("staffVersion", company.getStaffVersion())
                .put("candidates", candidates).put("dailyPayroll", company.calculateDailyPayroll().getAmount().toPlainString())
                .put("payrollArrears", company.getPayrollArrears().getAmount().toPlainString())
                .put("currency", "IDR").put("effectiveRule", "NEXT_DAILY_SETTLEMENT");
    }

    public synchronized JSONObject previewStaffAction(String companyId, String action, String employeeId, String role) throws JSONException {
        OperatingCompany company = requireCompany(companyId);
        if (action == null || employeeId == null || employeeId.trim().isEmpty())
            throw new IllegalArgumentException("Aksi dan identitas pegawai wajib diisi");
        if ((action.equals("HIRE") || action.equals("ASSIGN")) && (role == null || role.trim().isEmpty()))
            throw new IllegalArgumentException("Jabatan tujuan wajib diisi");
        Employee current = company.getEmployee(employeeId);
        Employee after;
        Money cost;
        switch (action) {
            case "HIRE":
                if (current != null) throw new IllegalStateException("Pegawai sudah direkrut");
                after = candidate(company, Employee.Role.valueOf(role));
                if (!employeeId.equals(after.getId())) throw new IllegalArgumentException("Kandidat tidak dikenal");
                cost = after.getDailySalary().multiply(2);
                break;
            case "TRAIN":
                if (current == null) throw new IllegalArgumentException("Pegawai tidak ditemukan");
                if (current.getLevel() >= 5) throw new IllegalStateException("Level pegawai sudah maksimum");
                after = new Employee(current.getId(), current.getName(), current.getRole(), current.getLevel());
                after.upgrade();
                cost = after.getDailySalary().multiply(3);
                break;
            case "ASSIGN":
                if (current == null) throw new IllegalArgumentException("Pegawai tidak ditemukan");
                if (current.getRole() == Employee.Role.CEO) throw new IllegalStateException("CEO tidak boleh meninggalkan jabatan");
                Employee.Role target = Employee.Role.valueOf(role);
                candidate(company, target);
                if (target == current.getRole()) throw new IllegalArgumentException("Jabatan tujuan harus berbeda");
                after = new Employee(current.getId(), current.getName(), target, current.getLevel());
                cost = after.getDailySalary().multiply(2);
                break;
            case "DISMISS":
                if (current == null) throw new IllegalArgumentException("Pegawai tidak ditemukan");
                if (current.getRole() == Employee.Role.CEO) throw new IllegalStateException("CEO tidak boleh diberhentikan tanpa pengganti");
                after = null;
                cost = current.getDailySalary();
                break;
            default: throw new IllegalArgumentException("Aksi pegawai tidak dikenal");
        }
        if (after != null && company.getEmployees().stream().anyMatch(employee ->
                !employee.getId().equals(employeeId) && employee.getRole() == after.getRole()))
            throw new IllegalStateException("Jabatan sudah terisi");
        Money basePayroll = Money.zero(Currency.IDR);
        for (Employee employee : company.getEmployees())
            if (!employee.getId().equals(employeeId)) basePayroll = basePayroll.add(employee.getDailySalary());
        if (after != null) basePayroll = basePayroll.add(after.getDailySalary());
        return new JSONObject().put("success", true).put("companyId", companyId).put("action", action)
                .put("staffVersion", company.getStaffVersion())
                .put("employeeId", employeeId).put("cost", cost.getAmount().toPlainString()).put("currency", "IDR")
                .put("salaryBefore", current == null ? "0" : current.getDailySalary().getAmount().toPlainString())
                .put("salaryAfter", after == null ? "0" : after.getDailySalary().getAmount().toPlainString())
                .put("dailyPayrollBefore", company.calculateDailyPayroll().getAmount().toPlainString())
                .put("dailyPayrollAfter", basePayroll.multiply(company.getBenefitTier().getCostMultiplier()).getAmount().toPlainString())
                .put("effectiveRule", "NEXT_DAILY_SETTLEMENT")
                .put("employeeAfter", after == null ? JSONObject.NULL : after.toJsonObject());
    }

    public synchronized JSONObject executeStaffAction(String requestId, String companyId, String action,
            String employeeId, String role, Account companyAccount, EconomyLedger ledger, long now) throws JSONException {
        return executeStaffAction(requestId, companyId, action, employeeId, role, companyAccount, ledger, now,
                requireCompany(companyId).getStaffVersion());
    }

    public synchronized JSONObject executeStaffAction(String requestId, String companyId, String action,
            String employeeId, String role, Account companyAccount, EconomyLedger ledger, long now, long expectedVersion) throws JSONException {
        OperatingCompany company = requireCompany(companyId);
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 160)
            throw new IllegalArgumentException("ID aksi pegawai tidak valid");
        if (companyAccount == null || !company.getAccountId().equals(companyAccount.getId())
                || companyAccount.getType() != AccountType.COMPANY)
            throw new IllegalArgumentException("Akun bukan milik perusahaan");
        Objects.requireNonNull(ledger, "Ledger wajib tersedia");
        String payload = new JSONArray().put(action).put(employeeId).put(role == null ? "" : role).toString();
        JSONObject existing = company.getStaffReceipt(requestId);
        if (existing != null) {
            if (!payload.equals(existing.getString("payload"))) throw new IllegalArgumentException("ID aksi sudah digunakan untuk input berbeda");
            return new JSONObject(existing.toString());
        }
        if (expectedVersion != company.getStaffVersion()) throw new IllegalStateException("STALE: susunan atau kebijakan pegawai berubah; tinjau ulang biaya");
        if (company.getStaffVersion() == Long.MAX_VALUE) throw new IllegalStateException("Versi pegawai mencapai batas");
        JSONObject quote = previewStaffAction(companyId, action, employeeId, role);
        Money cost = new Money(new java.math.BigDecimal(quote.getString("cost")), Currency.IDR);
        if (companyAccount.getAvailableCash(Currency.IDR).subtract(cost).isLessThan(company.getPayrollArrears()))
            throw new IllegalStateException("Kas tidak cukup setelah menyisihkan tunggakan gaji");
        ledger.recordMutation("COMP-STAFF-" + new JSONArray().put(companyId).put(requestId), companyAccount,
                LedgerEntryType.TRANSACTION_FEE, cost, false, now, "Biaya " + action + " pegawai " + employeeId);
        if (quote.isNull("employeeAfter")) company.removeEmployee(employeeId);
        else company.addEmployee(Employee.fromJsonObject(quote.getJSONObject("employeeAfter")));
        JSONObject receipt = new JSONObject(quote.toString()).put("requestId", requestId).put("payload", payload)
                .put("status", "COMPLETED").put("receiptId", "STAFF-" + companyId + "-" + requestId);
        company.recordStaffReceipt(requestId, receipt);
        return receipt;
    }

    public synchronized JSONObject previewCapitalTransfer(String companyId, boolean toCompany, Account personal,
            Account companyAccount, Money amount) throws JSONException {
        OperatingCompany company = requireCompany(companyId);
        if (personal == null || personal.getType() != AccountType.PERSONAL || companyAccount == null
                || companyAccount.getType() != AccountType.COMPANY || !company.getAccountId().equals(companyAccount.getId())
                || amount == null || amount.getCurrency() != Currency.IDR || !amount.isPositive())
            throw new IllegalArgumentException("Akun atau nominal transfer tidak valid");
        Money available = (toCompany ? personal : companyAccount).getAvailableCash(Currency.IDR);
        Money reserve = company.getPayrollArrears().add(company.calculateDailyPayroll()).add(company.calculateDailyOperatingCost());
        boolean eligible = !available.isLessThan(amount) && (toCompany || !available.subtract(amount).isLessThan(reserve));
        String version = new JSONArray().put(company.getStaffVersion()).put(personal.getAvailableCash(Currency.IDR).getAmount().toPlainString())
                .put(companyAccount.getAvailableCash(Currency.IDR).getAmount().toPlainString()).put(company.getPayrollArrears().getAmount().toPlainString()).toString();
        return new JSONObject().put("success", true).put("companyId", companyId).put("toCompany", toCompany)
                .put("amount", amount.getAmount().toPlainString()).put("currency", "IDR").put("fee", "0")
                .put("version", version).put("eligible", eligible).put("personalAccountId", personal.getId())
                .put("companyAccountId", companyAccount.getId()).put("operatingReserve", reserve.getAmount().toPlainString())
                .put("reason", eligible ? "" : "Kas tersedia tidak cukup setelah cadangan operasional dan tunggakan");
    }

    public synchronized JSONObject executeCapitalTransfer(String requestId, String companyId, boolean toCompany,
            Account personal, Account companyAccount, Money amount, String expectedVersion, EconomyLedger ledger, long now) throws JSONException {
        OperatingCompany company = requireCompany(companyId);
        JSONObject quote = previewCapitalTransfer(companyId, toCompany, personal, companyAccount, amount);
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 160 || now < 0)
            throw new IllegalArgumentException("Identitas transfer tidak valid");
        String payload = new JSONArray().put("CAPITAL").put(toCompany).put(personal.getId()).put(companyAccount.getId())
                .put(amount.getAmount().toPlainString()).toString();
        JSONObject previous = company.getStaffReceipt(requestId);
        if (previous != null) {
            if (!payload.equals(previous.getString("payload"))) throw new IllegalArgumentException("ID transfer sudah digunakan untuk input berbeda");
            return new JSONObject(previous.toString());
        }
        if (!quote.getString("version").equals(expectedVersion)) throw new IllegalStateException("STALE: saldo perusahaan berubah; tinjau ulang transfer");
        if (!quote.getBoolean("eligible")) throw new IllegalStateException(quote.getString("reason"));
        Account debit = toCompany ? personal : companyAccount, credit = toCompany ? companyAccount : personal;
        LedgerEntryType type = toCompany ? LedgerEntryType.COMPANY_CAPITAL_INJECTION : LedgerEntryType.COMPANY_CAPITAL_WITHDRAWAL;
        String key = "COMP-CAPITAL-" + new JSONArray().put(companyId).put(requestId);
        if (ledger.isRequestProcessed(key + "-DEBIT") || ledger.isRequestProcessed(key + "-CREDIT"))
            throw new IllegalStateException("Transfer terputus harus dipulihkan dari checkpoint transaksi");
        ledger.recordMutation(key + "-DEBIT", debit, type, amount, false, now, "Transfer modal " + companyId);
        ledger.recordMutation(key + "-CREDIT", credit, type, amount, true, now, "Penerimaan modal " + companyId);
        JSONObject receipt = new JSONObject(quote.toString()).put("requestId", requestId).put("payload", payload)
                .put("status", "COMPLETED").put("timestamp", now).put("receiptId", "CAPITAL-" + companyId + "-" + requestId);
        company.recordStaffReceipt(requestId, receipt);
        return receipt;
    }

    public synchronized JSONObject previewCompanyAction(String companyId, String action, int hours, String benefitTier) throws JSONException {
        OperatingCompany company = requireCompany(companyId);
        OperatingCompany projected = OperatingCompany.fromJsonObject(company.toJsonObject());
        Money cost = Money.zero(Currency.IDR);
        if ("UPGRADE".equals(action)) {
            projected.upgradeLevel();
            // ponytail: expansion is paid once and available at the next daily settlement, without a second construction clock.
            cost = company.calculateDailyOperatingCost().multiply(10).add(Money.of(5_000_000L * company.getLevel(), Currency.IDR));
        } else if ("POLICY".equals(action)) {
            projected.setWorkPolicy(hours, OperatingCompany.BenefitTier.valueOf(benefitTier));
        } else throw new IllegalArgumentException("Aksi perusahaan tidak dikenal");
        return new JSONObject().put("success", true).put("companyId", companyId).put("action", action)
                .put("version", company.getStaffVersion()).put("cost", cost.getAmount().toPlainString()).put("currency", "IDR")
                .put("levelBefore", company.getLevel()).put("levelAfter", projected.getLevel())
                .put("workHours", projected.getWorkHours()).put("benefitTier", projected.getBenefitTier().name())
                .put("dailyPayrollBefore", company.calculateDailyPayroll().getAmount().toPlainString())
                .put("dailyPayrollAfter", projected.calculateDailyPayroll().getAmount().toPlainString())
                .put("dailyCapacityBefore", company.calculateDailyCapacity().getAmount().toPlainString())
                .put("dailyCapacityAfter", projected.calculateDailyCapacity().getAmount().toPlainString())
                .put("dailyOperatingCostBefore", company.calculateDailyOperatingCost().getAmount().toPlainString())
                .put("dailyOperatingCostAfter", projected.calculateDailyOperatingCost().getAmount().toPlainString())
                .put("durationGameSeconds", 0).put("effectiveRule", "NEXT_DAILY_SETTLEMENT");
    }

    public synchronized JSONObject executeCompanyAction(String requestId, String companyId, String action,
            int hours, String benefitTier, long expectedVersion, Account companyAccount, EconomyLedger ledger, long timestamp) throws JSONException {
        OperatingCompany company = requireCompany(companyId);
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 160 || timestamp < 0)
            throw new IllegalArgumentException("Identitas aksi perusahaan tidak valid");
        if (companyAccount == null || companyAccount.getType() != AccountType.COMPANY
                || !company.getAccountId().equals(companyAccount.getId()))
            throw new IllegalArgumentException("Akun bukan milik perusahaan");
        Objects.requireNonNull(ledger);
        String payload = new JSONArray().put(action).put(hours).put(benefitTier).toString();
        JSONObject existing = company.getStaffReceipt(requestId);
        if (existing != null) {
            if (!payload.equals(existing.getString("payload"))) throw new IllegalArgumentException("ID aksi sudah dipakai untuk input berbeda");
            return new JSONObject(existing.toString());
        }
        if (expectedVersion != company.getStaffVersion()) throw new IllegalStateException("STALE: perusahaan berubah; tinjau ulang");
        if (company.getStaffVersion() >= Long.MAX_VALUE - 1) throw new IllegalStateException("Versi perusahaan mencapai batas");
        JSONObject quote = previewCompanyAction(companyId, action, hours, benefitTier);
        Money cost = new Money(new java.math.BigDecimal(quote.getString("cost")), Currency.IDR);
        Money reserve = company.getPayrollArrears().add(company.calculateDailyPayroll()).add(company.calculateDailyOperatingCost());
        if (cost.isPositive() && companyAccount.getAvailableCash(Currency.IDR).subtract(cost).isLessThan(reserve))
            throw new IllegalStateException("Kas tidak cukup setelah menyisihkan tunggakan dan biaya operasi satu hari");
        if (cost.isPositive()) ledger.recordMutation("COMP-ACTION-" + new JSONArray().put(companyId).put(requestId),
                companyAccount, LedgerEntryType.TRANSACTION_FEE, cost, false, timestamp, "Biaya ekspansi " + company.getName());
        if ("UPGRADE".equals(action)) company.upgradeLevel();
        else {
            company.setWorkPolicy(hours, OperatingCompany.BenefitTier.valueOf(benefitTier));
            company.recordPolicy(timestamp);
        }
        JSONObject receipt = new JSONObject(quote.toString()).put("requestId", requestId).put("payload", payload)
                .put("status", "COMPLETED").put("timestamp", timestamp).put("receiptId", "COMPANY-" + companyId + "-" + requestId);
        company.recordStaffReceipt(requestId, receipt);
        return receipt;
    }

    public JSONArray exportState() throws JSONException {
        JSONArray arr = new JSONArray();
        for (OperatingCompany c : companies.values()) {
            arr.put(c.toJsonObject());
        }
        return arr;
    }

    public void restoreState(JSONArray arr) throws JSONException {
        Map<String, OperatingCompany> restored = new LinkedHashMap<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                OperatingCompany c = OperatingCompany.fromJsonObject(arr.getJSONObject(i));
                if (restored.put(c.getId(), c) != null) throw new IllegalArgumentException("ID perusahaan duplikat");
            }
        }
        companies.clear();
        companies.putAll(restored);
    }
}
