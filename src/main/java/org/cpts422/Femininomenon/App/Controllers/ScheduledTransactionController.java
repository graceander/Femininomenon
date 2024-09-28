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
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ScheduledTransactionController {

    private final ScheduledTransactionService scheduledTransactionService;
//    private final TransactionService transactionService;
    private final UsersService usersService;
    private final TransactionService transactionService;

    public ScheduledTransactionController(ScheduledTransactionService scheduledTransactionService, UsersService usersService, TransactionService transactionService) {
        this.scheduledTransactionService = scheduledTransactionService;
        // this.transactionService = transactionService;
        this.usersService = usersService;
        this.transactionService = transactionService;
    }
//    @GetMapping("/goHome")
//    public String goHome(@RequestParam("login") String login, Model model) {
//        return "redirect:/home?login=" + login;
//    }


//    @GetMapping("/home")
//    public String homePage(String login, Model model) {
//        UserModel user = usersService.findByLogin(login);
//        if (user == null) {
//            model.addAttribute("error", "User not found");
//            return "error";
//        }
//        List<TransactionModel> transactions = transactionService.getTransactionsByUser(user.getLogin());
//
//        if (transactions == null || transactions.isEmpty()) {
//            model.addAttribute("message", "No transactions found for this user.");
//        } else {
//            model.addAttribute("transactions", transactions);
//        }
//        model.addAttribute("user", user);
//        return "home";
//    }

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

        ScheduledTransactionModel newScheduledTransaction = new ScheduledTransactionModel(
                user,
                frequency,
                recentPayment,
                amount,
                category,
                description,
                type,
                account);
        scheduledTransactionService.saveTransaction(newScheduledTransaction);
        TransactionModel initialTransaction = scheduledTransactionService.onCreateScheduledTransaction(newScheduledTransaction);
        transactionService.saveTransaction(initialTransaction);
        return "redirect:/home?login=" + login;
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
        return "redirect:/home?login=" + login;
    }

    @PostMapping("/updateScheduledTransaction")
    public String updateScheduledTransaction( Long id, String login, String frequency, float amount,  String category, String description,  ScheduledTransactionModel.TransactionType type, String account, Model model) {

        // get the transaction ID
        ScheduledTransactionModel scheduledTransaction = scheduledTransactionService.getTransactionById(id);
        if (scheduledTransaction == null || !scheduledTransaction.getUser().getLogin().equals(login)) {
            model.addAttribute("error", "Transaction noes not belong to the user");
            return "error";
        }

        scheduledTransaction.setFrequency(frequency);
        scheduledTransaction.setAmount(amount);
        scheduledTransaction.setCategory(category);
        scheduledTransaction.setDescription(description);
        scheduledTransaction.setType(type);
        scheduledTransaction.setAccount(account);
        scheduledTransactionService.saveTransaction(scheduledTransaction);

        return "redirect:/home?login=" + login;
    }






}
