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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TransContAndCurrConvTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private UsersService usersService;

    private CurrencyConversionService currencyConversionService;
    private TransactionController transactionController;
    private UserModel testUser;

    @BeforeEach
    void setUp() {
        currencyConversionService = new CurrencyConversionService();
        transactionController = new TransactionController(transactionService, usersService, currencyConversionService);

        testUser = new UserModel();
        testUser.setLogin("grace");
        testUser.setCurrency("USD");

        // This stub is needed for both tests
        when(usersService.findByLogin("grace")).thenReturn(testUser);
    }

    @ParameterizedTest
    @CsvSource({
            "USD, EUR, 100.0",
            "USD, GBP, 200.0",
            "EUR, USD, 85.0",
            "GBP, JPY, 150.0"
    })
    void testCurrencyConversionIntegration(String fromCurrency, String toCurrency, double amount) {
        // Set initial currency
        testUser.setCurrency(fromCurrency);

        // Change currency
        transactionController.changeCurrency("grace", toCurrency);

        // Verify user currency was updated
        verify(usersService).saveUser(argThat(user ->
                user.getCurrency().equals(toCurrency)
        ));
    }

    @Test
    void testZeroAmountTransactionWithCurrencyChange() {
        // Create transaction with zero amount
        transactionController.submitTransaction(
                "grace",
                0.0f,
                "UTILITIES",
                "Zero amount test",
                TransactionModel.TransactionType.EXPENSE,
                "EXPENSE"
        );

        // Change currency and verify user update
        transactionController.changeCurrency("grace", "EUR");
        verify(usersService).saveUser(argThat(user ->
                user.getCurrency().equals("EUR")
        ));
    }
}
