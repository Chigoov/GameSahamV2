package com.tradingglobal3d.core.business;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;

/**
 * Model Perusahaan Operasional (Empire / Business).
 * Mengelola kebijakan kerja, benefit, moral pegawai, payroll, dan pendapatan harian.
 */
public final class OperatingCompany {
    public enum BenefitTier {
        STANDARD("Standar", 1.0, 0),
        PLUS("Tunjangan Plus", 1.25, 15),
        PREMIUM("Fasilitas Eksekutif", 1.50, 30);

        private final String label;
        private final double costMultiplier;
        private final int moraleBonus;

        BenefitTier(String label, double costMultiplier, int moraleBonus) {
            this.label = label;
            this.costMultiplier = costMultiplier;
            this.moraleBonus = moraleBonus;
        }

        public String getLabel() { return label; }
        public double getCostMultiplier() { return costMultiplier; }
        public int getMoraleBonus() { return moraleBonus; }
    }

    private final String id;
    private final String name;
    private final String sector;
    private final String accountId;
    private int level;
    private int workHours; // 6, 8, 10
    private BenefitTier benefitTier;
    private int morale; // 0 - 100
    private Money payrollArrears = Money.zero(Currency.IDR);
    private long lastOperationsDay = Long.MIN_VALUE;
    private Money lastRevenue = Money.zero(Currency.IDR);
    private Money lastOperatingCost = Money.zero(Currency.IDR);
    private Money lastPayrollPaid = Money.zero(Currency.IDR);
    private final Map<String, Employee> employees = new LinkedHashMap<>();
    private final Map<String, JSONObject> staffReceipts = new LinkedHashMap<>();
    private long staffVersion;
    private final JSONArray policyHistory = new JSONArray();
    private final Map<Long, Money[]> operationalReports = new LinkedHashMap<>();

    public OperatingCompany(String id, String name, String sector, String accountId, int level) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.sector = Objects.requireNonNull(sector);
        this.accountId = Objects.requireNonNull(accountId);
        this.level = Math.max(1, level);
        this.workHours = 8;
        this.benefitTier = BenefitTier.STANDARD;
        this.morale = 75;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSector() { return sector; }
    public String getAccountId() { return accountId; }
    public Money getLastRealizedRevenue() { return lastRevenue; }
    public int getLevel() { return level; }
    public int getWorkHours() { return workHours; }
    public BenefitTier getBenefitTier() { return benefitTier; }
    public int getMorale() { return morale; }
    public Collection<Employee> getEmployees() { return Collections.unmodifiableCollection(employees.values()); }
    public long getStaffVersion() { return staffVersion; }

    public void setWorkPolicy(int workHours, BenefitTier benefitTier) {
        if (workHours != 6 && workHours != 8 && workHours != 10) {
            throw new IllegalArgumentException("Kebijakan jam kerja yang sah hanya 6, 8, atau 10 jam");
        }
        Objects.requireNonNull(benefitTier);
        if (this.workHours != workHours || this.benefitTier != benefitTier) staffVersion = Math.addExact(staffVersion, 1);
        this.workHours = workHours;
        this.benefitTier = benefitTier;
    }

    public void addEmployee(Employee emp) {
        Objects.requireNonNull(emp);
        if (emp.getId().trim().isEmpty()) throw new IllegalArgumentException("ID pegawai wajib diisi");
        Employee previous = employees.get(emp.getId());
        if (previous != null && previous.getRole() == Employee.Role.CEO && emp.getRole() != Employee.Role.CEO)
            throw new IllegalStateException("CEO tidak boleh meninggalkan jabatan");
        for (Employee current : employees.values())
            if (!current.getId().equals(emp.getId()) && current.getRole() == emp.getRole())
                throw new IllegalStateException("Jabatan sudah terisi");
        staffVersion = Math.addExact(staffVersion, 1);
        employees.put(emp.getId(), emp);
    }

    public Employee getEmployee(String employeeId) {
        return employees.get(employeeId);
    }

