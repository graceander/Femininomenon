package org.cpts422.Femininomenon.App.Repository;

import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;


public interface TransactionRepository extends JpaRepository<TransactionModel, Long> {
    List<TransactionModel> findByUserLogin(String userLogin);
    List<TransactionModel> findByUserLoginAndDateBetween(String userLogin, LocalDateTime startDate, LocalDateTime endDate);

}