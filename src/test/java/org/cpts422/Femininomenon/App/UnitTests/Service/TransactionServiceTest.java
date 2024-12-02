/**
 * Grace Anderson
 * 11759304
 */
package org.cpts422.Femininomenon.App.UnitTests.Service;

import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Repository.TransactionRepository;
import org.cpts422.Femininomenon.App.Service.InboxMessageService;
import org.cpts422.Femininomenon.App.Service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testing for TransactionService class.
 * Tests cover transaction management, spending calculations, and category-based analysis.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InboxMessageService inboxMessageService;

    private TransactionService transactionService;

    /**
     * Sets up the test environment before each test.
     * Initializes a new TransactionService with mocked dependencies.
     */
    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, inboxMessageService);
    }

    /**
     * Tests monthly category spending calculation for a single category.
     * Verifies that transactions within the same category are correctly summed.
     */
    @Test
    void getSpendingByCategory_MonthlyBreakdown_CalculatesCorrectly() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        LocalDateTime withinMonth = LocalDateTime.of(2024, 3, 15, 12, 0);
        TransactionModel transaction1 = createExpenseTransaction(100.0f, "GROCERIES", withinMonth);
        TransactionModel transaction2 = createExpenseTransaction(50.0f, "GROCERIES", withinMonth);

        when(transactionRepository.findByUserLogin(login))
                .thenReturn(Arrays.asList(transaction1, transaction2));

        Map<String, Float> result = transactionService.getSpendingByCategory(login, year, month);

        assertNotNull(result, "Result map should not be null");
        assertTrue(result.containsKey("GROCERIES"), "Should contain GROCERIES category");
        assertEquals(150.0f, result.get("GROCERIES"), 0.001f);
    }

    /**
     * Tests spending calculation across multiple categories.
     * Verifies that transactions are correctly grouped and summed by category.
     */
    @Test
    void getSpendingByCategory_MultipleCategories_CalculatesCorrectly() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        LocalDateTime withinMonth = LocalDateTime.of(2024, 3, 15, 12, 0);
        TransactionModel transaction1 = createExpenseTransaction(100.0f, "GROCERIES", withinMonth);
        TransactionModel transaction2 = createExpenseTransaction(50.0f, "ENTERTAINMENT", withinMonth);
        TransactionModel transaction3 = createExpenseTransaction(75.0f, "GROCERIES", withinMonth);

        when(transactionRepository.findByUserLogin(login))
                .thenReturn(Arrays.asList(transaction1, transaction2, transaction3));

        Map<String, Float> result = transactionService.getSpendingByCategory(login, year, month);

        assertNotNull(result, "Result map should not be null");
        assertTrue(result.containsKey("GROCERIES"), "Should contain GROCERIES category");
        assertTrue(result.containsKey("ENTERTAINMENT"), "Should contain ENTERTAINMENT category");
        assertEquals(175.0f, result.get("GROCERIES"), 0.001f);
        assertEquals(50.0f, result.get("ENTERTAINMENT"), 0.001f);
    }

    /**
     * Tests behavior when no transactions exist.
     * Verifies that an empty map is returned rather than null.
     */
    @Test
    void getSpendingByCategory_EmptyTransactions_ReturnsEmptyMap() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        when(transactionRepository.findByUserLogin(login)).thenReturn(List.of());

        Map<String, Float> result = transactionService.getSpendingByCategory(login, year, month);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty");
    }

    /**
     * Tests monthly total spending calculation.
     * Verifies that all expenses within the month are correctly summed.
     */
    @Test
    void getTotalSpendingForMonth_CalculatesCorrectly() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        LocalDateTime withinMonth = LocalDateTime.of(2024, 3, 15, 12, 0);
        TransactionModel expense1 = createExpenseTransaction(100.0f, "GROCERIES", withinMonth);
        TransactionModel expense2 = createExpenseTransaction(50.0f, "ENTERTAINMENT", withinMonth);

        when(transactionRepository.findByUserLogin(login))
                .thenReturn(Arrays.asList(expense1, expense2));

        float totalSpending = transactionService.getTotalSpendingForMonth(login, year, month);
        assertEquals(150.0f, totalSpending, 0.001f);
    }

    /**
     * Tests spending calculation for different time periods.
     * Verifies that the same transaction is correctly counted in different period contexts.
     */
    @Test
    void getSpendingByPeriod_HandlesDifferentPeriods() {
        String userLogin = "testUser";
        TransactionModel expense = createExpenseTransaction(100.0f, "GROCERIES", LocalDateTime.now());

        when(transactionRepository.findByUserLoginAndDateBetween(eq(userLogin), any(), any()))
                .thenReturn(List.of(expense));

        // Test different periods
        Map<TransactionModel.CategoryType, Double> dayResult = transactionService.getSpendingByCategory(userLogin, "day");
        Map<TransactionModel.CategoryType, Double> weekResult = transactionService.getSpendingByCategory(userLogin, "week");
        Map<TransactionModel.CategoryType, Double> monthResult = transactionService.getSpendingByCategory(userLogin, "month");
        Map<TransactionModel.CategoryType, Double> yearResult = transactionService.getSpendingByCategory(userLogin, "year");
        Map<TransactionModel.CategoryType, Double> overallResult = transactionService.getSpendingByCategory(userLogin, "overall");

        assertEquals(100.0, dayResult.get(TransactionModel.CategoryType.GROCERIES));
        assertEquals(100.0, weekResult.get(TransactionModel.CategoryType.GROCERIES));
        assertEquals(100.0, monthResult.get(TransactionModel.CategoryType.GROCERIES));
        assertEquals(100.0, yearResult.get(TransactionModel.CategoryType.GROCERIES));
        assertEquals(100.0, overallResult.get(TransactionModel.CategoryType.GROCERIES));
    }

    /**
     * Tests that saving a transaction triggers appropriate rule checks.
     * Verifies that:
     * 1. The transaction is saved to the repository
     * 2. Spending rules are checked for the user
     * 3. Overall overspending is evaluated
     */
    @Test
    void saveTransaction_SavesAndChecksRules() {
        TransactionModel transaction = new TransactionModel();
        UserModel user = new UserModel();
        transaction.setUser(user);

        transactionService.saveTransaction(transaction);

        verify(transactionRepository).save(transaction);
        verify(inboxMessageService).checkSpendingRules(user);
        verify(inboxMessageService).checkForOverallOverspending(user);
    }

    /**
     * Tests the basic transaction deletion functionality.
     * Verifies that the repository's delete method is called with the correct transaction.
     */
    @Test
    void removeTransaction_DeletesTransaction() {
        TransactionModel transaction = new TransactionModel();

        transactionService.removeTransaction(transaction);

        verify(transactionRepository).delete(transaction);
    }

    /**
     * Tests retrieving an existing transaction by ID.
     * Verifies that the correct transaction is returned when it exists in the repository.
     */
    @Test
    void getTransactionById_ExistingTransaction_ReturnsTransaction() {
        Long id = 1L;
        TransactionModel expectedTransaction = new TransactionModel();
        when(transactionRepository.findById(id)).thenReturn(Optional.of(expectedTransaction));

        TransactionModel result = transactionService.getTransactionById(id);

        assertEquals(expectedTransaction, result);
    }

    /**
     * Tests handling of non-existent transaction retrieval.
     * Verifies that null is returned when attempting to retrieve a transaction that doesn't exist.
     */
    @Test
    void getTransactionById_NonexistentTransaction_ReturnsNull() {
        Long id = 1L;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        TransactionModel result = transactionService.getTransactionById(id);

        assertNull(result);
    }

    /**
     * Tests spending calculation across all time periods.
     * Verifies that the same expense is correctly calculated regardless of the period specified,
     * including handling of invalid period values.
     */
    @Test
    void getTotalSpending_AllPeriods_CalculatesCorrectly() {
        String userLogin = "testUser";
        TransactionModel expense = createExpenseTransaction(100.0f, "GROCERIES", LocalDateTime.now());

        when(transactionRepository.findByUserLoginAndDateBetween(eq(userLogin), any(), any()))
                .thenReturn(List.of(expense));

        // Test all periods including default case
        assertEquals(100.0, transactionService.getTotalSpending(userLogin, "day"));
        assertEquals(100.0, transactionService.getTotalSpending(userLogin, "week"));
        assertEquals(100.0, transactionService.getTotalSpending(userLogin, "month"));
        assertEquals(100.0, transactionService.getTotalSpending(userLogin, "year"));
        assertEquals(100.0, transactionService.getTotalSpending(userLogin, "overall"));
        assertEquals(100.0, transactionService.getTotalSpending(userLogin, "invalid")); // tests default case
    }

    /**
     * Tests basic retrieval of user transactions.
     * Verifies that the service returns the exact list of transactions provided by the repository.
     */
    @Test
    void getTransactionsByUser_ReturnsUserTransactions() {
        String login = "testUser";
        List<TransactionModel> expectedTransactions = Arrays.asList(
                createExpenseTransaction(100.0f, "GROCERIES", LocalDateTime.now())
        );

        when(transactionRepository.findByUserLogin(login)).thenReturn(expectedTransactions);

        List<TransactionModel> result = transactionService.getTransactionsByUser(login);

        assertEquals(expectedTransactions, result);
    }

    /**
     * Tests monthly spending calculation with transactions outside the target month.
     * Verifies that only transactions within the specified month are included in the total,
     * while transactions from adjacent months are correctly excluded.
     */
    @Test
    void getTotalSpendingForMonth_OutsideMonthTransactions_ExcludesThemFromTotal() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        // Transaction within month
        LocalDateTime withinMonth = LocalDateTime.of(2024, 3, 15, 12, 0);
        TransactionModel validTransaction = createExpenseTransaction(100.0f, "GROCERIES", withinMonth);

        // Transactions outside month
        LocalDateTime beforeMonth = LocalDateTime.of(2024, 2, 28, 12, 0);
        LocalDateTime afterMonth = LocalDateTime.of(2024, 4, 1, 0, 0);
        TransactionModel beforeTransaction = createExpenseTransaction(50.0f, "GROCERIES", beforeMonth);
        TransactionModel afterTransaction = createExpenseTransaction(75.0f, "GROCERIES", afterMonth);

        when(transactionRepository.findByUserLogin(login))
                .thenReturn(Arrays.asList(validTransaction, beforeTransaction, afterTransaction));

        float result = transactionService.getTotalSpendingForMonth(login, year, month);

        assertEquals(100.0f, result, 0.001f);
    }

    /**
     * Tests monthly spending calculation with mixed transaction types.
     * Verifies that non-expense transactions (ex: income) are properly excluded
     * from the total spending calculation.
     */
    @Test
    void getTotalSpendingForMonth_NonExpenseTransactions_ExcludesThemFromTotal() {
        String login = "testUser";
        int year = 2024;
        int month = 3;
        LocalDateTime date = LocalDateTime.of(2024, 3, 15, 12, 0);

        TransactionModel expense = createExpenseTransaction(100.0f, "GROCERIES", date);
        TransactionModel income = createTransaction(50.0f, TransactionModel.TransactionType.INCOME,
                TransactionModel.CategoryType.SALARY, date);

        when(transactionRepository.findByUserLogin(login))
                .thenReturn(Arrays.asList(expense, income));

        float result = transactionService.getTotalSpendingForMonth(login, year, month);

        assertEquals(100.0f, result, 0.001f);
    }

    /**
     * Tests spending calculation with mixed transaction types.
     * Verifies that only EXPENSE transactions are included in the total,
     * while other transaction types are correctly filtered out.
     */
    @Test
    void getTotalSpending_MixedTransactionTypes_FiltersCorrectly() {
        String userLogin = "testUser";
        LocalDateTime now = LocalDateTime.now();
        List<TransactionModel> transactions = Arrays.asList(
                createTransaction(100.0f, TransactionModel.TransactionType.EXPENSE, TransactionModel.CategoryType.GROCERIES, now),
                createTransaction(200.0f, TransactionModel.TransactionType.INCOME, TransactionModel.CategoryType.SALARY, now),
                createTransaction(50.0f, TransactionModel.TransactionType.EXPENSE, TransactionModel.CategoryType.ENTERTAINMENT, now)
        );

        when(transactionRepository.findByUserLoginAndDateBetween(eq(userLogin), any(), any()))
                .thenReturn(transactions);

        double result = transactionService.getTotalSpending(userLogin, "day");
        assertEquals(150.0, result, 0.001, "Should only sum EXPENSE transactions");
    }

    /**
     * Tests category-based spending calculation with mixed transaction types.
     * Verifies that:
     * 1. Only EXPENSE transactions are included in category totals
     * 2. Categories with only non-expense transactions are excluded
     * 3. Multiple transactions in the same category are correctly summed
     */
    @Test
    void getSpendingByCategory_PeriodBased_MixedTransactions() {
        String userLogin = "testUser";
        LocalDateTime now = LocalDateTime.now();
        List<TransactionModel> transactions = Arrays.asList(
                createTransaction(100.0f, TransactionModel.TransactionType.EXPENSE, TransactionModel.CategoryType.GROCERIES, now),
                createTransaction(200.0f, TransactionModel.TransactionType.INCOME, TransactionModel.CategoryType.GROCERIES, now),
                createTransaction(50.0f, TransactionModel.TransactionType.EXPENSE, TransactionModel.CategoryType.GROCERIES, now)
        );

        when(transactionRepository.findByUserLoginAndDateBetween(eq(userLogin), any(), any()))
                .thenReturn(transactions);

        Map<TransactionModel.CategoryType, Double> result = transactionService.getSpendingByCategory(userLogin, "day");
        assertEquals(150.0, result.get(TransactionModel.CategoryType.GROCERIES), 0.001);
        assertEquals(1, result.size(), "Should only include categories with expenses");
    }

    /**
     * Tests comprehensive monthly category spending calculations.
     * Verifies handling of:
     * 1. Transactions at month boundaries
     * 2. Mixed transaction types within the month
     * 3. Multiple categories
     * 4. Transactions outside the month
     */
    @Test
    void getSpendingByCategory_MonthlyFiltering_AllCombinations() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        LocalDateTime beforeMonth = LocalDateTime.of(2024, 2, 28, 23, 59, 59);
        LocalDateTime startOfMonth = LocalDateTime.of(2024, 3, 1, 0, 0, 0);
        LocalDateTime middleOfMonth = LocalDateTime.of(2024, 3, 15, 12, 0, 0);
        LocalDateTime endOfMonth = LocalDateTime.of(2024, 3, 31, 23, 59, 59);
        LocalDateTime startOfNextMonth = LocalDateTime.of(2024, 4, 1, 0, 0, 0);

        List<TransactionModel> transactions = Arrays.asList(
                // Test boundary conditions for dates
                createTransaction(100.0f, TransactionModel.TransactionType.EXPENSE, TransactionModel.CategoryType.GROCERIES, beforeMonth),
                createTransaction(200.0f, TransactionModel.TransactionType.EXPENSE, TransactionModel.CategoryType.GROCERIES, startOfMonth),
                createTransaction(300.0f, TransactionModel.TransactionType.EXPENSE, TransactionModel.CategoryType.GROCERIES, middleOfMonth),
                createTransaction(400.0f, TransactionModel.TransactionType.EXPENSE, TransactionModel.CategoryType.GROCERIES, endOfMonth),
                createTransaction(500.0f, TransactionModel.TransactionType.EXPENSE, TransactionModel.CategoryType.GROCERIES, startOfNextMonth),

                // Test transaction type filtering
                createTransaction(1000.0f, TransactionModel.TransactionType.INCOME, TransactionModel.CategoryType.GROCERIES, middleOfMonth),

                // Test multiple categories
                createTransaction(150.0f, TransactionModel.TransactionType.EXPENSE, TransactionModel.CategoryType.ENTERTAINMENT, middleOfMonth)
        );

        when(transactionRepository.findByUserLogin(login)).thenReturn(transactions);

        Map<String, Float> result = transactionService.getSpendingByCategory(login, year, month);

        assertTrue(result.containsKey("GROCERIES"));
        assertTrue(result.containsKey("ENTERTAINMENT"));
        assertEquals(900.0f, result.get("GROCERIES"), 0.001f, "Should include only expenses within month (200+300+400)");
        assertEquals(150.0f, result.get("ENTERTAINMENT"), 0.001f);
        assertEquals(2, result.size(), "Should only include categories with expenses within the month");
    }

    /**
     * Tests monthly spending calculation with various date conditions.
     * Verifies the handling of transactions:
     * 1. Before the month starts
     * 2. At month start (excluded)
     * 3. Within the month
     * 4. At month end (excluded)
     * 5. After month ends
     * Also verifies correct handling of non-expense transactions.
     */
    @Test
    void getTotalSpendingForMonth_AllDateConditions() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        // Create test dates matching the service's exact calculations
        LocalDateTime startOfMonth = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1); // This matches the service implementation

        // Test points
        LocalDateTime beforeMonth = startOfMonth.minusSeconds(1);
        LocalDateTime afterStartOfMonth = startOfMonth.plusSeconds(1);
        LocalDateTime middleOfMonth = LocalDateTime.of(2024, 3, 15, 12, 0);
        LocalDateTime beforeEndOfMonth = endOfMonth.minusSeconds(1);
        LocalDateTime afterEndOfMonth = endOfMonth.plusSeconds(1);

        List<TransactionModel> transactions = Arrays.asList(
                // Should NOT be included (outside range)
                createTransaction(100.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, beforeMonth),
                createTransaction(200.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, startOfMonth),  // At start, excluded
                createTransaction(500.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, afterEndOfMonth),

                // Should be included (within range)
                createTransaction(150.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, afterStartOfMonth),
                createTransaction(200.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, middleOfMonth),
                createTransaction(100.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, beforeEndOfMonth),

                // Should NOT be included (different type)
                createTransaction(1000.0f, TransactionModel.TransactionType.INCOME,
                        TransactionModel.CategoryType.SALARY, middleOfMonth)
        );

        when(transactionRepository.findByUserLogin(login)).thenReturn(transactions);

        float result = transactionService.getTotalSpendingForMonth(login, year, month);
        assertEquals(450.0f, result, 0.001f, "Should include expenses within month (150+200+100)");
    }

    /**
     * Tests the exclusion of transactions exactly at month boundaries.
     * Verifies that transactions occurring exactly at the start or end
     * of a month are excluded from the month's total spending.
     */
    @Test
    void getTotalSpendingForMonth_BoundaryExclusion() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        LocalDateTime startOfMonth = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        List<TransactionModel> transactions = Arrays.asList(
                createTransaction(100.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, startOfMonth),
                createTransaction(200.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, endOfMonth.plusSeconds(1))
        );

        when(transactionRepository.findByUserLogin(login)).thenReturn(transactions);

        float result = transactionService.getTotalSpendingForMonth(login, year, month);
        assertEquals(0.0f, result, 0.001f, "Should exclude transactions at boundaries");
    }

    /**
     * Tests transaction inclusion for dates immediately after month start.
     * Verifies the boundary condition where a transaction occurs one second
     * after the start of the month, which should be included in the monthly total.
     * This tests the "greater than" part of the date range comparison.
     */
    @Test
    void getTotalSpendingForMonth_SingleDayAfterStart() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        LocalDateTime startOfMonth = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime justAfterStart = startOfMonth.plusSeconds(1);

        List<TransactionModel> transactions = Arrays.asList(
                createTransaction(100.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, justAfterStart)
        );

        when(transactionRepository.findByUserLogin(login)).thenReturn(transactions);

        float result = transactionService.getTotalSpendingForMonth(login, year, month);
        assertEquals(100.0f, result, 0.001f, "Should include transaction just after start");
    }

    /**
     * Tests transaction inclusion for dates immediately before month end.
     * Verifies the boundary condition where a transaction occurs one second
     * before the end of the month, which should be included in the monthly total.
     * This tests the "less than" part of the date range comparison.
     */
    @Test
    void getTotalSpendingForMonth_SingleDayBeforeEnd() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        LocalDateTime startOfMonth = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        LocalDateTime justBeforeEnd = endOfMonth.minusSeconds(1);

        List<TransactionModel> transactions = Arrays.asList(
                createTransaction(100.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, justBeforeEnd)
        );

        when(transactionRepository.findByUserLogin(login)).thenReturn(transactions);

        float result = transactionService.getTotalSpendingForMonth(login, year, month);
        assertEquals(100.0f, result, 0.001f, "Should include transaction just before end");
    }

    /**
     * Tests transaction exclusion for dates exactly at month end.
     * Verifies that a transaction occurring exactly at the end of the month
     * (last second) is excluded from the monthly total. This tests the strict
     * boundary exclusion at the end of the time range.
     */
    @Test
    void getTotalSpendingForMonth_ExactlyAtEndDate() {
        String login = "testUser";
        int year = 2024;
        int month = 3;

        LocalDateTime startOfMonth = LocalDateTime.of(2024, 3, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        List<TransactionModel> transactions = Arrays.asList(
                createTransaction(100.0f, TransactionModel.TransactionType.EXPENSE,
                        TransactionModel.CategoryType.GROCERIES, endOfMonth)
        );

        when(transactionRepository.findByUserLogin(login)).thenReturn(transactions);

        float result = transactionService.getTotalSpendingForMonth(login, year, month);
        assertEquals(0.0f, result, 0.001f, "Should exclude transaction exactly at end");
    }

    /**
     * Tests category spending calculation when all transactions are filtered out.
     * Verifies that when all transactions are non-expense types (income in this case),
     * the resulting category map is empty rather than containing zero values.
     * This test is important for ensuring proper handling of edge cases where:
     * 1. All transactions are filtered out due to type
     * 2. The system returns an empty map instead of a map with zero values
     * 3. No categories are included in the result, even if transactions exist
     */
    @Test
    void getSpendingByCategory_EmptyTransactionsAfterFiltering() {
        String login = "testUser";
        int year = 2024;
        int month = 3;
        LocalDateTime date = LocalDateTime.of(2024, 3, 15, 12, 0);

        // Only include non-expense transactions
        List<TransactionModel> transactions = Arrays.asList(
                createTransaction(100.0f, TransactionModel.TransactionType.INCOME, TransactionModel.CategoryType.SALARY, date),
                createTransaction(200.0f, TransactionModel.TransactionType.INCOME, TransactionModel.CategoryType.SALARY, date)
        );

        when(transactionRepository.findByUserLogin(login)).thenReturn(transactions);

        Map<String, Float> result = transactionService.getSpendingByCategory(login, year, month);
        assertTrue(result.isEmpty(), "Should be empty when no expenses exist");
    }

    /**
     * Helper method to create a transaction with specified type and category.
     * Used for creating test data with complete transaction information.
     */
    private TransactionModel createTransaction(float amount, TransactionModel.TransactionType type,
                                               TransactionModel.CategoryType category, LocalDateTime date) {
        TransactionModel transaction = new TransactionModel();
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setDate(date);
        return transaction;
    }

    /**
     * Helper method specifically for creating expense transactions.
     * Simplifies the creation of expense-type transactions for testing.
     */
    private TransactionModel createExpenseTransaction(float amount, String category, LocalDateTime date) {
        TransactionModel transaction = new TransactionModel();
        transaction.setAmount(amount);
        transaction.setType(TransactionModel.TransactionType.EXPENSE);
        transaction.setCategory(TransactionModel.CategoryType.valueOf(category));
        transaction.setDate(date);
        return transaction;
    }
}