    public Employee removeEmployee(String employeeId) {
        if (employees.containsKey(employeeId)) staffVersion = Math.addExact(staffVersion, 1);
        return employees.remove(employeeId);
    }
    JSONObject getStaffReceipt(String requestId) { return staffReceipts.get(requestId); }
    void recordStaffReceipt(String requestId, JSONObject receipt) throws JSONException {
        staffReceipts.put(requestId, new JSONObject(receipt.toString()));
    }
    public Money getPayrollArrears() { return payrollArrears; }
    public long getLastOperationsDay() { return lastOperationsDay; }

    void setPayrollArrears(Money amount) { payrollArrears = amount; }
    void completeOperations(long day, Money revenue, Money operatingCost, Money payrollPaid) {
        lastOperationsDay = day;
        lastRevenue = revenue;
        lastOperatingCost = operatingCost;
        lastPayrollPaid = payrollPaid;
        operationalReports.put(day, new Money[]{revenue, operatingCost, payrollPaid, payrollArrears});
        if (payrollArrears.isPositive()) morale = Math.max(0, morale - 20);
        else {
            int target = targetMorale();
            morale += Math.max(-5, Math.min(5, target - morale));
        }
    }

    private int targetMorale() {
        int base = 60;
        if (workHours == 6) base += 20;
        else if (workHours == 10) base -= 20;

        base += benefitTier.getMoraleBonus();

        for (Employee e : employees.values()) {
            base += e.getMoraleContribution();
        }

        return Math.max(0, Math.min(100, base));
    }

    public Money calculateDailyPayroll() {
        Money totalBase = Money.zero(Currency.IDR);
        for (Employee e : employees.values()) totalBase = totalBase.add(e.getDailySalary());
        return totalBase.multiply(BigDecimal.valueOf(benefitTier.getCostMultiplier()));
    }

    // ponytail: legacy free-text sectors map to Jasa; canonical new sectors need no registry.
    public String getSectorModel() {
        String normalized = sector.toLowerCase(Locale.ROOT);
        if (normalized.contains("manufaktur")) return "MANUFAKTUR";
        if (normalized.contains("ritel")) return "RITEL";
        return "JASA";
    }

    public Money calculateDailyOperatingCost() {
        long base = getSectorModel().equals("MANUFAKTUR") ? 15_000_000L
                : getSectorModel().equals("RITEL") ? 5_000_000L : 1_000_000L;
        return Money.of(base, Currency.IDR).multiply(BigDecimal.valueOf(level));
    }

    public Money calculateDailyCapacity() {
        long base = getSectorModel().equals("MANUFAKTUR") ? 50_000_000L
                : getSectorModel().equals("RITEL") ? 20_000_000L : 8_000_000L;
        double productivity = 1.0 + (workHours == 10 ? 0.25 : workHours == 6 ? -0.15 : 0);
        for (Employee e : employees.values()) productivity += e.getProductivityMultiplier() - 1.0;
        double sensitivity = getSectorModel().equals("JASA") ? 0.4 : getSectorModel().equals("RITEL") ? 0.2 : 0.1;
        double moraleFactor = 1.0 - sensitivity * (100 - morale) / 100.0;
        double staffing = employees.isEmpty() ? 0 : getSectorModel().equals("MANUFAKTUR") && employees.size() < 3 ? 0.5 : 1;
        return Money.of(base, Currency.IDR).multiply(BigDecimal.valueOf(level))
                .multiply(productivity * moraleFactor * staffing);
    }

    public BigDecimal getDemandFactor(long day) {
        int range = getSectorModel().equals("JASA") ? 11 : getSectorModel().equals("RITEL") ? 46 : 71;
        int minimum = getSectorModel().equals("JASA") ? 95 : getSectorModel().equals("RITEL") ? 80 : 70;
        // ponytail: deterministic bounded cycle avoids random state; calibrate against earned progression later.
        long cycle = Math.floorMod(day, 997L) * 37 + Math.floorMod(id.hashCode(), 997);
        return BigDecimal.valueOf(minimum + Math.floorMod(cycle, range), 2);
    }

