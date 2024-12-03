package org.cpts422.Femininomenon.App.UnitTests.Service;

import org.cpts422.Femininomenon.App.Models.*;
import org.cpts422.Femininomenon.App.Repository.*;
import org.cpts422.Femininomenon.App.Service.InboxMessageService;
import org.cpts422.Femininomenon.App.Service.UserRuleService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.*;
import java.time.temporal.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for InboxMessageService. Tests the functionality of message handling,
 * spending rule checks, and transaction analysis.
 */
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

    /**
     * Sets up test data before each test method execution.
     * Initializes test user, rule, and transaction models.
     */
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

    /**
     * Helper method to set up transaction repository mock with specified transactions.
     * @param transactions List of transactions to be returned by the mock
     */
    private void setupTransactionMock(List<TransactionModel> transactions) {
        when(transactionRepository.findByUserLoginAndDateBetween(
                eq(testUser.getLogin()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);
    }

    /**
     * Helper method to verify that a saved message contains expected content.
     * @param expectedContent the content that should be present in the message
     */
    private void verifyMessageContains(String expectedContent) {
        verify(inboxMessageRepository).save(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().getMessage().contains(expectedContent));
    }

    /**
     * Helper method to verify multiple messages were saved with expected content.
     * @param expectedCalls number of expected save calls
     * @param messageContents array of expected message contents
     */
    private void verifyMultipleMessages(int expectedCalls, String... messageContents) {
        verify(inboxMessageRepository, times(expectedCalls)).save(messageCaptor.capture());
        List<InboxMessageModel> messages = messageCaptor.getAllValues();
        for (int i = 0; i < messageContents.length; i++) {
            assertTrue(messages.get(i).getMessage().contains(messageContents[i]));
        }
    }

    /**
     * Tests retrieval of inbox messages for a specific user.
     */
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

    /**
     * Tests marking a single message as read.
     */
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

    /**
     * Tests exception handling when marking a non-existent message as read.
     */
    @Test
    void markMessageAsRead_ShouldThrowException_WhenMessageNotFound() {
        when(inboxMessageRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> inboxMessageService.markMessageAsRead(1L));
    }

    /**
     * Tests marking all unread messages as read for a specific user.
     */
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

    /**
     * Tests alert creation when maximum spending limit is exceeded.
     */
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

    /**
     * Tests alert creation when minimum savings target is not met.
     */
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

    /**
     * Tests alert creation when category spending exceeds comparison category.
     */
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

    /**
     * Tests alert creation when overall expenses exceed income.
     */
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

    /**
     * Tests exception handling when frequency is null in getStartDate method.
     * @throws Exception when method reflection or invocation fails
     */
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

    /**
     * Tests handling of daily frequency in getStartDate method.
     * @throws Exception when method reflection or invocation fails
     */
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

    /**
     * Tests handling of weekly frequency in getStartDate method.
     * @throws Exception when method reflection or invocation fails
     */
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

    /**
     * Tests handling of unsupported frequency in getStartDate method.
     * @throws Exception when method reflection or invocation fails
     */
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

    /**
     * Tests overspending check with empty transaction list.
     */
    @Test
    void checkForOverallOverspending_ShouldHandleEmptyTransactions() {
        when(transactionRepository.findByUserLoginAndDateBetween(
                eq(testUser.getLogin()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        inboxMessageService.checkForOverallOverspending(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    /**
     * Tests overspending check when expenses don't exceed income.
     */
    @Test
    void checkForOverallOverspending_ShouldHandleNoOverspending() {
        testIncome.setAmount(2000.0F);
        testExpense.setAmount(1000.0F);
        setupTransactionMock(Arrays.asList(testIncome, testExpense));

        inboxMessageService.checkForOverallOverspending(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    /**
     * Tests spending rule check when alert message already exists.
     */
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

    /**
     * Tests overspending check when no large expenses are present.
     */
    @Test
    void checkForOverallOverspending_ShouldNotCreateAlert_WhenNoLargeExpense() {
        testIncome.setAmount(1000.0F);
        testExpense.setAmount(100.0F); // Small expense, less than 50% of income
        setupTransactionMock(Arrays.asList(testIncome, testExpense));

        inboxMessageService.checkForOverallOverspending(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    /**
     * Tests message addition when message already exists.
     */
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

    /**
     * Tests spending rule check with null additional category.
     */
    @Test
    void checkSpendingRules_ShouldHandleNullAdditionalCategory() {
        testRule.setRuleType(UserRuleModel.RuleType.NOT_EXCEED_CATEGORY);
        testRule.setAdditionalCategory(null);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));

        inboxMessageService.checkSpendingRules(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    /**
     * Tests exception handling for unsupported frequency value in getStartDate.
     * @throws Exception when method reflection or invocation fails
     */
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

    /**
     * Tests that no alert is created when category spending is lower than comparison.
     */
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

    /**
     * Tests spending rule check with null category.
     */
    @Test
    void checkSpendingRules_ShouldHandleNullCategory() {
        testRule.setCategory(null);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));

        inboxMessageService.checkSpendingRules(testUser);

        verify(inboxMessageRepository, never()).save(any());
        verify(transactionRepository, never()).findByUserLoginAndDateBetween(any(), any(), any());
    }

    /**
     * Tests that no alert is created when minimum savings target is met.
     */
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

    /**
     * Tests maximum spending check with empty transaction list.
     */
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

    /**
     * Tests spending rule check with empty rules list.
     */
    @Test
    void checkSpendingRules_ShouldHandleEmptyRulesList() {
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.emptyList());

        inboxMessageService.checkSpendingRules(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    /**
     * Tests overspending check when no income transactions are present.
     */
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

    /**
     * Tests large expense check when no income transactions are present.
     * @throws InvocationTargetException if the invoked method throws an exception
     * @throws IllegalAccessException if the method is inaccessible
     * @throws NoSuchMethodException if the method doesn't exist
     */
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

    /**
     * Tests large expense check when expenses exceed income threshold.
     * @throws InvocationTargetException if the invoked method throws an exception
     * @throws IllegalAccessException if the method is inaccessible
     * @throws NoSuchMethodException if the method doesn't exist
     */
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

    /**
     * Tests large expense check when expenses are below income threshold.
     * @throws InvocationTargetException if the invoked method throws an exception
     * @throws IllegalAccessException if the method is inaccessible
     * @throws NoSuchMethodException if the method doesn't exist
     */
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

    /**
     * Tests large expense check when no expense transactions are present.
     * @throws InvocationTargetException if the invoked method throws an exception
     * @throws IllegalAccessException if the method is inaccessible
     * @throws NoSuchMethodException if the method doesn't exist
     */
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