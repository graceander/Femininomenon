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
    public String addRule(@RequestParam String userLogin,
                          @RequestParam TransactionModel.CategoryType category,
                          @RequestParam float limitAmount,
                          @RequestParam UserRuleModel.Frequency frequency,
                          @RequestParam UserRuleModel.RuleType ruleType) {
        UserModel user = usersService.findByLogin(userLogin);
        if (user != null) {
            UserRuleModel newRule = new UserRuleModel(user, category, limitAmount, frequency, ruleType);
            userRuleService.saveRule(newRule);
        }
        return "redirect:/user/rules/view?userLogin=" + userLogin;
    }

    @GetMapping("/goHome")
    public String goHome(@RequestParam("login") String login, Model model) {
        return "redirect:/home?login=" + login;
    }

}


