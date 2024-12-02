package org.cpts422.Femininomenon.App.UnitTests.Models;

import static org.junit.jupiter.api.Assertions.*;
import org.cpts422.Femininomenon.App.Models.TransactionModel.CategoryType;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Models.UserRuleModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
class UserRuleModelTest {
    private UserRuleModel userRule;
    private UserModel user;

    @BeforeEach
    void setUp() {
        user = new UserModel();
        user.setId(1);
        userRule = new UserRuleModel();
    }

    @Test
    void testValues() {
        assertNull(userRule.getUser());
        assertNull(userRule.getCategory());
        assertEquals(0.0f, userRule.getLimitAmount());
        assertNull(userRule.getFrequency());
        assertNull(userRule.getRuleType());
        assertNull(userRule.getAdditionalCategory());
    }

    @Test
    void testConstructorAndGetters() {
        userRule = new UserRuleModel(user, CategoryType.GROCERIES, 100.0f, UserRuleModel.Frequency.MONTHLY, UserRuleModel.RuleType.MAXIMUM_SPENDING, CategoryType.ENTERTAINMENT);
        assertEquals(user, userRule.getUser());
        assertEquals(CategoryType.GROCERIES, userRule.getCategory());
        assertEquals(100.0f, userRule.getLimitAmount());
        assertEquals(UserRuleModel.Frequency.MONTHLY, userRule.getFrequency());
        assertEquals(UserRuleModel.RuleType.MAXIMUM_SPENDING, userRule.getRuleType());
        assertEquals(CategoryType.ENTERTAINMENT, userRule.getAdditionalCategory());
    }

    @Test
    void testGetAndSetUser() {
        userRule.setUser(user);
        assertEquals(user, userRule.getUser());
    }

    @Test
    void testGetAndSetCategory() {
        userRule.setCategory(CategoryType.ENTERTAINMENT);
        assertEquals(CategoryType.ENTERTAINMENT, userRule.getCategory());
    }

    @Test
    void testGetAndSetLimitAmount() {
        userRule.setLimitAmount(200.0f);
        assertEquals(200.0f, userRule.getLimitAmount());
    }

    @Test
    void testGetAndSetFrequency() {
        userRule.setFrequency(UserRuleModel.Frequency.WEEKLY);
        assertEquals(UserRuleModel.Frequency.WEEKLY, userRule.getFrequency());
    }

    @Test
    void testGetAndSetRuleType() {
        userRule.setRuleType(UserRuleModel.RuleType.MAXIMUM_SPENDING);
        assertEquals(UserRuleModel.RuleType.MAXIMUM_SPENDING, userRule.getRuleType());
    }

    @Test
    void testGetAndSetAdditionalCategory() {
        userRule.setAdditionalCategory(CategoryType.GROCERIES);
        assertEquals(CategoryType.GROCERIES, userRule.getAdditionalCategory());
    }
    @Test
    void testGetAndSetId() {
        userRule.setId(1L);
        assertEquals(1L, userRule.getId());
    }
}