package org.cpts422.Femininomenon.App.Service;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserRuleModel;
import org.cpts422.Femininomenon.App.Repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionModel> getTransactionsByUser(String login) {
        return transactionRepository.findByUserLogin(login);
    }

    public void saveTransaction(TransactionModel transaction) {
        transactionRepository.save(transaction);
    }

    public void removeTransaction(TransactionModel transaction) {
        transactionRepository.delete(transaction);
    }

    // Method to get a transaction by ID
    public TransactionModel getTransactionById(Long id) {
        Optional<TransactionModel> transaction = transactionRepository.findById(id);
        return transaction.orElse(null);
    }


}

