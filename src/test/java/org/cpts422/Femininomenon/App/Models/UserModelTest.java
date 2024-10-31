package org.cpts422.Femininomenon.App.Models;

import static org.junit.jupiter.api.Assertions.*;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Models.UserRuleModel;
import org.cpts422.Femininomenon.App.Models.TransactionModel.CategoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class UserModelTest {
    private UserModel user;

    @BeforeEach
    void setUp() {
        user = new UserModel();
    }
    @Test
    void testGetAndSetId() {
        user.setId(42);
        assertEquals(42, user.getId());
    }
    @Test
    void testDefaultCurrency() {
        assertEquals("USD", user.getCurrency());
    }

    @Test
    void testSetCurrency() {
        user.setCurrency("EUR");
        assertEquals("EUR", user.getCurrency());
    }

    @Test
    void testGetAndSetLogin() {
        user.setLogin("testUser");
        assertEquals("testUser", user.getLogin());
    }

    @Test
    void testGetAndSetPassword() {
        user.setPassword("password123");
        assertEquals("password123", user.getPassword());
    }

    @Test
    void testGetAndSetEmail() {
        user.setEmail("Matt@Gmail.com");
        assertEquals("Matt@Gmail.com", user.getEmail());
    }

    @Test
    void testGetAndSetFirstName() {
        user.setFirstName("Matt");
        assertEquals("Matt", user.getFirstName());
    }

    @Test
    void testGetAndSetLastName() {
        user.setLastName("Matt");
        assertEquals("Matt", user.getLastName());
    }

    @Test
    void testGetAndSetSpendingLimits() {
        user.setSpendingLimit(CategoryType.GROCERIES, 100.0);
        Map<CategoryType, Double> limits = user.getSpendingLimits();
        assertEquals(100.0, limits.get(CategoryType.GROCERIES));
    }

    @Test
    void testGetAndSetRules() {
        UserRuleModel rule1 = Mockito.mock(UserRuleModel.class);
        UserRuleModel rule2 = Mockito.mock(UserRuleModel.class);
        user.setRules(List.of(rule1, rule2));
        assertEquals(2, user.getRules().size());
        assertTrue(user.getRules().contains(rule1));
        assertTrue(user.getRules().contains(rule2));
    }
}