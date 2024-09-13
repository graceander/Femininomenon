package org.cpts422.Femininomenon.App.Controllers;

import org.cpts422.Femininomenon.App.Entity.UserEntity;
import org.cpts422.Femininomenon.App.Repository.UserRepository;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
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
        model.addAttribute("registerRequest", new UserEntity());

        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute UserEntity userEntity) {
        UserEntity registerNew = usersService.registerUser(userEntity.getLogin(), userEntity.getPassword(), userEntity.getEmail());
        if (registerNew == null) {
            return "error";
        } else {
            return "redirect:/";
        }
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("loginRequest", new UserEntity());
        return "index";
    }
    @PostMapping("/login")
    public String login(@ModelAttribute UserEntity userEntity) {
        System.out.println("login request: " + userEntity);
        UserEntity authenticateTheUser = usersService.authenticateUser(userEntity.getLogin(), userEntity.getPassword());
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
