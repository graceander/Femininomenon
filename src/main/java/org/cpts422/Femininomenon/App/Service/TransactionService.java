package org.cpts422.Femininomenon.App.Service;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final InboxMessageService inboxMessageService;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, InboxMessageService inboxMessageService) {
        this.transactionRepository = transactionRepository;
        this.inboxMessageService = inboxMessageService;
    }

    public void saveTransaction(TransactionModel transaction) {
        transactionRepository.save(transaction);

        // Check for overspending after saving the transaction
        UserModel user = transaction.getUser();
        inboxMessageService.checkSpendingRules(user);
        inboxMessageService.checkForOverallOverspending(user);
    }

    public List<TransactionModel> getTransactionsByUser(String login) {
        return transactionRepository.findByUserLogin(login);
    }

    public void removeTransaction(TransactionModel transaction) {
        transactionRepository.delete(transaction);
    }

    // Method to get a transaction by ID
    public TransactionModel getTransactionById(Long id) {
        Optional<TransactionModel> transaction = transactionRepository.findById(id);
        return transaction.orElse(null);
    }

    public float getTotalSpendingForMonth(String login, int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        return getTransactionsByUser(login).stream()
                .filter(t -> t.getDate().isAfter(startOfMonth) && t.getDate().isBefore(endOfMonth))
                .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                .map(TransactionModel::getAmount)
                .reduce(0f, Float::sum);
    }

    public Map<String, Float> getSpendingByCategory(String login, int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        return getTransactionsByUser(login).stream()
                .filter(t -> t.getDate().isAfter(startOfMonth) && t.getDate().isBefore(endOfMonth))
                .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().name(), // Convert enum to String
                        Collectors.summingDouble(TransactionModel::getAmount)
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().floatValue()
                ));
    }
}

