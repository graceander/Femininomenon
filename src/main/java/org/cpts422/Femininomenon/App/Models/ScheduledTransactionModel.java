package org.cpts422.Femininomenon.App.Models;
import jakarta.persistence.*;
import jdk.jfr.Category;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "scheduled_transactions_table")
public class ScheduledTransactionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    UserModel user;

    @Column(name = "frequency", nullable = false)
    String frequency;

    @Column(name = "recentPayment", nullable = false)
    LocalDateTime recentPayment;

    @Column(name = "amount", nullable = false)
    float amount;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    CategoryType category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    TransactionType type;

    @Column(name = "account")
    String account;

    public enum CategoryType {
        GROCERIES,
        UTILITIES,
        ENTERTAINMENT,
        TRANSPORTATION,
        SALARY,
        HEALTHCARE,
        OTHER
    }

    public enum TransactionType {
        INCOME,
        EXPENSE
    }

    public ScheduledTransactionModel() {}

    public ScheduledTransactionModel(UserModel user, String frequency, LocalDateTime recentPayment, float amount, CategoryType category, String description, TransactionType type, String account) {
        this.user = user;
        this.frequency = frequency;
        this.recentPayment = recentPayment;
        this.amount = amount;
        this.category = category;
        this.description = "Scheduled Transaction: " + description;
        this.type = type;
        this.account = account;
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

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public LocalDateTime getRecentPayment() {
        return recentPayment;
    }

    public void setRecentPayment(LocalDateTime recentPayment) {
        this.recentPayment = recentPayment;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public CategoryType getCategory() {
        return category;
    }

    public void setCategory(CategoryType category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduledTransactionModel that = (ScheduledTransactionModel) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(frequency, that.frequency) &&
                Objects.equals(recentPayment, that.recentPayment) &&
                Objects.equals(amount, that.amount) &&
                category == that.category &&
                Objects.equals(description, that.description) &&
                type == that.type &&
                Objects.equals(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, frequency, recentPayment, amount, category, description, type, account);
    }

    @Override
    public String toString() {
        return "ScheduledTransaction{" +
                "id=" + id +
                ", user=" + user +
                ", frequency=" + frequency +
                ", recentPayment=" + recentPayment +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", account='" + account + '\'' +
                '}';
    }
}
