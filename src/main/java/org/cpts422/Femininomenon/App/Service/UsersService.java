package org.cpts422.Femininomenon.App.Service;

import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UsersService {

    private final UserRepository userRepository;

    public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserModel registerUser(String firstName, String lastName, String login, String password, String email) {
        if (login != null && password != null) {
            UserModel usersModel = new UserModel();
            usersModel.setFirstName(firstName);
            usersModel.setLastName(lastName);
            usersModel.setLogin(login);
            usersModel.setPassword(password);
            usersModel.setEmail(email);
            UserModel savedUser = userRepository.save(usersModel);
            System.out.println("User registered: " + savedUser);
            return savedUser;
        } else {
            return null;
        }
    }

        public UserModel authenticateUser(String login, String password) {
        return userRepository.findByLoginAndPassword(login, password).orElse(null);
    }

    public UserModel findByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

}
