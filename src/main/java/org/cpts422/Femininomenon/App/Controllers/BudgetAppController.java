package org.cpts422.Femininomenon.App.Controllers;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BudgetAppController {

    @RequestMapping("/")
    public String index(Model model) {
        return "index";
    }
}
