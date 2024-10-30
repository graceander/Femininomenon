package org.cpts422.Femininomenon.App.Controllers;

import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.CurrencyConversionService;
import org.cpts422.Femininomenon.App.Service.TransactionService;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class TransactionController {

    private final TransactionService transactionService;
    private final UsersService usersService;
    private final CurrencyConversionService currencyConversionService;

    @Autowired
    public TransactionController(TransactionService transactionService, UsersService usersService, CurrencyConversionService currencyConversionService) {
        this.transactionService = transactionService;
        this.usersService = usersService;
        this.currencyConversionService = currencyConversionService;
    }

    @GetMapping("/goHome")
    public String goHome(@RequestParam("login") String login, Model model) {
        return "redirect:/home?login=" + login;
    }

    @PostMapping("/changeCurrency")
    public String changeCurrency(@RequestParam("userLogin") String userLogin,
                                 @RequestParam("newCurrency") String newCurrency) {
        UserModel user = usersService.findByLogin(userLogin);
        if (user != null) {
            user.setCurrency(newCurrency);
            usersService.saveUser(user);
            System.out.println("Currency updated to " + newCurrency + " for user: " + userLogin);
        } else {
            System.out.println("User not found: " + userLogin);
        }
        return "redirect:/home?login=" + userLogin;
    }

    @GetMapping("/home")
    public String homePage(@RequestParam("login") String login,
                           @RequestParam(value = "period", defaultValue = "overall") String period,
                           Model model) {
        UserModel user = usersService.findByLogin(login);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "error";
        }

        Map<TransactionModel.CategoryType, Double> spendingByCategory = transactionService.getSpendingByCategory(login, period);
        double totalSpending = transactionService.getTotalSpending(login, period);
        List<TransactionModel> transactions = transactionService.getTransactionsByUser(login);

        // Convert amounts to user's currency
        String userCurrency = user.getCurrency();
        spendingByCategory = spendingByCategory.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> currencyConversionService.convert(e.getValue(), "USD", userCurrency)
                ));
        totalSpending = currencyConversionService.convert(totalSpending, "USD", userCurrency);

        transactions.forEach(t -> t.setAmount(
                (float) currencyConversionService.convert(t.getAmount(), "USD", userCurrency)
        ));

        model.addAttribute("user", user);
        model.addAttribute("spendingByCategory", spendingByCategory);
        model.addAttribute("totalSpending", totalSpending);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("transactions", transactions);

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

        TransactionModel.CategoryType categoryType;

        categoryType = TransactionModel.CategoryType.valueOf(category.toUpperCase().replace(" ", "_"));

        TransactionModel newTransaction = new TransactionModel(user, LocalDateTime.now(), amount, categoryType, description, type, account);
        transactionService.saveTransaction(newTransaction);
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

    @PostMapping("/deleteTransaction")
    public String deleteTransaction( Long id, String login) {
        TransactionModel transaction = transactionService.getTransactionById(id);
        if (transaction != null && transaction.getUser().getLogin().equals(login)) {
            transactionService.removeTransaction(transaction);
        }
        return "redirect:/home?login=" + login;
    }

    @PostMapping("/updateTransaction")
    public String updateTransaction( Long id, String login,  String date, float amount,  String category, String description,  TransactionModel.TransactionType type, String account, Model model) {

        // get the transaction ID
        TransactionModel transaction = transactionService.getTransactionById(id);
        if (transaction == null || !transaction.getUser().getLogin().equals(login)) {
            model.addAttribute("error", "Transaction does not belong to the user");
            return "error";
        }

        TransactionModel.CategoryType categoryType;
        try {
            categoryType = TransactionModel.CategoryType.valueOf(category.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Handle case where the category does not match any enum values
            model.addAttribute("error", "Invalid category: " + category);
            return "error";
        }

        transaction.setDate(LocalDateTime.parse(date));
        transaction.setAmount(amount);
        transaction.setCategory(categoryType);
        transaction.setDescription(description);
        transaction.setType(type);
        transaction.setAccount(account);
        transactionService.saveTransaction(transaction);

        return "redirect:/home?login=" + login;
    }






}
