package org.cpts422.Femininomenon.App.Service;

import org.cpts422.Femininomenon.App.Models.UserRuleModel;
import org.cpts422.Femininomenon.App.Repository.UserRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRuleService {
    private final UserRuleRepository userRuleRepository;

    @Autowired
    public UserRuleService(UserRuleRepository userRuleRepository) {
        this.userRuleRepository = userRuleRepository;
    }

    public List<UserRuleModel> getRulesByUserLogin(String userLogin) {
        return userRuleRepository.findByUserLogin(userLogin);
    }

    public void saveRule(UserRuleModel rule) {
        userRuleRepository.save(rule);
    }
}
