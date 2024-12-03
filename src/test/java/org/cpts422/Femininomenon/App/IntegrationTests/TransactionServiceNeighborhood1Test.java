package org.cpts422.Femininomenon.App.IntegrationTests;

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

    @Test
    void testRemoveTransactionIntegration() {
        // Execute
        transactionService.removeTransaction(testExpenseTransaction);

        // Verify
        verify(transactionRepository).delete(testExpenseTransaction);
    }

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