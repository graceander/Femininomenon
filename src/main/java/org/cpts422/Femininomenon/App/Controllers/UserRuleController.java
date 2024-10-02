package org.cpts422.Femininomenon.App.Controllers;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Models.UserRuleModel;
import org.cpts422.Femininomenon.App.Service.UserRuleService;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user/rules")
public class UserRuleController {
    private final UserRuleService userRuleService;
    private final UsersService usersService;

    @Autowired
    public UserRuleController(UserRuleService userRuleService, UsersService usersService) {
        this.userRuleService = userRuleService;
        this.usersService = usersService;
    }

    @GetMapping("/view")
    public String viewRules(@RequestParam("userLogin") String userLogin, Model model) {
        List<UserRuleModel> rules = userRuleService.getRulesByUserLogin(userLogin);
        model.addAttribute("rules", rules);
        model.addAttribute("userLogin", userLogin);
        return "userAddRules";
    }

    @PostMapping("/add")
    public String addRule(@RequestParam("userLogin") String userLogin,
                          @RequestParam("category") TransactionModel.CategoryType category,
                          @RequestParam("limitAmount") float limitAmount,
                          @RequestParam("frequency") UserRuleModel.Frequency frequency,
                          @RequestParam("ruleType") UserRuleModel.RuleType ruleType,
                          @RequestParam(value = "extraCategory", required = false) TransactionModel.CategoryType extraCategory) {

        UserModel user = usersService.findByLogin(userLogin);

        UserRuleModel newRule;
        if (ruleType == UserRuleModel.RuleType.NOT_EXCEED_CATEGORY) {
            newRule = new UserRuleModel(user, category, limitAmount, frequency, ruleType, extraCategory);
        } else {
            newRule = new UserRuleModel(user, category, limitAmount, frequency, ruleType, null);
        }

        userRuleService.saveRule(newRule);

        return "redirect:/home?login=" + userLogin;
    }




}


