package org.cpts422.Femininomenon.App.IntegrationTests;

import org.cpts422.Femininomenon.App.Models.*;
import org.cpts422.Femininomenon.App.Repository.TransactionRepository;
import org.cpts422.Femininomenon.App.Service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceNeighborhood1Test {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InboxMessageService inboxMessageService;

    private TransactionService transactionService;
    private UserModel testUser;
    private TransactionModel testExpenseTransaction;
    private TransactionModel testIncomeTransaction;


    /**
     * Sets up the test environment before each test execution.
     * Initializes:
     * 1. TransactionService with mocked dependencies
     * 2. Test user with basic properties
     * 3. Sample expense and income transactions
     * This setup ensures each test starts with a clean, consistent state
     * and has access to common test data.
     */
    @BeforeEach
    void setUp() {
        // Initialize the real service with mocked dependencies
        transactionService = new TransactionService(transactionRepository, inboxMessageService);

        // Setup test user
        testUser = new UserModel();
        testUser.setLogin("testUser");

        // Setup test transactions
        testExpenseTransaction = new TransactionModel();
        testExpenseTransaction.setUser(testUser);
        testExpenseTransaction.setAmount(100.0f);
        testExpenseTransaction.setType(TransactionModel.TransactionType.EXPENSE);
        testExpenseTransaction.setCategory(TransactionModel.CategoryType.GROCERIES);
        testExpenseTransaction.setDate(LocalDateTime.now());

        testIncomeTransaction = new TransactionModel();
        testIncomeTransaction.setUser(testUser);
        testIncomeTransaction.setAmount(200.0f);
        testIncomeTransaction.setType(TransactionModel.TransactionType.INCOME);
        testIncomeTransaction.setDate(LocalDateTime.now());
    }

    /**
     * Tests the integration of transaction saving with neighboring services.
     * Verifies that:
     * 1. The transaction is properly saved to the repository
     * 2. Spending rules are checked via the inbox service
     * 3. Overall overspending is monitored
     * This test ensures proper coordination between transaction persistence
     * and notification systems.
     */
    @Test
    void testSaveTransactionIntegration() {
        // Setup
        when(transactionRepository.save(any(TransactionModel.class))).thenReturn(testExpenseTransaction);

        // Execute
        transactionService.saveTransaction(testExpenseTransaction);

        // Verify interactions with both neighbors
        verify(transactionRepository).save(testExpenseTransaction);
        verify(inboxMessageService).checkSpendingRules(testUser);
        verify(inboxMessageService).checkForOverallOverspending(testUser);
    }

    /**
     * Tests the retrieval and categorization of spending data.
     * Verifies that:
     * 1. Correct date range queries are made to the repository
     * 2. Transactions are properly categorized
     * 3. Spending amounts are accurately calculated per category
     * This test ensures accurate spending analysis and reporting functionality.
     */
    @Test
    void testGetSpendingByCategoryIntegration() {
        // Setup
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime now = LocalDateTime.now();
        List<TransactionModel> transactions = Arrays.asList(testExpenseTransaction, testIncomeTransaction);

        when(transactionRepository.findByUserLoginAndDateBetween(
                eq("testUser"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);

        // Execute
        Map<TransactionModel.CategoryType, Double> result = transactionService.getSpendingByCategory("testUser", "month");

        // Verify
        verify(transactionRepository).findByUserLoginAndDateBetween(
                eq("testUser"), any(LocalDateTime.class), any(LocalDateTime.class));
        assertNotNull(result);
        assertEquals(100.0, result.get(TransactionModel.CategoryType.GROCERIES));
    }

    /**
     * Tests the retrieval of individual transactions by ID.
     * Verifies that:
     * 1. The correct transaction is retrieved from the repository
     * 2. The transaction data is properly returned
     * This test ensures accurate transaction lookup functionality.
     */
    @Test
    void testGetTransactionByIdIntegration() {
        // Setup
        Long testId = 1L;
        when(transactionRepository.findById(testId)).thenReturn(Optional.of(testExpenseTransaction));

        // Execute
        TransactionModel result = transactionService.getTransactionById(testId);

        // Verify
        verify(transactionRepository).findById(testId);
        assertNotNull(result);
        assertEquals(testExpenseTransaction, result);
    }

    /**
     * Tests the transaction removal process.
     * Verifies that:
     * 1. The transaction is properly deleted from the repository
     * 2. The delete operation is called exactly once
     * This test ensures reliable transaction deletion functionality.
     */
    @Test
    void testRemoveTransactionIntegration() {
        // Execute
        transactionService.removeTransaction(testExpenseTransaction);

        // Verify
        verify(transactionRepository).delete(testExpenseTransaction);
    }

    /**
     * Tests the calculation of total spending over a time period.
     * Verifies that:
     * 1. Correct date range queries are made to the repository
     * 2. Only expense transactions are included in the total
     * 3. The spending amount is accurately calculated
     * This test ensures accurate spending calculation functionality,
     * particularly the exclusion of income transactions from spending totals.
     */
    @Test
    void testGetTotalSpendingIntegration() {
        // Setup
        List<TransactionModel> transactions = Arrays.asList(testExpenseTransaction, testIncomeTransaction);
        when(transactionRepository.findByUserLoginAndDateBetween(
                eq("testUser"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);

        // Execute
        double totalSpending = transactionService.getTotalSpending("testUser", "month");

        // Verify
        verify(transactionRepository).findByUserLoginAndDateBetween(
                eq("testUser"), any(LocalDateTime.class), any(LocalDateTime.class));
        assertEquals(100.0, totalSpending); // Only expense transaction should be counted
    }
}