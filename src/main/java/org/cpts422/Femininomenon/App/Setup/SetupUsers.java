package org.cpts422.Femininomenon.App.Setup;
import jakarta.annotation.PostConstruct;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.springframework.stereotype.Component;

@Component
public class SetupUsers {

    private final UsersService usersService;
    public SetupUsers(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostConstruct
    public void loadData() {
        usersService.registerUser("Matthew", "Pham", "matthew", "matthew", "OrangeCats@gmail.com");
        usersService.registerUser("Grace", "Anderson", "grace", "grace", "Cats@gmail.com");
        usersService.registerUser("Briana", "Briana", "briana", "briana", "Cow@gmail.com");
        System.out.println("All the users are registered.");
    }
}
