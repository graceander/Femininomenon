package org.cpts422.Femininomenon.App.Setup;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import jakarta.annotation.PostConstruct;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.TransactionService;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Component
public class SetupUsers {

    private final UsersService usersService;
    private final TransactionService transactionService;

    public SetupUsers(UsersService usersService, TransactionService transactionService) {
        this.usersService = usersService;
        this.transactionService = transactionService;
    }

    @PostConstruct
    public void loadData() {
        // Register users
        UserModel userMatthew = usersService.registerUser("Matthew", "Pham", "matthew", "matthew", "OrangeCats@gmail.com");
        UserModel userGrace = usersService.registerUser("Grace", "Anderson", "grace", "grace", "Cats@gmail.com");
        UserModel userBriana = usersService.registerUser("Briana", "Briana", "briana", "briana", "Cow@gmail.com");

        System.out.println("All the users are registered.");
        addTransactionsToUser(userMatthew);
        addTransactionsToUser(userGrace);
        addTransactionsToUser(userBriana);

    }

    public void addTransactionsToUser(UserModel user) {
        String line;
        String splitSep = ",";
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        try {
            File file = new File("src/main/java/org/cpts422/Femininomenon/App/Setup/Transaction1.csv");
            BufferedReader br = new BufferedReader(new FileReader(file));
            //skip the header of the csv
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] transactionData = line.split(splitSep);
                LocalDateTime date = LocalDateTime.parse(transactionData[2], formatter);
                float amount = Float.parseFloat(transactionData[3]);
                String category = transactionData[4];
                String description = transactionData[5];
                TransactionModel.TransactionType type = TransactionModel.TransactionType.valueOf(transactionData[6].toUpperCase());
                String account = transactionData[7];

                TransactionModel newTransaction = new TransactionModel(user, date, amount, category, description, type, account);
                transactionService.saveTransaction(newTransaction);
            }
            System.out.println("Transactions successfully loaded for all users");
        } catch (IOException e) {
            System.out.println("Error loading transactions: " + e.getMessage());
        }
    }

}
