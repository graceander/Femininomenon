package org.cpts422.Femininomenon.App.Controllers;

import static org.junit.jupiter.api.Assertions.*;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Models.UserRuleModel;
import org.cpts422.Femininomenon.App.Service.UserRuleService;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
class UserRuleControllerTest {
    @InjectMocks
    private UserRuleController userRuleController;
    @Mock
    private UserRuleService userRuleService;
    @Mock
    private UsersService usersService;
    @Mock
    private Model model;
    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testViewRules() {
        String userLogin = "Matt";
        UserRuleModel rule1 = new UserRuleModel();
        UserRuleModel rule2 = new UserRuleModel();
        List<UserRuleModel> rules = Arrays.asList(rule1, rule2);
        when(userRuleService.getRulesByUserLogin(userLogin)).thenReturn(rules);
        String viewName = userRuleController.viewRules(userLogin, model);
        verify(model).addAttribute("rules", rules);
        verify(model).addAttribute("userLogin", userLogin);
        assertEquals("userAddRules", viewName);
    }

    @Test
    void testAddRuleCompare() {
        String userLogin = "Matt";
        TransactionModel.CategoryType category = TransactionModel.CategoryType.GROCERIES;
        float limitAmount = 100.0f;
        UserRuleModel.Frequency frequency = UserRuleModel.Frequency.WEEKLY;
        UserRuleModel.RuleType ruleType = UserRuleModel.RuleType.NOT_EXCEED_CATEGORY;
        TransactionModel.CategoryType extraCategory = TransactionModel.CategoryType.TRANSPORTATION;
        UserModel user = new UserModel();
        when(usersService.findByLogin(userLogin)).thenReturn(user);
        String viewName = userRuleController.addRule(userLogin, category, limitAmount, frequency, ruleType, extraCategory);
        UserRuleModel expectedRule = new UserRuleModel(user, category, limitAmount, frequency, ruleType, extraCategory);
        verify(userRuleService).saveRule(expectedRule);
        assertEquals("redirect:/home?login=" + userLogin, viewName);
    }

    @Test
    void testAddRuleMaxiumSpending() {
        String userLogin = "Matt";
        TransactionModel.CategoryType category = TransactionModel.CategoryType.GROCERIES;
        float limitAmount = 100.0f;
        UserRuleModel.Frequency frequency = UserRuleModel.Frequency.WEEKLY;
        UserRuleModel.RuleType ruleType = UserRuleModel.RuleType.MAXIMUM_SPENDING;
        TransactionModel.CategoryType GROCERIES = null;
        UserModel user = new UserModel();
        when(usersService.findByLogin(userLogin)).thenReturn(user);
        String viewName = userRuleController.addRule(userLogin, category, limitAmount, frequency, ruleType, null);
        UserRuleModel expectedRule = new UserRuleModel(user, category, limitAmount, frequency, ruleType, null);
        verify(userRuleService).saveRule(expectedRule);
        assertEquals("redirect:/home?login=" + userLogin, viewName);
    }
}