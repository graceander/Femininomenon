package org.cpts422.Femininomenon.App.Repository;

import org.cpts422.Femininomenon.App.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByLoginAndPassword(String login, String password);
}
