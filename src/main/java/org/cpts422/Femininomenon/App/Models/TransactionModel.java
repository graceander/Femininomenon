package org.cpts422.Femininomenon.App.Models;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transactions_table")
public class TransactionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    UserModel user;

    @Column(name = "date", nullable = false)
    LocalDateTime date;

    @Column(name = "amount", nullable = false)
    float amount;

    @Column(name = "category")
    String category;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    TransactionType type;

    @Column(name = "account")
    String account;

    public enum TransactionType {
        INCOME,
        EXPENSE
    }

    public TransactionModel() {}

    public TransactionModel(UserModel user, LocalDateTime date, float amount, String category, String description, TransactionType type, String account) {
        this.user = user;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.description = description;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
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
        TransactionModel that = (TransactionModel) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(date, that.date) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(category, that.category) &&
                Objects.equals(description, that.description) &&
                type == that.type &&
                Objects.equals(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, date, amount, category, description, type, account);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", user=" + user +
                ", date=" + date +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", account='" + account + '\'' +
                '}';
    }
}
