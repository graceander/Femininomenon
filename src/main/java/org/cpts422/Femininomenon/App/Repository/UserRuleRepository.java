package org.cpts422.Femininomenon.App.Repository;

import org.cpts422.Femininomenon.App.Models.UserRuleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRuleRepository extends JpaRepository<UserRuleModel, Long> {
    List<UserRuleModel> findByUserLogin(String userLogin);


}
