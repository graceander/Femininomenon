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

    public void addMessage(UserModel user, String messageContent) {
        // Check if a similar message already exists for this user within the last 24 hours
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        boolean messageExists = inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(user, messageContent, oneDayAgo);

        if (!messageExists) {
            InboxMessageModel newMessage = new InboxMessageModel(user, messageContent);
            inboxMessageRepository.save(newMessage);
        }
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

            double totalAmount = transactions.stream()
                    .filter(t -> t.getCategory() == rule.getCategory())
                    .filter(t -> t.getType() == TransactionModel.TransactionType.EXPENSE)
                    .mapToDouble(TransactionModel::getAmount)
                    .sum();

            if (rule.getRuleType() == UserRuleModel.RuleType.MAXIMUM_SPENDING && totalAmount > rule.getLimitAmount()) {
                String message = String.format("Alert: You've exceeded your %s spending limit for %s. Spent: $%.2f, Limit: $%.2f",
                        rule.getFrequency().toString().toLowerCase(), rule.getCategory(), totalAmount, rule.getLimitAmount());
                addMessage(user, message);
            } else if (rule.getRuleType() == UserRuleModel.RuleType.MINIMUM_SAVINGS) {
                double savings = transactions.stream()
                        .filter(t -> t.getType() == TransactionModel.TransactionType.INCOME)
                        .mapToDouble(TransactionModel::getAmount)
                        .sum() - totalAmount;

                if (savings < rule.getLimitAmount()) {
                    String message = String.format("Alert: Your %s savings for %s are below the target. Saved: $%.2f, Target: $%.2f",
                            rule.getFrequency().toString().toLowerCase(), rule.getCategory(), savings, rule.getLimitAmount());
                    addMessage(user, message);
                }
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
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
            addMessage(user, message);
        }
    }
}
