package org.cpts422.Femininomenon.App.Repository;

import org.cpts422.Femininomenon.App.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
    Optional<UserModel> findByLoginAndPassword(String login, String password);
    Optional<UserModel> findByLogin(String login);
}
