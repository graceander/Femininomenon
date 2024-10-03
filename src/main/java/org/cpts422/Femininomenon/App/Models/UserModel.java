package org.cpts422.Femininomenon.App.Models;
import jakarta.persistence.*;
import org.cpts422.Femininomenon.App.Models.UserRuleModel;

import java.util.*;

@Entity
@Table(name = "users_table")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String login;
    String password;
    String email;
    String firstName;
    String lastName;

    @Column(name = "currency")
    private String currency = "USD"; // Default currency

    @ElementCollection
    @CollectionTable(name = "user_spending_limits", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "spending_limit")
    private Map<TransactionModel.CategoryType, Double> spendingLimits = new HashMap<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRuleModel> rules = new ArrayList<>();

    public List<UserRuleModel> getRules() {
        return rules;
    }

    public void setRules(List<UserRuleModel> rules) {
        this.rules = rules;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCurrency() { return currency; }

    public void setCurrency(String currency) { this.currency = currency; }

    // Equals and HashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel that = (UserModel) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(login, that.login) &&
                Objects.equals(password, that.password) &&
                Objects.equals(email, that.email) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login, password, email, firstName, lastName);
    }

    // The password is not included in the toString method to keep it safe
    @Override
    public String toString() {
        return "UserEntity{" +
                "Email='" + email + '\'' +
                ", login='" + login + '\'' +
                ", id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    public Map<TransactionModel.CategoryType, Double> getSpendingLimits() {
        return spendingLimits;
    }

    public void setSpendingLimit(TransactionModel.CategoryType category, Double limit) {
        this.spendingLimits.put(category, limit);
    }

}
