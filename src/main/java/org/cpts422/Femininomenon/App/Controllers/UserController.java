package org.cpts422.Femininomenon.App.Controllers;

import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {
    private final UsersService usersService;

    public UserController(UsersService usersService) {
        this.usersService = usersService;
    }


    @GetMapping("/register")
    public String getRegisterPage(Model model){
        model.addAttribute("registerRequest", new UserModel());

        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute UserModel userModel) {
        UserModel registerNew = usersService.registerUser(userModel.getLogin(), userModel.getPassword(), userModel.getEmail());
        if (registerNew == null) {
            return "error";
        } else {
            return "redirect:/";
        }
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("loginRequest", new UserModel());
        return "index";
    }
    @PostMapping("/login")
    public String login(@ModelAttribute UserModel userModel) {
        System.out.println("login request: " + userModel);
        UserModel authenticateTheUser = usersService.authenticateUser(userModel.getLogin(), userModel.getPassword());
        System.out.println(authenticateTheUser);
        if(authenticateTheUser == null) {
            System.out.println("Error");
            return "error";
        } else {
            System.out.println("Login successful, redirecting to home.");
            return "redirect:/home"; // Or another page for testing
        }
    }

    @GetMapping("/home")
    public String homePage(Model model) {
        return "home";
    }









}
