package org.cpts422.Femininomenon.App.Controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password) {

        return "home";
    }
}
