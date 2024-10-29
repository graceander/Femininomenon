package org.cpts422.Femininomenon.App.Repository;

import org.cpts422.Femininomenon.App.Models.ScheduledTransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransactionModel, Long> {
    List<ScheduledTransactionModel> findByUserLogin(String userLogin);

}