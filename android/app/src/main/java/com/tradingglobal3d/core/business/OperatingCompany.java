package com.tradingglobal3d.core.business;

import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final Map<String, Employee> employees = new LinkedHashMap<>();

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
    public int getLevel() { return level; }
    public int getWorkHours() { return workHours; }
    public BenefitTier getBenefitTier() { return benefitTier; }
    public int getMorale() { return morale; }
    public Collection<Employee> getEmployees() { return Collections.unmodifiableCollection(employees.values()); }

    public void setWorkPolicy(int workHours, BenefitTier benefitTier) {
        if (workHours != 6 && workHours != 8 && workHours != 10) {
            throw new IllegalArgumentException("Kebijakan jam kerja yang sah hanya 6, 8, atau 10 jam");
        }
        this.workHours = workHours;
        this.benefitTier = Objects.requireNonNull(benefitTier);
        updateMorale();
    }

    public void addEmployee(Employee emp) {
        Objects.requireNonNull(emp);
        employees.put(emp.getId(), emp);
        updateMorale();
    }

    public Employee getEmployee(String employeeId) {
        return employees.get(employeeId);
    }

    private void updateMorale() {
        int base = 60;
        if (workHours == 6) base += 20;
        else if (workHours == 10) base -= 20;

        base += benefitTier.getMoraleBonus();

        for (Employee e : employees.values()) {
            base += e.getMoraleContribution();
        }

        this.morale = Math.max(10, Math.min(100, base));
    }

    public Money calculateDailyPayroll() {
        long totalBase = 0;
        for (Employee e : employees.values()) {
            totalBase += e.getDailySalary().getAmount().longValue();
        }
        long withBenefits = Math.round(totalBase * benefitTier.getCostMultiplier());
        return Money.of(withBenefits, Currency.IDR);
    }

    public Money calculateDailyRevenue() {
        long basePerLevel = 25_000_000L; // 25 jt basis revenue harian per level
        double prod = 1.0;
        for (Employee e : employees.values()) {
            prod += (e.getProductivityMultiplier() - 1.0);
        }

        // Efek jam kerja: 10 jam = 1.25x prod, 6 jam = 0.85x prod
        if (workHours == 10) prod *= 1.25;
        else if (workHours == 6) prod *= 0.85;

        // Efek morale: 100 morale = 1.0x, 50 morale = 0.5x
        double moraleFactor = (double) morale / 100.0;

        double finalRevenue = (double) basePerLevel * (double) level * prod * moraleFactor;
        return Money.of(Math.round(finalRevenue), Currency.IDR);
    }

    public Money calculateDailyNetProfit() {
        return calculateDailyRevenue().subtract(calculateDailyPayroll());
    }

    public void upgradeLevel() {
        this.level++;
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
        obj.put("dailyRevenue", calculateDailyRevenue().getAmount().toPlainString());
        obj.put("dailyPayroll", calculateDailyPayroll().getAmount().toPlainString());
        obj.put("dailyNetProfit", calculateDailyNetProfit().getAmount().toPlainString());

        JSONArray empArr = new JSONArray();
        for (Employee e : employees.values()) {
            empArr.put(e.toJsonObject());
        }
        obj.put("employees", empArr);
        return obj;
    }

    public static OperatingCompany fromJsonObject(JSONObject obj) throws JSONException {
        String id = obj.getString("id");
        String name = obj.getString("name");
        String sector = obj.getString("sector");
        String accountId = obj.getString("accountId");
        int level = obj.optInt("level", 1);
        OperatingCompany comp = new OperatingCompany(id, name, sector, accountId, level);
        comp.workHours = obj.optInt("workHours", 8);
        comp.benefitTier = BenefitTier.valueOf(obj.optString("benefitTier", BenefitTier.STANDARD.name()));
        comp.morale = obj.optInt("morale", 75);

        JSONArray empArr = obj.optJSONArray("employees");
        if (empArr != null) {
            for (int i = 0; i < empArr.length(); i++) {
                comp.addEmployee(Employee.fromJsonObject(empArr.getJSONObject(i)));
            }
        }
        return comp;
    }
}
