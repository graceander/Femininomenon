package org.cpts422.Femininomenon.App.Service;

import org.apache.catalina.User;
import org.cpts422.Femininomenon.App.Entity.UserEntity;
import org.cpts422.Femininomenon.App.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsersService {



    private final UserRepository userRepository;

    public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity registerUser(String login, String password, String email) {
        if (login != null && password != null) {
            UserEntity usersEntity = new UserEntity();
            usersEntity.setLogin(login);
            usersEntity.setPassword(password);
            usersEntity.setEmail(email);
            UserEntity savedUser = userRepository.save(usersEntity);
            System.out.println("User registered: " + savedUser);
            return savedUser;
        } else {
            return null;
        }
    }


    public UserEntity authenticateUser(String login, String password) {
        return userRepository.findByLoginAndPassword(login, password).orElse(null);
    }

}
