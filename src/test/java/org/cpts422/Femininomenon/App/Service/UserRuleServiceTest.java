package org.cpts422.Femininomenon.App.Service;
import org.cpts422.Femininomenon.App.Models.UserRuleModel;
import org.cpts422.Femininomenon.App.Repository.UserRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserRuleServiceTest {
    @InjectMocks
    private UserRuleService userRuleService;

    @Mock
    private UserRuleRepository userRuleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRulesByUserLogin() {
        String userLogin = "mattp";
        UserRuleModel rule1 = new UserRuleModel();
        UserRuleModel rule2 = new UserRuleModel();
        List<UserRuleModel> expectedRules = new ArrayList<>();
        expectedRules.add(rule1);
        expectedRules.add(rule2);
        when(userRuleRepository.findByUserLogin(userLogin)).thenReturn(expectedRules);
        List<UserRuleModel> actualRules = userRuleService.getRulesByUserLogin(userLogin);
        assertEquals(expectedRules, actualRules);
        verify(userRuleRepository).findByUserLogin(userLogin);
    }

    @Test
    void testSaveRule() {
        UserRuleModel rule = new UserRuleModel();
        userRuleService.saveRule(rule);
        verify(userRuleRepository).save(rule);
    }
}