    public Money calculateDailyRevenue(long day) {
        // Demand caps sales, so hiring beyond available demand cannot create unlimited revenue.
        long base = getSectorModel().equals("MANUFAKTUR") ? 50_000_000L
                : getSectorModel().equals("RITEL") ? 20_000_000L : 8_000_000L;
        Money demand = Money.of(base, Currency.IDR).multiply(BigDecimal.valueOf(level)).multiply(getDemandFactor(day));
        Money capacity = calculateDailyCapacity();
        return capacity.isLessThan(demand) ? capacity : demand;
    }

    public Money calculateDailyRevenue() {
        Money revenue = calculateDailyRevenue(lastOperationsDay == Long.MIN_VALUE ? 0 : lastOperationsDay + 1);
        return payrollArrears.isPositive() ? revenue.multiply(0.5) : revenue;
    }

    public Money calculateDailyNetProfit() {
        return calculateDailyRevenue().subtract(calculateDailyPayroll()).subtract(calculateDailyOperatingCost());
    }

    void upgradeLevel() {
        if (level >= 20) throw new IllegalStateException("Level perusahaan sudah maksimum");
        staffVersion = Math.addExact(staffVersion, 1);
        this.level++;
    }

    void recordPolicy(long timestamp) throws JSONException {
        policyHistory.put(new JSONObject().put("timestamp", timestamp).put("workHours", workHours)
                .put("benefitTier", benefitTier.name()).put("effectiveRule", "NEXT_DAILY_SETTLEMENT"));
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("name", name);
        obj.put("sector", sector);
        obj.put("accountId", accountId);
        obj.put("level", level);
        obj.put("workHours", workHours);
        obj.put("benefitTier", benefitTier.name());
        obj.put("morale", morale);
        obj.put("sectorModel", getSectorModel());
        obj.put("payrollArrears", payrollArrears.getAmount().toPlainString());
        obj.put("lastOperationsDay", lastOperationsDay);
        obj.put("lastRevenue", lastRevenue.getAmount().toPlainString());
        obj.put("lastOperatingCost", lastOperatingCost.getAmount().toPlainString());
        obj.put("lastPayrollPaid", lastPayrollPaid.getAmount().toPlainString());
        obj.put("dailyOperatingCost", calculateDailyOperatingCost().getAmount().toPlainString());
        obj.put("dailyCapacity", calculateDailyCapacity().getAmount().toPlainString());
        obj.put("policyEffectiveRule", "NEXT_DAILY_SETTLEMENT");
        obj.put("dailyRevenue", calculateDailyRevenue().getAmount().toPlainString());
        obj.put("dailyPayroll", calculateDailyPayroll().getAmount().toPlainString());
        obj.put("dailyNetProfit", calculateDailyNetProfit().getAmount().toPlainString());

        JSONArray empArr = new JSONArray();
        for (Employee e : employees.values()) {
            empArr.put(e.toJsonObject());
        }
        obj.put("employees", empArr);
        obj.put("staffVersion", staffVersion);
        obj.put("staffReceipts", new JSONObject(staffReceipts));
        obj.put("policyHistory", new JSONArray(policyHistory.toString()));
        JSONArray reports = new JSONArray();
        for (Map.Entry<Long, Money[]> entry : operationalReports.entrySet()) {
            Money[] values = entry.getValue();
            reports.put(new JSONObject().put("day", entry.getKey()).put("revenue", values[0].getAmount().toPlainString())
                    .put("operatingCost", values[1].getAmount().toPlainString()).put("payrollPaid", values[2].getAmount().toPlainString())
                    .put("payrollArrears", values[3].getAmount().toPlainString()).put("currency", "IDR")
                    .put("netCashflow", values[0].subtract(values[1]).subtract(values[2]).getAmount().toPlainString()));
        }
        obj.put("operationalReports", reports);
        return obj;
    }

