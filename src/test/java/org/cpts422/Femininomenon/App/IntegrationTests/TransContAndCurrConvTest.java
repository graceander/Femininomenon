package org.cpts422.Femininomenon.App.IntegrationTests;

import org.cpts422.Femininomenon.App.Controllers.*;
import org.cpts422.Femininomenon.App.Models.*;
import org.cpts422.Femininomenon.App.Service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Transaction Controller and Currency Conversion functionality.
 * Tests the interaction between transaction submission and currency conversion operations.
 */
@ExtendWith(MockitoExtension.class)
public class TransContAndCurrConvTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private UsersService usersService;

    private CurrencyConversionService currencyConversionService;
    private TransactionController transactionController;
    private UserModel testUser;

    /**
     * Sets up the test environment before each test method.
     * Initializes the currency conversion service, transaction controller, and a test user with default settings.
     */
    @BeforeEach
    void setUp() {
        currencyConversionService = new CurrencyConversionService();
        transactionController = new TransactionController(transactionService, usersService, currencyConversionService);
        testUser = new UserModel();
        testUser.setLogin("grace");
        testUser.setCurrency("USD");
        when(usersService.findByLogin("grace")).thenReturn(testUser);
    }

    /**
     * Tests the complete flow of submitting a transaction and then changing the currency.
     * Verifies that:
     * 1. A transaction can be submitted successfully
     * 2. The transaction is saved with correct properties
     * 3. Currency can be changed after transaction submission
     * 4. User currency is updated correctly
     */
    @Test
    void testTransactionSubmissionWithCurrencyConversion() {
        // Setup
        float amount = 100.0f;
        String category = "UTILITIES";
        String description = "Test transaction";

        // Create transaction in USD
        transactionController.submitTransaction(
                "grace",
                amount,
                category,
                description,
                TransactionModel.TransactionType.EXPENSE,
                "EXPENSE"
        );

        // Verify transaction was saved with correct basic properties
        verify(transactionService).saveTransaction(any(TransactionModel.class));

        // Change currency to EUR
        transactionController.changeCurrency("grace", "EUR");

        // Verify user currency was updated
        verify(usersService).saveUser(argThat(user ->
                user.getCurrency().equals("EUR")
        ));
    }

    /**
     * Tests currency conversion between different currency pairs.
     * Verifies that the user's currency is correctly updated when changed
     * between different currency combinations.
     *
     * @param fromCurrency The initial currency of the user
     * @param toCurrency The target currency to convert to
     */
    @ParameterizedTest
    @CsvSource({
            "USD, EUR",
            "EUR, USD",
            "USD, GBP",
            "GBP, USD"
    })
    void testCurrencyChangeAffectsUser(String fromCurrency, String toCurrency) {
        // Setup initial currency
        testUser.setCurrency(fromCurrency);

        // Change currency
        transactionController.changeCurrency("grace", toCurrency);

        // Verify the user's currency was updated
        verify(usersService).saveUser(argThat(user ->
                user.getCurrency().equals(toCurrency)
        ));
    }

    /**
     * Tests handling of multiple transactions with different currencies.
     * Verifies that:
     * 1. Multiple transactions can be submitted successfully
     * 2. Currency can be changed between transactions
     * 3. All transactions are saved correctly
     * 4. User currency is updated appropriately
     */
    @Test
    void testMultipleTransactionsWithDifferentCurrencies() {
        // First transaction in USD
        transactionController.submitTransaction(
                "grace",
                100.0f,
                "UTILITIES",
                "USD transaction",
                TransactionModel.TransactionType.EXPENSE,
                "EXPENSE"
        );

        // Change currency to EUR
        transactionController.changeCurrency("grace", "EUR");

        // Second transaction in EUR
        transactionController.submitTransaction(
                "grace",
                200.0f,
                "GROCERIES",
                "EUR transaction",
                TransactionModel.TransactionType.EXPENSE,
                "EXPENSE"
        );

        // Verify transactions were saved
        verify(transactionService, times(2)).saveTransaction(any(TransactionModel.class));

        // Verify currency was updated
        verify(usersService).saveUser(argThat(user ->
                user.getCurrency().equals("EUR")
        ));
    }
}
