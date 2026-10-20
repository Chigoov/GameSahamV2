package com.tradingglobal3d.core.business;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Model Pegawai / Eksekutif Perusahaan Operasional.
 */
public final class Employee {
    public enum Role {
        CEO("Chief Executive Officer", 5_000_000, 0.25, 10),
        HEAD_OF_RESEARCH("Kepala Riset & Analis", 2_500_000, 0.15, 5),
        LEAD_TRADER("Lead Trader Institusi", 2_000_000, 0.15, 5),
        HR_DIRECTOR("Direktur SDM & Budaya", 1_500_000, 0.05, 15),
        COMPLIANCE_HEAD("Kepala Kepatuhan & Risiko", 1_500_000, 0.05, 10);

        private final String label;
        private final long baseSalary;
        private final double productivityBonus;
        private final int moraleBoost;

        Role(String label, long baseSalary, double productivityBonus, int moraleBoost) {
            this.label = label;
            this.baseSalary = baseSalary;
            this.productivityBonus = productivityBonus;
            this.moraleBoost = moraleBoost;
        }

        public String getLabel() { return label; }
        public long getBaseSalary() { return baseSalary; }
        public double getProductivityBonus() { return productivityBonus; }
        public int getMoraleBoost() { return moraleBoost; }
    }

    private final String id;
    private final String name;
    private final Role role;
    private int level;

    public Employee(String id, String name, Role role, int level) {
        this.id = Objects.requireNonNull(id, "ID tidak boleh null");
        this.name = Objects.requireNonNull(name, "Nama tidak boleh null");
        this.role = Objects.requireNonNull(role, "Role tidak boleh null");
        this.level = Math.max(1, level);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public int getLevel() { return level; }

    public Money getDailySalary() {
        long salary = role.getBaseSalary() * (long) level;
        return Money.of(salary, Currency.IDR);
    }

    public double getProductivityMultiplier() {
        return 1.0 + (role.getProductivityBonus() * (double) level);
    }

    public int getMoraleContribution() {
        return role.getMoraleBoost() * level;
    }

    public void upgrade() {
        if (this.level < 5) {
            this.level++;
        }
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("name", name);
        obj.put("role", role.name());
        obj.put("level", level);
        obj.put("salary", getDailySalary().getAmount().toPlainString());
        return obj;
    }

    public static Employee fromJsonObject(JSONObject obj) throws JSONException {
        String id = obj.getString("id");
        String name = obj.getString("name");
        Role role = Role.valueOf(obj.getString("role"));
        int level = obj.optInt("level", 1);
        return new Employee(id, name, role, level);
    }
}