    public static OperatingCompany fromJsonObject(JSONObject obj) throws JSONException {
        String id = obj.getString("id");
        String name = obj.getString("name");
        String sector = obj.getString("sector");
        String accountId = obj.getString("accountId");
        int level = obj.optInt("level", 1);
        OperatingCompany comp = new OperatingCompany(id, name, sector, accountId, level);
        comp.setWorkPolicy(obj.optInt("workHours", 8),
                BenefitTier.valueOf(obj.optString("benefitTier", BenefitTier.STANDARD.name())));
        comp.morale = Math.max(0, Math.min(100, obj.optInt("morale", 75)));
        comp.payrollArrears = new Money(new BigDecimal(obj.optString("payrollArrears", "0")), Currency.IDR);
        if (comp.payrollArrears.isNegative()) throw new IllegalArgumentException("Tunggakan gaji tidak boleh negatif");
        comp.lastOperationsDay = obj.optLong("lastOperationsDay", Long.MIN_VALUE);
        comp.lastRevenue = new Money(new BigDecimal(obj.optString("lastRevenue", "0")), Currency.IDR);
        comp.lastOperatingCost = new Money(new BigDecimal(obj.optString("lastOperatingCost", "0")), Currency.IDR);
        comp.lastPayrollPaid = new Money(new BigDecimal(obj.optString("lastPayrollPaid", "0")), Currency.IDR);

        JSONArray empArr = obj.optJSONArray("employees");
        if (empArr != null) {
            for (int i = 0; i < empArr.length(); i++) {
                JSONObject saved = empArr.getJSONObject(i);
                int employeeLevel = saved.getInt("level");
                Employee employee = Employee.fromJsonObject(saved);
                if (employeeLevel < 1 || employeeLevel > 5 || employee.getId().trim().isEmpty()
                        || comp.getEmployee(employee.getId()) != null
                        || comp.getEmployees().stream().anyMatch(existing -> existing.getRole() == employee.getRole()))
                    throw new IllegalArgumentException("State pegawai tidak valid");
                comp.addEmployee(employee);
            }
        }
        JSONObject receipts = obj.optJSONObject("staffReceipts");
        if (receipts != null) {
            Iterator<String> keys = receipts.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject receipt = receipts.getJSONObject(key);
                if (key.trim().isEmpty() || key.length() > 160 || !key.equals(receipt.getString("requestId"))
                        || !id.equals(receipt.getString("companyId")) || !receipt.getBoolean("success"))
                    throw new IllegalArgumentException("Receipt pegawai tidak valid");
                receipt.getString("payload");
                comp.recordStaffReceipt(key, receipt);
            }
        }
        comp.staffVersion = obj.optLong("staffVersion", 0);
        if (comp.staffVersion < 0 || comp.staffVersion == Long.MAX_VALUE)
            throw new IllegalArgumentException("Versi pegawai tidak valid");
        JSONArray policy = obj.optJSONArray("policyHistory");
        for (int i = 0; policy != null && i < policy.length(); i++) {
            JSONObject entry = policy.getJSONObject(i);
            int hours = entry.getInt("workHours");
            if ((hours != 6 && hours != 8 && hours != 10) || entry.getLong("timestamp") < 0)
                throw new IllegalArgumentException("Riwayat kebijakan tidak valid");
            BenefitTier.valueOf(entry.getString("benefitTier"));
            comp.policyHistory.put(new JSONObject(entry.toString()));
        }
        JSONArray reports = obj.optJSONArray("operationalReports");
        for (int i = 0; reports != null && i < reports.length(); i++) {
            JSONObject report = reports.getJSONObject(i);
            long day = report.getLong("day");
            Money[] values = new Money[4];
            String[] fields = {"revenue", "operatingCost", "payrollPaid", "payrollArrears"};
            for (int j = 0; j < fields.length; j++) {
                values[j] = new Money(new BigDecimal(report.getString(fields[j])), Currency.IDR);
                if (values[j].isNegative()) throw new IllegalArgumentException("Laporan perusahaan tidak valid");
            }
            if (day > comp.lastOperationsDay || comp.operationalReports.put(day, values) != null)
                throw new IllegalArgumentException("Hari laporan perusahaan tidak valid");
        }
        return comp;
    }
}
