package org.cpts422.Femininomenon.App.Service;

import org.cpts422.Femininomenon.App.Models.*;
import org.cpts422.Femininomenon.App.Repository.*;
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
    public InboxMessageService(InboxMessageRepository inboxMessageRepository, TransactionRepository transactionRepository, UserRuleService userRuleService) {
        this.inboxMessageRepository = inboxMessageRepository;
        this.transactionRepository = transactionRepository;
        this.userRuleService = userRuleService;
    }

    public List<InboxMessageModel> getInboxMessages(UserModel user) {
        return inboxMessageRepository.findByUser(user);
    }

    private void addMessage(UserModel user, String messageContent) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        boolean messageExists = inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                user, messageContent, oneDayAgo);

        if (!messageExists) {
            inboxMessageRepository.save(new InboxMessageModel(user, messageContent));
        }
    }

    public void markMessageAsRead(Long messageId) {
        InboxMessageModel message = inboxMessageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        message.setRead(true);
        inboxMessageRepository.save(message);
    }

    public void markAllMessagesAsRead(UserModel user) {
        List<InboxMessageModel> unreadMessages = inboxMessageRepository.findByUserAndIsReadFalse(user);
        unreadMessages.forEach(message -> message.setRead(true));
        inboxMessageRepository.saveAll(unreadMessages);
    }

    // Main spending rules check method - now acts as orchestrator
    public void checkSpendingRules(UserModel user) {
        List<UserRuleModel> rules = userRuleService.getRulesByUserLogin(user.getLogin());
        LocalDateTime now = LocalDateTime.now();

        rules.stream()
                .filter(rule -> rule.getCategory() != null)
                .forEach(rule -> processRule(user, rule, now));
    }

    // Individual rule processing
    private void processRule(UserModel user, UserRuleModel rule, LocalDateTime now) {
        LocalDateTime startDate = getStartDate(now, rule.getFrequency());
        List<TransactionModel> transactions = getTransactionsForPeriod(user.getLogin(), startDate, now);
        Map<TransactionModel.CategoryType, Double> categorySpending = calculateCategorySpending(transactions);

        switch (rule.getRuleType()) {
            case MAXIMUM_SPENDING:
                checkMaximumSpending(user, rule, categorySpending);
                break;
            case MINIMUM_SAVINGS:
                checkMinimumSavings(user, rule, transactions, categorySpending);
                break;
            case NOT_EXCEED_CATEGORY:
                checkCategoryComparison(user, rule, categorySpending);
                break;
        }
    }

    // Helper methods for rule processing
    private Map<TransactionModel.CategoryType, Double> calculateCategorySpending(List<TransactionModel> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        TransactionModel::getCategory,
                        Collectors.summingDouble(TransactionModel::getAmount)
                ));
    }

    private void checkMaximumSpending(UserModel user, UserRuleModel rule, Map<TransactionModel.CategoryType, Double> categorySpending) {
        double spending = categorySpending.getOrDefault(rule.getCategory(), 0.0);
        if (spending > rule.getLimitAmount()) {
            String message = String.format("Alert: You've exceeded your %s spending limit for %s. " + "Spent: $%.2f, Limit: $%.2f",
                    rule.getFrequency().toString().toLowerCase(),
                    rule.getCategory(), spending, rule.getLimitAmount());
            addMessage(user, message);
        }
    }

    private void checkMinimumSavings(UserModel user, UserRuleModel rule, List<TransactionModel> transactions, Map<TransactionModel.CategoryType, Double> categorySpending) {
        double totalIncome = calculateTotalIncome(transactions);
        double spending = categorySpending.getOrDefault(rule.getCategory(), 0.0);
        double savings = totalIncome - spending;

        if (savings < rule.getLimitAmount()) {
            String message = String.format("Alert: Your %s savings for %s are below the target. " + "Saved: $%.2f, Target: $%.2f",
                    rule.getFrequency().toString().toLowerCase(),
                    rule.getCategory(), savings, rule.getLimitAmount());
            addMessage(user, message);
        }
    }

    private void checkCategoryComparison(UserModel user, UserRuleModel rule, Map<TransactionModel.CategoryType, Double> categorySpending) {
        if (rule.getAdditionalCategory() != null) {
            double mainCategoryAmount = categorySpending.getOrDefault(rule.getCategory(), 0.0);
            double comparisonAmount = categorySpending.getOrDefault(rule.getAdditionalCategory(), 0.0);

            if (mainCategoryAmount > comparisonAmount) {
                String message = String.format("Alert: Your spending in %s ($%.2f) has exceeded your " +
                                "spending in the prioritized category %s ($%.2f).",
                        rule.getCategory(), mainCategoryAmount,
                        rule.getAdditionalCategory(), comparisonAmount);
                addMessage(user, message);
            }
        }
    }

    // Refactored overall overspending check
    public void checkForOverallOverspending(UserModel user) {
        LocalDateTime startOfMonth = getStartOfCurrentMonth();
        List<TransactionModel> transactions = getTransactionsForPeriod(
                user.getLogin(), startOfMonth, LocalDateTime.now());

        if (transactions.isEmpty()) {
            return;
        }

        checkMonthlyOverspending(user, transactions);
        checkLargeExpenses(user, transactions);
    }

    private void checkMonthlyOverspending(UserModel user, List<TransactionModel> transactions) {
        double totalIncome = calculateTotalIncome(transactions);
        double totalExpenses = calculateTotalExpenses(transactions);

        if (totalExpenses > totalIncome) {
            String message = totalIncome == 0
                    ? String.format("Alert: You have expenses ($%.2f) but no income recorded this month.", totalExpenses)
                    : String.format("Alert: Your expenses ($%.2f) have exceeded your income ($%.2f) this month.",
                    totalExpenses, totalIncome);
            addMessage(user, message);
        }
    }

    private void checkLargeExpenses(UserModel user, List<TransactionModel> transactions) {
        double totalIncome = calculateTotalIncome(transactions);
        double largestExpense = findLargestExpense(transactions);

        if (largestExpense > 0 && (totalIncome == 0 || largestExpense > totalIncome * 0.5)) {
            String message = totalIncome == 0
                    ? String.format("Alert: You have a large individual expense of $%.2f with no income recorded this month.",
                    largestExpense)
                    : String.format("Alert: You have a large individual expense of $%.2f, which is more than 50%% of your monthly income.",
                    largestExpense);
            addMessage(user, message);
        }
    }

    // Utility methods
    private LocalDateTime getStartDate(LocalDateTime now, UserRuleModel.Frequency frequency) {
        if (frequency == null) {
            throw new IllegalArgumentException("Frequency cannot be null");
        }
        switch (frequency) {
            case DAILY:
                return now.truncatedTo(ChronoUnit.DAYS);
            case WEEKLY:
                return now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            case MONTHLY:
                return now.with(TemporalAdjusters.firstDayOfMonth());
            default:
                throw new IllegalArgumentException("Unsupported frequency type: " + frequency);
        }
    }

    private LocalDateTime getStartOfCurrentMonth() {
        return LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    private List<TransactionModel> getTransactionsForPeriod(String userLogin, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByUserLoginAndDateBetween(userLogin, startDate, endDate);
    }

    private double calculateTotalIncome(List<TransactionModel> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionModel.TransactionType.INCOME)
                .mapToDouble(TransactionModel::getAmount)
                .sum();
    }

    private double calculateTotalExpenses(List<TransactionModel> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                .mapToDouble(TransactionModel::getAmount)
                .sum();
    }

    private double findLargestExpense(List<TransactionModel> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                .mapToDouble(TransactionModel::getAmount)
                .max()
                .orElse(0);
    }
}
