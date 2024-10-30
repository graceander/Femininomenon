package org.cpts422.Femininomenon.App.Models;

import jakarta.persistence.*;
import org.apache.catalina.User;

import java.util.Objects;

@Entity
@Table(name = "rules_table")
public class UserRuleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    UserModel user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    TransactionModel.CategoryType category;

    @Column(name = "limit_amount", nullable = false)
    float limitAmount;

    @Column(name = "frequency", nullable = false)
    @Enumerated(EnumType.STRING)
    Frequency frequency;

    @Column(name = "rule_type", nullable = false)
    @Enumerated(EnumType.STRING)
    RuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "additional_category")
    TransactionModel.CategoryType additionalCategory;

    public enum Frequency {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    public enum RuleType {
        MAXIMUM_SPENDING, MINIMUM_SAVINGS, NOT_EXCEED_CATEGORY
    }

    public UserRuleModel() {}

    public UserRuleModel(UserModel user, TransactionModel.CategoryType category, float limitAmount, Frequency frequency, RuleType ruleType, TransactionModel.CategoryType additionalCategory) {
        this.user = user;
        this.category = category;
        this.limitAmount = limitAmount;
        this.frequency = frequency;
        this.ruleType = ruleType;
        this.additionalCategory = additionalCategory;
    }


    public TransactionModel.CategoryType getAdditionalCategory() {
        return additionalCategory;
    }

    public void setAdditionalCategory(TransactionModel.CategoryType additionalCategory) {
        this.additionalCategory = additionalCategory;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public TransactionModel.CategoryType getCategory() {
        return category;
    }

    public void setCategory(TransactionModel.CategoryType category) {
        this.category = category;
    }

    public float getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(float limitAmount) {
        this.limitAmount = limitAmount;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRuleModel ruleModel = (UserRuleModel) o;
        return Float.compare(ruleModel.limitAmount, limitAmount) == 0 &&
                Objects.equals(id, ruleModel.id) &&
                Objects.equals(user, ruleModel.user) &&
                Objects.equals(category, ruleModel.category) &&
                frequency == ruleModel.frequency &&
                ruleType == ruleModel.ruleType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, category, limitAmount, frequency, ruleType);
    }

    @Override
    public String toString() {
        return "RuleModel{" +
                "id=" + id +
                ", user=" + user +
                ", category='" + category + '\'' +
                ", limitAmount=" + limitAmount +
                ", frequency=" + frequency +
                ", ruleType=" + ruleType +
                ", additionalCategory=" + additionalCategory +
                '}';
    }
}
