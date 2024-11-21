package org.cpts422.Femininomenon.App.Service;

import org.cpts422.Femininomenon.App.Models.*;
import org.cpts422.Femininomenon.App.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboxMessageServiceTest {
    @Mock
    private InboxMessageRepository inboxMessageRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRuleService userRuleService;
    @InjectMocks
    private InboxMessageService inboxMessageService;
    @Captor
    private ArgumentCaptor<InboxMessageModel> messageCaptor;
    @Captor
    private ArgumentCaptor<List<InboxMessageModel>> messagesListCaptor;

    private UserModel testUser;
    private UserRuleModel testRule;
    private TransactionModel testExpense;
    private TransactionModel testIncome;

    @BeforeEach
    void setUp() {
        testUser = new UserModel();
        testUser.setLogin("testUser");

        testRule = new UserRuleModel();
        testRule.setCategory(TransactionModel.CategoryType.GROCERIES);
        testRule.setFrequency(UserRuleModel.Frequency.MONTHLY);
        testRule.setLimitAmount(1000.0F);

        testExpense = new TransactionModel();
        testExpense.setType(TransactionModel.TransactionType.EXPENSE);
        testExpense.setCategory(TransactionModel.CategoryType.GROCERIES);

        testIncome = new TransactionModel();
        testIncome.setType(TransactionModel.TransactionType.INCOME);
    }

    private void setupTransactionMock(List<TransactionModel> transactions) {
        when(transactionRepository.findByUserLoginAndDateBetween(
                eq(testUser.getLogin()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);
    }

    private void verifyMessageContains(String expectedContent) {
        verify(inboxMessageRepository).save(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().getMessage().contains(expectedContent));
    }

    private void verifyMultipleMessages(int expectedCalls, String... messageContents) {
        verify(inboxMessageRepository, times(expectedCalls)).save(messageCaptor.capture());
        List<InboxMessageModel> messages = messageCaptor.getAllValues();
        for (int i = 0; i < messageContents.length; i++) {
            assertTrue(messages.get(i).getMessage().contains(messageContents[i]));
        }
    }

    @Test
    void getInboxMessages_ShouldReturnMessages() {
        List<InboxMessageModel> expectedMessages = Arrays.asList(
                new InboxMessageModel(testUser, "Test message 1"),
                new InboxMessageModel(testUser, "Test message 2")
        );
        when(inboxMessageRepository.findByUser(testUser)).thenReturn(expectedMessages);

        List<InboxMessageModel> actualMessages = inboxMessageService.getInboxMessages(testUser);

        assertEquals(expectedMessages, actualMessages);
        verify(inboxMessageRepository).findByUser(testUser);
    }

    @Test
    void markMessageAsRead_ShouldMarkMessageAsRead() {
        InboxMessageModel message = new InboxMessageModel(testUser, "Test message");
        message.setId(1L);
        message.setRead(false);
        when(inboxMessageRepository.findById(1L)).thenReturn(Optional.of(message));

        inboxMessageService.markMessageAsRead(1L);

        verifyMessageContains("Test message");
        assertTrue(messageCaptor.getValue().isRead());
    }

    @Test
    void markMessageAsRead_ShouldThrowException_WhenMessageNotFound() {
        when(inboxMessageRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> inboxMessageService.markMessageAsRead(1L));
    }

    @Test
    void markAllMessagesAsRead_ShouldMarkAllMessagesAsRead() {
        List<InboxMessageModel> unreadMessages = Arrays.asList(
                new InboxMessageModel(testUser, "Message 1"),
                new InboxMessageModel(testUser, "Message 2")
        );
        when(inboxMessageRepository.findByUserAndIsReadFalse(testUser)).thenReturn(unreadMessages);

        inboxMessageService.markAllMessagesAsRead(testUser);

        verify(inboxMessageRepository).saveAll(messagesListCaptor.capture());
        assertTrue(messagesListCaptor.getValue().stream().allMatch(InboxMessageModel::isRead));
    }

    @Test
    void checkSpendingRules_ShouldCreateAlert_WhenMaximumSpendingExceeded() {
        testRule.setRuleType(UserRuleModel.RuleType.MAXIMUM_SPENDING);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));
        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);

        testExpense.setAmount(1500.0F);
        setupTransactionMock(Collections.singletonList(testExpense));

        inboxMessageService.checkSpendingRules(testUser);

        verifyMessageContains("exceeded your monthly spending limit");
    }

    @Test
    void checkSpendingRules_ShouldCreateAlert_WhenMinimumSavingsNotMet() {
        testRule.setRuleType(UserRuleModel.RuleType.MINIMUM_SAVINGS);
        testRule.setLimitAmount(500.0F);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));
        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);

        testIncome.setAmount(1000.0F);
        testExpense.setAmount(800.0F);
        setupTransactionMock(Arrays.asList(testIncome, testExpense));

        inboxMessageService.checkSpendingRules(testUser);

        verifyMessageContains("savings");
    }

    @Test
    void checkSpendingRules_ShouldCreateAlert_WhenCategoryExceedsComparison() {
        testRule.setRuleType(UserRuleModel.RuleType.NOT_EXCEED_CATEGORY);
        testRule.setCategory(TransactionModel.CategoryType.ENTERTAINMENT);
        testRule.setAdditionalCategory(TransactionModel.CategoryType.GROCERIES);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));
        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);

        TransactionModel entertainmentExpense = new TransactionModel();
        entertainmentExpense.setType(TransactionModel.TransactionType.EXPENSE);
        entertainmentExpense.setCategory(TransactionModel.CategoryType.ENTERTAINMENT);
        entertainmentExpense.setAmount(800.0F);

        testExpense.setAmount(500.0F);
        setupTransactionMock(Arrays.asList(entertainmentExpense, testExpense));

        inboxMessageService.checkSpendingRules(testUser);

        verifyMessageContains("exceeded your spending");
    }

    @Test
    void checkForOverallOverspending_ShouldCreateAlerts_WhenExpensesExceedIncome() {
        testIncome.setAmount(1000.0F);
        testExpense.setAmount(1500.0F);
        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);
        setupTransactionMock(Arrays.asList(testIncome, testExpense));

        inboxMessageService.checkForOverallOverspending(testUser);

        verifyMultipleMessages(2, "expenses", "large individual expense");
    }

    @Test
    void getStartDate_ShouldThrowException_WhenFrequencyIsNull() throws Exception {
        Method getStartDateMethod = InboxMessageService.class.getDeclaredMethod("getStartDate",
                LocalDateTime.class, UserRuleModel.Frequency.class);
        getStartDateMethod.setAccessible(true);

        Exception exception = assertThrows(Exception.class,
                () -> getStartDateMethod.invoke(inboxMessageService, LocalDateTime.now(), null));

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Frequency cannot be null", exception.getCause().getMessage());
    }

    @Test
    void getStartDate_ShouldHandleDaily() throws Exception {
        Method getStartDateMethod = InboxMessageService.class.getDeclaredMethod("getStartDate",
                LocalDateTime.class, UserRuleModel.Frequency.class);
        getStartDateMethod.setAccessible(true);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime result = (LocalDateTime) getStartDateMethod.invoke(inboxMessageService,
                now, UserRuleModel.Frequency.DAILY);

        assertEquals(now.truncatedTo(ChronoUnit.DAYS), result);
    }

    @Test
    void getStartDate_ShouldHandleWeekly() throws Exception {
        Method getStartDateMethod = InboxMessageService.class.getDeclaredMethod("getStartDate",
                LocalDateTime.class, UserRuleModel.Frequency.class);
        getStartDateMethod.setAccessible(true);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime result = (LocalDateTime) getStartDateMethod.invoke(inboxMessageService,
                now, UserRuleModel.Frequency.WEEKLY);

        assertEquals(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), result);
    }

    @Test
    void getStartDate_ShouldHandleUnsupportedFrequency() throws Exception {
        Method getStartDateMethod = InboxMessageService.class.getDeclaredMethod("getStartDate",
                LocalDateTime.class, UserRuleModel.Frequency.class);
        getStartDateMethod.setAccessible(true);

        LocalDateTime now = LocalDateTime.now();
        Exception exception = assertThrows(Exception.class,
                () -> getStartDateMethod.invoke(inboxMessageService, now, null));

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Frequency cannot be null", exception.getCause().getMessage());
    }

    @Test
    void checkForOverallOverspending_ShouldHandleEmptyTransactions() {
        when(transactionRepository.findByUserLoginAndDateBetween(
                eq(testUser.getLogin()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        inboxMessageService.checkForOverallOverspending(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void checkForOverallOverspending_ShouldHandleNoOverspending() {
        testIncome.setAmount(2000.0F);
        testExpense.setAmount(1000.0F);
        setupTransactionMock(Arrays.asList(testIncome, testExpense));

        inboxMessageService.checkForOverallOverspending(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void checkSpendingRules_ShouldSkipWhenMessageExists() {
        testRule.setRuleType(UserRuleModel.RuleType.MAXIMUM_SPENDING);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));
        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(true);

        testExpense.setAmount(1500.0F);
        setupTransactionMock(Collections.singletonList(testExpense));

        inboxMessageService.checkSpendingRules(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void checkForOverallOverspending_ShouldNotCreateAlert_WhenNoLargeExpense() {
        testIncome.setAmount(1000.0F);
        testExpense.setAmount(100.0F); // Small expense, less than 50% of income
        setupTransactionMock(Arrays.asList(testIncome, testExpense));

        inboxMessageService.checkForOverallOverspending(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void addMessage_ShouldHandleExistingMessage() {
        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(true);

        Method addMessageMethod;
        try {
            addMessageMethod = InboxMessageService.class.getDeclaredMethod("addMessage",
                    UserModel.class, String.class);
            addMessageMethod.setAccessible(true);
            addMessageMethod.invoke(inboxMessageService, testUser, "Test message");

            verify(inboxMessageRepository, never()).save(any());
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    void checkSpendingRules_ShouldHandleNullAdditionalCategory() {
        testRule.setRuleType(UserRuleModel.RuleType.NOT_EXCEED_CATEGORY);
        testRule.setAdditionalCategory(null);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));

        inboxMessageService.checkSpendingRules(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void getStartDate_ShouldThrowException_ForUnsupportedFrequencyValue() throws Exception {
        Method getStartDateMethod = InboxMessageService.class.getDeclaredMethod("getStartDate",
                LocalDateTime.class, UserRuleModel.Frequency.class);
        getStartDateMethod.setAccessible(true);

        // Use YEARLY frequency which isn't handled in the switch statement
        Exception exception = assertThrows(Exception.class,
                () -> getStartDateMethod.invoke(inboxMessageService, LocalDateTime.now(), UserRuleModel.Frequency.YEARLY));

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getCause().getMessage().contains("Unsupported frequency type"));
    }

    @Test
    void checkSpendingRules_ShouldNotCreateAlert_WhenNotExceedCategoryAndAmountLower() {
        testRule.setRuleType(UserRuleModel.RuleType.NOT_EXCEED_CATEGORY);
        testRule.setCategory(TransactionModel.CategoryType.ENTERTAINMENT);
        testRule.setAdditionalCategory(TransactionModel.CategoryType.GROCERIES);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));

        TransactionModel entertainmentExpense = new TransactionModel();
        entertainmentExpense.setType(TransactionModel.TransactionType.EXPENSE);
        entertainmentExpense.setCategory(TransactionModel.CategoryType.ENTERTAINMENT);
        entertainmentExpense.setAmount(300.0F);

        TransactionModel groceriesExpense = new TransactionModel();
        groceriesExpense.setType(TransactionModel.TransactionType.EXPENSE);
        groceriesExpense.setCategory(TransactionModel.CategoryType.GROCERIES);
        groceriesExpense.setAmount(500.0F);

        setupTransactionMock(Arrays.asList(entertainmentExpense, groceriesExpense));

        inboxMessageService.checkSpendingRules(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void checkSpendingRules_ShouldHandleNullCategory() {
        testRule.setCategory(null);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));

        inboxMessageService.checkSpendingRules(testUser);

        verify(inboxMessageRepository, never()).save(any());
        verify(transactionRepository, never()).findByUserLoginAndDateBetween(any(), any(), any());
    }

    @Test
    void checkSpendingRules_ShouldNotCreateAlert_WhenMinimumSavingsMet() {
        testRule.setRuleType(UserRuleModel.RuleType.MINIMUM_SAVINGS);
        testRule.setLimitAmount(200.0F);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));

        testIncome.setAmount(1000.0F);
        testExpense.setAmount(700.0F); // Savings of 300, above limit
        setupTransactionMock(Arrays.asList(testIncome, testExpense));

        inboxMessageService.checkSpendingRules(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void checkSpendingRules_ShouldHandleMaximumSpendingWithNoTransactions() {
        testRule.setRuleType(UserRuleModel.RuleType.MAXIMUM_SPENDING);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));

        // Return empty list for transactions
        setupTransactionMock(Collections.emptyList());

        inboxMessageService.checkSpendingRules(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void checkSpendingRules_ShouldHandleEmptyRulesList() {
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.emptyList());

        inboxMessageService.checkSpendingRules(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void checkForOverallOverspending_ShouldHandleZeroIncome() {
        // Setup a transaction list with only expenses
        testExpense.setAmount(100.0F);
        TransactionModel expense2 = new TransactionModel();
        expense2.setType(TransactionModel.TransactionType.EXPENSE);
        expense2.setAmount(50.0F);

        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);
        setupTransactionMock(Arrays.asList(testExpense, expense2));

        inboxMessageService.checkForOverallOverspending(testUser);

        verify(inboxMessageRepository, times(2)).save(messageCaptor.capture());
        List<InboxMessageModel> messages = messageCaptor.getAllValues();
        assertTrue(messages.get(0).getMessage().contains("no income"));
    }

    @Test
    void checkLargeExpenses_WithNoIncome() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        TransactionModel expense = new TransactionModel();
        expense.setType(TransactionModel.TransactionType.EXPENSE);
        expense.setAmount(1000.0F);

        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);

        Method checkLargeExpensesMethod = InboxMessageService.class.getDeclaredMethod(
                "checkLargeExpenses", UserModel.class, List.class);
        checkLargeExpensesMethod.setAccessible(true);
        checkLargeExpensesMethod.invoke(inboxMessageService, testUser, Collections.singletonList(expense));

        verify(inboxMessageRepository).save(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().getMessage().contains("with no income recorded"));
    }

    @Test
    void checkLargeExpenses_WithIncomeButOverThreshold() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        TransactionModel income = new TransactionModel();
        income.setType(TransactionModel.TransactionType.INCOME);
        income.setAmount(1000.0F);

        TransactionModel expense = new TransactionModel();
        expense.setType(TransactionModel.TransactionType.EXPENSE);
        expense.setAmount(600.0F); // Over 50% of income

        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);

        Method checkLargeExpensesMethod = InboxMessageService.class.getDeclaredMethod(
                "checkLargeExpenses", UserModel.class, List.class);
        checkLargeExpensesMethod.setAccessible(true);
        checkLargeExpensesMethod.invoke(inboxMessageService, testUser, Arrays.asList(income, expense));

        verify(inboxMessageRepository).save(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().getMessage().contains("more than 50%"));
    }

    @Test
    void checkLargeExpenses_WithIncomeAndUnderThreshold() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        TransactionModel income = new TransactionModel();
        income.setType(TransactionModel.TransactionType.INCOME);
        income.setAmount(1000.0F);

        TransactionModel expense = new TransactionModel();
        expense.setType(TransactionModel.TransactionType.EXPENSE);
        expense.setAmount(400.0F); // Under 50% of income

        Method checkLargeExpensesMethod = InboxMessageService.class.getDeclaredMethod(
                "checkLargeExpenses", UserModel.class, List.class);
        checkLargeExpensesMethod.setAccessible(true);
        checkLargeExpensesMethod.invoke(inboxMessageService, testUser, Arrays.asList(income, expense));

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void checkLargeExpenses_WithNoExpenses() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        TransactionModel income = new TransactionModel();
        income.setType(TransactionModel.TransactionType.INCOME);
        income.setAmount(1000.0F);

        Method checkLargeExpensesMethod = InboxMessageService.class.getDeclaredMethod(
                "checkLargeExpenses", UserModel.class, List.class);
        checkLargeExpensesMethod.setAccessible(true);
        checkLargeExpensesMethod.invoke(inboxMessageService, testUser, Collections.singletonList(income));

        verify(inboxMessageRepository, never()).save(any());
    }
}