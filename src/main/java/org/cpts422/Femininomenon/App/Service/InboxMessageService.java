package org.cpts422.Femininomenon.App.Service;

import org.cpts422.Femininomenon.App.Models.InboxMessageModel;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Models.UserRuleModel;
import org.cpts422.Femininomenon.App.Repository.InboxMessageRepository;
import org.cpts422.Femininomenon.App.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InboxMessageService {

    private final InboxMessageRepository inboxMessageRepository;
    private final TransactionRepository transactionRepository;
    private final UserRuleService userRuleService;

    @Autowired
    public InboxMessageService(InboxMessageRepository inboxMessageRepository,
                               TransactionRepository transactionRepository,
                               UserRuleService userRuleService) {
        this.inboxMessageRepository = inboxMessageRepository;
        this.transactionRepository = transactionRepository;
        this.userRuleService = userRuleService;
    }

    public List<InboxMessageModel> getInboxMessages(UserModel user) {
        return inboxMessageRepository.findByUser(user);
    }

    private void addMessage(UserModel user, String messageContent) {
        System.out.println("Adding message for user " + user.getLogin() + ": " + messageContent);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        boolean messageExists = inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(user, messageContent, oneDayAgo);

        if (!messageExists) {
            InboxMessageModel newMessage = new InboxMessageModel(user, messageContent);
            inboxMessageRepository.save(newMessage);
            System.out.println("New message added to inbox for user: " + user.getLogin());
        } else {
            System.out.println("Similar message already exists for user: " + user.getLogin() + ". Skipping.");
        }
    }

    public void markMessageAsRead(Long messageId) {
        InboxMessageModel message = inboxMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setRead(true);
        inboxMessageRepository.save(message);
    }

    public void markAllMessagesAsRead(UserModel user) {
        List<InboxMessageModel> unreadMessages = inboxMessageRepository.findByUserAndIsReadFalse(user);
        unreadMessages.forEach(message -> message.setRead(true));
        inboxMessageRepository.saveAll(unreadMessages);
    }

    // check for overspending
    public void checkSpendingRules(UserModel user) {
        List<UserRuleModel> rules = userRuleService.getRulesByUserLogin(user.getLogin());
        LocalDateTime now = LocalDateTime.now();

        for (UserRuleModel rule : rules) {
            if (rule.getCategory() == null) {
                continue; // Skip rules with null category
            }

            LocalDateTime startDate = getStartDate(now, rule.getFrequency());
            LocalDateTime endDate = now;

            List<TransactionModel> transactions = transactionRepository.findByUserLoginAndDateBetween(
                    user.getLogin(), startDate, endDate);

            Map<TransactionModel.CategoryType, Double> categorySpending = transactions.stream()
                    .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                    .collect(Collectors.groupingBy(
                            TransactionModel::getCategory,
                            Collectors.summingDouble(TransactionModel::getAmount)
                    ));

            double ruleAmount = categorySpending.getOrDefault(rule.getCategory(), 0.0);

            switch (rule.getRuleType()) {
                case MAXIMUM_SPENDING:
                    if (ruleAmount > rule.getLimitAmount()) {
                        String message = String.format("Alert: You've exceeded your %s spending limit for %s. Spent: $%.2f, Limit: $%.2f",
                                rule.getFrequency().toString().toLowerCase(), rule.getCategory(), ruleAmount, rule.getLimitAmount());
                        addMessage(user, message);
                    }
                    break;
                case MINIMUM_SAVINGS:
                    double totalIncome = transactions.stream()
                            .filter(t -> t.getType() == TransactionModel.TransactionType.INCOME)
                            .mapToDouble(TransactionModel::getAmount)
                            .sum();
                    double savings = totalIncome - ruleAmount;
                    if (savings < rule.getLimitAmount()) {
                        String message = String.format("Alert: Your %s savings for %s are below the target. Saved: $%.2f, Target: $%.2f",
                                rule.getFrequency().toString().toLowerCase(), rule.getCategory(), savings, rule.getLimitAmount());
                        addMessage(user, message);
                    }
                    break;
                case NOT_EXCEED_CATEGORY:
                    if (rule.getAdditionalCategory() != null) {
                        double comparisonAmount = categorySpending.getOrDefault(rule.getAdditionalCategory(), 0.0);
                        if (ruleAmount > comparisonAmount) {
                            String message = String.format("Alert: Your spending in %s ($%.2f) has exceeded your spending in the prioritized category %s ($%.2f).",
                                    rule.getCategory(), ruleAmount, rule.getAdditionalCategory(), comparisonAmount);
                            addMessage(user, message);
                        }
                    }
                    break;
            }
        }
    }

    private LocalDateTime getStartDate(LocalDateTime now, UserRuleModel.Frequency frequency) {
        switch (frequency) {
            case DAILY:
                return now.truncatedTo(ChronoUnit.DAYS);
            case WEEKLY:
                return now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            case MONTHLY:
                return now.with(TemporalAdjusters.firstDayOfMonth());
            default:
                throw new IllegalArgumentException("Unsupported frequency type");
        }
    }

    public void checkForOverallOverspending(UserModel user) {
        System.out.println("Starting overspending check for user: " + user.getLogin());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        List<TransactionModel> transactions = transactionRepository.findByUserLoginAndDateBetween(
                user.getLogin(), startOfMonth, endOfMonth);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found for user: " + user.getLogin() + " in the current month.");
            return; // Exit the method if there are no transactions
        }

        double totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionModel.TransactionType.INCOME)
                .mapToDouble(TransactionModel::getAmount)
                .sum();

        double totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                .mapToDouble(TransactionModel::getAmount)
                .sum();

        System.out.println("User: " + user.getLogin() + ", Total Income: " + totalIncome + ", Total Expenses: " + totalExpenses);

        if (totalExpenses > totalIncome) {
            String message;
            if (totalIncome == 0) {
                message = String.format("Alert: You have expenses ($%.2f) but no income recorded this month.", totalExpenses);
            } else {
                message = String.format("Alert: Your expenses ($%.2f) have exceeded your income ($%.2f) this month.",
                        totalExpenses, totalIncome);
            }
            addMessage(user, message);
            System.out.println("Overspending alert created for user: " + user.getLogin());
        } else {
            System.out.println("No overspending detected for user: " + user.getLogin());
        }

        // Check for large individual expenses
        double largestExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                .mapToDouble(TransactionModel::getAmount)
                .max()
                .orElse(0);

        if (totalIncome > 0 && largestExpense > totalIncome * 0.5) {
            String message = String.format("Alert: You have a large individual expense of $%.2f, which is more than 50%% of your monthly income.",
                    largestExpense);
            addMessage(user, message);
            System.out.println("Large expense alert created for user: " + user.getLogin());
        } else if (totalIncome == 0 && largestExpense > 0) {
            String message = String.format("Alert: You have a large individual expense of $%.2f with no income recorded this month.",
                    largestExpense);
            addMessage(user, message);
            System.out.println("Large expense alert created for user: " + user.getLogin());
        }
    }
}
