package org.cpts422.Femininomenon.App.Service;

import org.cpts422.Femininomenon.App.Models.InboxMessageModel;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Repository.InboxMessageRepository;
import org.cpts422.Femininomenon.App.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InboxMessageService {

    private final InboxMessageRepository inboxMessageRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public InboxMessageService(InboxMessageRepository inboxMessageRepository, TransactionRepository transactionRepository) {
        this.inboxMessageRepository = inboxMessageRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<InboxMessageModel> getInboxMessages(UserModel user) {
        return inboxMessageRepository.findByUser(user);
    }

    public void addMessage(InboxMessageModel message) {
        inboxMessageRepository.save(message);
    }
    // check for overspending
    public void checkForOverspending(UserModel user, Map<TransactionModel.CategoryType, Double> spendingLimits) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        List<TransactionModel> transactions = transactionRepository.findByUserLogin(user.getLogin());

        Map<TransactionModel.CategoryType, Double> totalSpending = transactions.stream()
                .filter(t -> t.getDate().isAfter(startOfMonth) && t.getDate().isBefore(endOfMonth))
                .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        TransactionModel::getCategory,
                        Collectors.summingDouble(TransactionModel::getAmount)
                ));

        for (Map.Entry<TransactionModel.CategoryType, Double> entry : totalSpending.entrySet()) {
            TransactionModel.CategoryType category = entry.getKey();
            Double spent = entry.getValue();
            Double limit = spendingLimits.get(category);

            if (limit != null && spent > limit) {
                String message = String.format("Alert: You've exceeded your spending limit for %s. Spent: $%.2f, Limit: $%.2f",
                        category.name(), spent, limit);
                addMessage(new InboxMessageModel(user, message));
            }
        }
    }

    public void checkForOverallOverspending(UserModel user) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        List<TransactionModel> transactions = transactionRepository.findByUserLogin(user.getLogin());

        double totalIncome = transactions.stream()
                .filter(t -> t.getDate().isAfter(startOfMonth) && t.getDate().isBefore(endOfMonth))
                .filter(t -> t.getType() == TransactionModel.TransactionType.INCOME)
                .mapToDouble(TransactionModel::getAmount)
                .sum();

        double totalExpenses = transactions.stream()
                .filter(t -> t.getDate().isAfter(startOfMonth) && t.getDate().isBefore(endOfMonth))
                .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                .mapToDouble(TransactionModel::getAmount)
                .sum();

        if (totalExpenses > totalIncome) {
            String message = String.format("Alert: Your expenses ($%.2f) have exceeded your income ($%.2f) this month.",
                    totalExpenses, totalIncome);
            addMessage(new InboxMessageModel(user, message));
        }
    }
}
