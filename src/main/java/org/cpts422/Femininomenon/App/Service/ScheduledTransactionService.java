package org.cpts422.Femininomenon.App.Service;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.ScheduledTransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Repository.ScheduledTransactionRepository;
import org.cpts422.Femininomenon.App.Utils.BankHolidays;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Optional;


@Service
public class ScheduledTransactionService {

    private final ScheduledTransactionRepository scheduledTransactionRepository;

    public ScheduledTransactionService(ScheduledTransactionRepository scheduledTransactionRepository) {
        this.scheduledTransactionRepository = scheduledTransactionRepository;
    }

    public List<ScheduledTransactionModel> getTransactionsByUser(String login) {
        return scheduledTransactionRepository.findByUserLogin(login);
    }

    public void saveTransaction(ScheduledTransactionModel scheduledTransaction) {
        scheduledTransactionRepository.save(scheduledTransaction);
    }

    public void removeTransaction(ScheduledTransactionModel scheduledTransaction) {
        scheduledTransactionRepository.delete(scheduledTransaction);
    }

    // Method to get a scheduled transaction by ID
    public ScheduledTransactionModel getTransactionById(Long id) {
        Optional<ScheduledTransactionModel> scheduledTransaction = scheduledTransactionRepository.findById(id);
        return scheduledTransaction.orElse(null);
    }

    // Method to determine the date of the next scheduled payment
    public LocalDateTime findNextPaymentDate(ScheduledTransactionModel scheduledTransaction) {
        String frequency = scheduledTransaction.getFrequency();
        LocalDateTime recentPayment = scheduledTransaction.getRecentPayment();
        if (frequency.equals("monthly") || frequency.equals("custom")) {
            return recentPayment.plusMonths(1); // increments month
        }
        else if (frequency.equals("weekly")) {
            return recentPayment.plusWeeks(1); // increments week by 1
        }
        else if (frequency.equals("biweekly")) {
            return recentPayment.plusWeeks(2); // increments week by 2
        }
        else {
            throw new IllegalArgumentException("Illegal Argument: Invalid selection for scheduled payment frequency");
        }
    }

    public TransactionModel onCreateScheduledTransaction(ScheduledTransactionModel scheduledTransaction) {

        TransactionModel.TransactionType newTransactionType = null;
        if (scheduledTransaction.getType() == ScheduledTransactionModel.TransactionType.INCOME) {
            newTransactionType = TransactionModel.TransactionType.INCOME;
        }
        else {
            newTransactionType = TransactionModel.TransactionType.EXPENSE;
        }

        LocalDateTime adjustedPaymentDate = BankHolidays.adjustForBankClosures(scheduledTransaction.getRecentPayment());


        TransactionModel newTransaction = new TransactionModel(
                scheduledTransaction.getUser(),
                adjustedPaymentDate,
                scheduledTransaction.getAmount(),
                scheduledTransaction.getCategory(),
                scheduledTransaction.getDescription(),
                newTransactionType,
                scheduledTransaction.getAccount());

        return newTransaction;
    }

    // Method to create a new transaction using the scheduled information
    public TransactionModel createTransaction(ScheduledTransactionModel scheduledTransaction) {
        LocalDateTime nextPaymentDate = findNextPaymentDate(scheduledTransaction);
        LocalDateTime adjustedPaymentDate = BankHolidays.adjustForBankClosures(nextPaymentDate);

        TransactionModel.TransactionType newTransactionType = null;
        if (scheduledTransaction.getType() == ScheduledTransactionModel.TransactionType.INCOME) {
            newTransactionType = TransactionModel.TransactionType.INCOME;
        }
        else {
            newTransactionType = TransactionModel.TransactionType.EXPENSE;
        }

        TransactionModel newTransaction = new TransactionModel(
                scheduledTransaction.getUser(),
                adjustedPaymentDate,
                scheduledTransaction.getAmount(),
                scheduledTransaction.getCategory(),
                scheduledTransaction.getDescription(),
                newTransactionType,
                scheduledTransaction.getAccount());

        if (newTransaction != null) {
            scheduledTransaction.setRecentPayment(nextPaymentDate);
            return newTransaction;
        }
        else {
            try {
                throw new Exception("Error creating new transaction");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

