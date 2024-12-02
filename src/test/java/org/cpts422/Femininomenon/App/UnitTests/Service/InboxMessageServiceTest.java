package org.cpts422.Femininomenon.App.UnitTests.Service;

import org.cpts422.Femininomenon.App.Models.*;
import org.cpts422.Femininomenon.App.Repository.*;
import org.cpts422.Femininomenon.App.Service.InboxMessageService;
import org.cpts422.Femininomenon.App.Service.UserRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void checkSpendingRules_ShouldHandleAllRuleTypes() {
        for (UserRuleModel.RuleType ruleType : UserRuleModel.RuleType.values()) {
            testRule.setRuleType(ruleType);
            if (ruleType == UserRuleModel.RuleType.NOT_EXCEED_CATEGORY) {
                testRule.setAdditionalCategory(TransactionModel.CategoryType.ENTERTAINMENT);
            }

            when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                    .thenReturn(Collections.singletonList(testRule));
            when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                    any(), any(), any())).thenReturn(false);

            testExpense.setAmount(1500.0F);
            setupTransactionMock(Collections.singletonList(testExpense));

            inboxMessageService.checkSpendingRules(testUser);
        }
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
    void checkForOverallOverspending_ShouldCreateAlert_WhenOnlyExpensesExist() {
        testExpense.setAmount(1000.0F);
        setupTransactionMock(Collections.singletonList(testExpense));
        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);

        inboxMessageService.checkForOverallOverspending(testUser);

        verifyMultipleMessages(2, "no income", "large individual expense");
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
    void checkForOverallOverspending_ShouldNotCreateAlert_WhenLargeExpenseButUnderThreshold() {
        testIncome.setAmount(1000.0F);
        testExpense.setAmount(400.0F); // 40% of income, under 50% threshold
        setupTransactionMock(Arrays.asList(testIncome, testExpense));

        inboxMessageService.checkForOverallOverspending(testUser);

        verify(inboxMessageRepository, never()).save(any());
    }

    @Test
    void checkForOverallOverspending_ShouldCreateOnlyOverspendingAlert_WhenExpensesExceedIncomeButNoLargeIndividualExpense() {
        testIncome.setAmount(1000.0F);

        TransactionModel expense1 = new TransactionModel();
        expense1.setType(TransactionModel.TransactionType.EXPENSE);
        expense1.setAmount(300.0F);

        TransactionModel expense2 = new TransactionModel();
        expense2.setType(TransactionModel.TransactionType.EXPENSE);
        expense2.setAmount(400.0F);

        TransactionModel expense3 = new TransactionModel();
        expense3.setType(TransactionModel.TransactionType.EXPENSE);
        expense3.setAmount(400.0F);

        setupTransactionMock(Arrays.asList(testIncome, expense1, expense2, expense3));
        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);

        inboxMessageService.checkForOverallOverspending(testUser);

        verify(inboxMessageRepository).save(messageCaptor.capture());
        String capturedMessage = messageCaptor.getValue().getMessage();
        assertTrue(capturedMessage.contains("expenses"));
        assertFalse(capturedMessage.contains("large individual expense"));
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
    void checkSpendingRules_ShouldHandleMinimumSavingsWithNoIncome() {
        testRule.setRuleType(UserRuleModel.RuleType.MINIMUM_SAVINGS);
        testRule.setLimitAmount(500.0F);
        when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                .thenReturn(Collections.singletonList(testRule));
        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);

        // Only expenses, no income
        testExpense.setAmount(800.0F);
        setupTransactionMock(Collections.singletonList(testExpense));

        inboxMessageService.checkSpendingRules(testUser);

        verifyMessageContains("savings");
    }

    @Test
    void checkForOverallOverspending_ShouldHandleZeroIncomeWithLargeExpense() {
        // Create a transaction with no income and a single large expense
        testExpense.setAmount(1000.0F); // Large expense
        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);
        setupTransactionMock(Collections.singletonList(testExpense));

        inboxMessageService.checkForOverallOverspending(testUser);

        verify(inboxMessageRepository, atLeast(1)).save(messageCaptor.capture());
        List<InboxMessageModel> messages = messageCaptor.getAllValues();
        boolean hasNoIncomeMessage = messages.stream()
                .anyMatch(msg -> msg.getMessage().contains("no income recorded"));
        boolean hasLargeExpenseMessage = messages.stream()
                .anyMatch(msg -> msg.getMessage().contains("large individual expense"));

        assertTrue(hasNoIncomeMessage);
        assertTrue(hasLargeExpenseMessage);
    }

    @Test
    void checkSpendingRules_ShouldHandleAllRuleTypesExhaustively() {
        // Test each rule type separately
        for (UserRuleModel.RuleType ruleType : UserRuleModel.RuleType.values()) {
            // Reset mocks for clean state
            clearInvocations(inboxMessageRepository, transactionRepository);

            // Set up the rule
            testRule.setRuleType(ruleType);
            testRule.setCategory(TransactionModel.CategoryType.GROCERIES);
            testRule.setLimitAmount(1000.0F);

            // Set up transactions based on rule type
            List<TransactionModel> transactions = new ArrayList<>();

            switch (ruleType) {
                case MAXIMUM_SPENDING:
                    testExpense.setAmount(1500.0F); // Exceed limit
                    transactions.add(testExpense);
                    break;

                case MINIMUM_SAVINGS:
                    testIncome.setAmount(2000.0F);
                    testExpense.setAmount(1800.0F); // Less than required savings
                    transactions.add(testIncome);
                    transactions.add(testExpense);
                    break;

                case NOT_EXCEED_CATEGORY:
                    testRule.setAdditionalCategory(TransactionModel.CategoryType.ENTERTAINMENT);

                    // First expense in primary category
                    testExpense.setCategory(TransactionModel.CategoryType.GROCERIES);
                    testExpense.setAmount(1000.0F);
                    transactions.add(testExpense);

                    // Second expense in comparison category
                    TransactionModel entertainmentExpense = new TransactionModel();
                    entertainmentExpense.setType(TransactionModel.TransactionType.EXPENSE);
                    entertainmentExpense.setCategory(TransactionModel.CategoryType.ENTERTAINMENT);
                    entertainmentExpense.setAmount(500.0F);
                    transactions.add(entertainmentExpense);
                    break;
            }

            // Set up mocks
            when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                    .thenReturn(Collections.singletonList(testRule));
            when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                    any(), any(), any())).thenReturn(false);
            when(transactionRepository.findByUserLoginAndDateBetween(
                    eq(testUser.getLogin()), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(transactions);

            // Execute the method
            inboxMessageService.checkSpendingRules(testUser);

            // Verify a message was created
            verify(inboxMessageRepository, atLeastOnce()).save(any());
        }
    }

    @Test
    void checkForOverallOverspending_ShouldCreateAlertForZeroIncomeWithExpense() {
        // Clear any previous test data
        clearInvocations(inboxMessageRepository);

        // Create a list with only expense transactions (no income transactions)
        List<TransactionModel> transactions = Collections.singletonList(testExpense);
        testExpense.setAmount(500.0F);  // Set a non-zero expense amount

        // Ensure the repository returns exactly these transactions
        when(transactionRepository.findByUserLoginAndDateBetween(
                eq(testUser.getLogin()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);

        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);

        // Execute the method
        inboxMessageService.checkForOverallOverspending(testUser);

        // Verify message creation
        verify(inboxMessageRepository, times(2)).save(messageCaptor.capture());
        List<InboxMessageModel> messages = messageCaptor.getAllValues();

        // Verify both expected messages were created
        assertTrue(messages.stream()
                .anyMatch(msg -> msg.getMessage().contains("no income recorded this month")));
        assertTrue(messages.stream()
                .anyMatch(msg -> msg.getMessage().contains("large individual expense")));
    }

    @Test
    void checkForOverallOverspending_ShouldHandleExactlyZeroIncome() {
        // Set up test data with exactly zero income and non-zero expense
        testExpense.setType(TransactionModel.TransactionType.EXPENSE);
        testExpense.setAmount(100.0F);

        testIncome.setType(TransactionModel.TransactionType.INCOME);
        testIncome.setAmount(0.0F);  // Explicitly set income to zero

        // Set up mock to return our test transactions
        when(transactionRepository.findByUserLoginAndDateBetween(
                eq(testUser.getLogin()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testExpense, testIncome));

        when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                any(), any(), any())).thenReturn(false);

        // Execute the method
        inboxMessageService.checkForOverallOverspending(testUser);

        // Capture and verify the messages
        verify(inboxMessageRepository, times(2)).save(messageCaptor.capture());
        List<InboxMessageModel> messages = messageCaptor.getAllValues();

        // Verify both types of messages were created
        assertTrue(messages.stream()
                .anyMatch(msg -> msg.getMessage().contains("no income recorded")));
        assertTrue(messages.stream()
                .anyMatch(msg -> msg.getMessage().contains("large individual expense")));
    }

    @Test
    void checkSpendingRules_ShouldHandleAllPossibleRuleTypes() {
        // Get all possible rule types
        UserRuleModel.RuleType[] ruleTypes = UserRuleModel.RuleType.values();

        for (UserRuleModel.RuleType ruleType : ruleTypes) {
            // Clear any previous interactions
            clearInvocations(inboxMessageRepository, transactionRepository);

            // Setup the rule
            testRule.setRuleType(ruleType);
            testRule.setCategory(TransactionModel.CategoryType.GROCERIES);
            testRule.setLimitAmount(1000.0F);

            if (ruleType == UserRuleModel.RuleType.NOT_EXCEED_CATEGORY) {
                testRule.setAdditionalCategory(TransactionModel.CategoryType.ENTERTAINMENT);
            }

            // Setup mock responses
            when(userRuleService.getRulesByUserLogin(testUser.getLogin()))
                    .thenReturn(Collections.singletonList(testRule));
            when(inboxMessageRepository.existsByUserAndMessageContainingAndTimestampAfter(
                    any(), any(), any())).thenReturn(false);

            // Setup transaction that would trigger the rule
            testExpense.setAmount(1500.0F);
            List<TransactionModel> transactions = Collections.singletonList(testExpense);
            when(transactionRepository.findByUserLoginAndDateBetween(
                    eq(testUser.getLogin()), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(transactions);

            // Execute the method
            inboxMessageService.checkSpendingRules(testUser);

            // Verify appropriate action was taken
            verify(inboxMessageRepository, atLeastOnce()).save(any());
        }
    }
}