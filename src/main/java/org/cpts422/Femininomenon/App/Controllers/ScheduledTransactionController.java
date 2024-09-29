package org.cpts422.Femininomenon.App.Controllers;

import org.cpts422.Femininomenon.App.Models.ScheduledTransactionModel;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.ScheduledTransactionService;
import org.cpts422.Femininomenon.App.Service.TransactionService;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ScheduledTransactionController {

    private final ScheduledTransactionService scheduledTransactionService;
    private final UsersService usersService;
    private final TransactionService transactionService;

    public ScheduledTransactionController(ScheduledTransactionService scheduledTransactionService, UsersService usersService, TransactionService transactionService) {
        this.scheduledTransactionService = scheduledTransactionService;
        this.usersService = usersService;
        this.transactionService = transactionService;
    }

    @GetMapping("/viewScheduledTransactions")
    public String ViewScheduledTransactionsPage( String login, Model model) {
        UserModel user = usersService.findByLogin(login);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "error";
        }
        model.addAttribute("user", user);
        List<ScheduledTransactionModel> scheduledTransactions = scheduledTransactionService.getTransactionsByUser(user.getLogin());

        if (scheduledTransactions == null || scheduledTransactions.isEmpty()) {
            model.addAttribute("message", "No scheduled transactions found for this user.");
        } else {
            model.addAttribute("scheduledTransactions", scheduledTransactions);
        }
        model.addAttribute("user", user);
        return "viewScheduledTransactions";
    }

    @GetMapping("/addScheduledTransaction")
    public String AddScheduledTransactionPage( String login, Model model) {
        UserModel user = usersService.findByLogin(login);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "error";
        }
        model.addAttribute("user", user);
        return "addScheduledTransaction";
    }


    @PostMapping("/submitScheduledTransaction")
    public String submitScheduledTransaction( String login, String frequency, LocalDateTime recentPayment, float amount, String category, String description,  ScheduledTransactionModel.TransactionType type,  String account) {
        UserModel user = usersService.findByLogin(login);
        if (user == null)
        {
            return "error";
        }

        ScheduledTransactionModel.CategoryType categoryType;
        try {
            categoryType = ScheduledTransactionModel.CategoryType.valueOf(category.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Handle case where the category does not match any enum values
            System.out.println("Invalid category: " + category);
            return "error";
        }

        ScheduledTransactionModel newScheduledTransaction = new ScheduledTransactionModel(
                user,
                frequency,
                recentPayment,
                amount,
                categoryType,
                description,
                type,
                account);
        scheduledTransactionService.saveTransaction(newScheduledTransaction);
        TransactionModel initialTransaction = scheduledTransactionService.onCreateScheduledTransaction(newScheduledTransaction);
        transactionService.saveTransaction(initialTransaction);
        return "redirect:/viewScheduledTransactions?login=" + login;
    }


    @GetMapping("/editScheduledTransaction")
    public String editTransactionPage(Long id, String login, Model model) {
        UserModel user = usersService.findByLogin(login);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "error";
        }
        ScheduledTransactionModel scheduledTransaction = scheduledTransactionService.getTransactionById(id);
        if (scheduledTransaction == null || !scheduledTransaction.getUser().getId().equals(user.getId())) {
            model.addAttribute("error", "Transaction does not belong to the user");
            return "error";
        }
        model.addAttribute("scheduledTransaction", scheduledTransaction);
        model.addAttribute("user", user);
        return "editScheduledTransaction";
    }

    @PostMapping("/deleteScheduledTransaction")
    public String deleteScheduledTransaction( Long id, String login) {
        ScheduledTransactionModel scheduledTransaction = scheduledTransactionService.getTransactionById(id);
        if (scheduledTransaction != null && scheduledTransaction.getUser().getLogin().equals(login)) {
            scheduledTransactionService.removeTransaction(scheduledTransaction);
        }
        return "redirect:/viewScheduledTransactions?login=" + login;
    }

    @PostMapping("/updateScheduledTransaction")
    public String updateScheduledTransaction( Long id, String login, String frequency, float amount, String category, String description,  ScheduledTransactionModel.TransactionType type, String account, Model model) {

        // get the transaction ID
        ScheduledTransactionModel scheduledTransaction = scheduledTransactionService.getTransactionById(id);
        if (scheduledTransaction == null || !scheduledTransaction.getUser().getLogin().equals(login)) {
            model.addAttribute("error", "Transaction noes not belong to the user");
            return "error";
        }

        ScheduledTransactionModel.CategoryType categoryType;
        try {
            categoryType = ScheduledTransactionModel.CategoryType.valueOf(category.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Handle case where the category does not match any enum values
            System.out.println("Invalid category: " + category);
            return "error";
        }

        scheduledTransaction.setFrequency(frequency);
        scheduledTransaction.setAmount(amount);
        scheduledTransaction.setCategory(categoryType);
        scheduledTransaction.setDescription(description);
        scheduledTransaction.setType(type);
        scheduledTransaction.setAccount(account);
        scheduledTransactionService.saveTransaction(scheduledTransaction);

        return "redirect:/viewScheduledTransactions?login=" + login;
    }






}
