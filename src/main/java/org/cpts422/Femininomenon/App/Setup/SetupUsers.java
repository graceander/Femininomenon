package org.cpts422.Femininomenon.App.Setup;

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



    }


}
