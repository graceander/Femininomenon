package org.cpts422.Femininomenon.App.Controllers;

import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.TransactionService;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class TransactionController {

    private final TransactionService transactionService;
    private final UsersService usersService;

    public TransactionController(TransactionService transactionService, UsersService usersService) {
        this.transactionService = transactionService;
        this.usersService = usersService;
    }



    @GetMapping("/home")
    public String homePage(String login, Model model) {
        UserModel user = usersService.findByLogin(login);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "error";
        }
        List<TransactionModel> transactions = transactionService.getTransactionsByUser(user.getLogin());

        if (transactions == null || transactions.isEmpty()) {
            model.addAttribute("message", "No transactions found for this user.");
        } else {
            model.addAttribute("transactions", transactions);
        }
        model.addAttribute("user", user);
        return "home";
    }

    @GetMapping("/addTransaction")
    public String AddTransactionPage( String login, Model model) {
        UserModel user = usersService.findByLogin(login);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "error";
        }
        model.addAttribute("user", user);
        return "addTransaction";
    }


    @PostMapping("/submitTransaction")
    public String submitTransaction( String login, float amount, String category,  String description,  TransactionModel.TransactionType type,  String account) {
        UserModel user = usersService.findByLogin(login);
        if (user == null)
        {
            return "error";
        }
        TransactionModel newTransaction = new TransactionModel(user, LocalDateTime.now(), amount, category, description, type, account);
        transactionService.saveTransaction(newTransaction);
        return "redirect:/home?login=" + login;
    }
    @PostMapping("/deleteTransaction")
    public String deleteTransaction( Long id, String login) {
        TransactionModel transaction = transactionService.getTransactionById(id);
        if (transaction != null && transaction.getUser().getLogin().equals(login)) {
            transactionService.removeTransaction(transaction);
        }
        return "redirect:/home?login=" + login;
    }

    @GetMapping("/editTransaction")
    public String editTransactionPage(Long id, String login, Model model) {
        UserModel user = usersService.findByLogin(login);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "error";
        }
        TransactionModel transaction = transactionService.getTransactionById(id);
        if (transaction == null || !transaction.getUser().getId().equals(user.getId())) {
            model.addAttribute("error", "Transaction does not belong to the user");
            return "error";
        }
        model.addAttribute("transaction", transaction);
        model.addAttribute("user", user);
        return "editTransaction";
    }





}
