/**
 * Grace Anderson
 * 11759304
 */
package org.cpts422.Femininomenon.App.UnitTests.Service;

import org.cpts422.Femininomenon.App.Service.CurrencyConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CurrencyConversionService.
 * Verifies the currency conversion functionality including valid conversions,
 * edge cases, and error conditions.
 */
class CurrencyConversionServiceTest {
    private CurrencyConversionService currencyConversionService;

    /**
     * Sets up a fresh instance of CurrencyConversionService before each test.
     * This ensures each test starts with a clean state.
     */
    @BeforeEach
    void setUp() {
        currencyConversionService = new CurrencyConversionService();
    }

    /**
     * Tests basic currency conversion between USD and EUR in both directions.
     * Verifies that:
     * 1. Converting from USD to EUR uses the correct exchange rate
     * 2. Converting from EUR to USD uses the inverse rate correctly
     * Expected rates: 1 USD = 0.85 EUR, therefore 1 EUR ≈ 1.1765 USD
     */
    @Test
    void convert_ValidCurrencies_SuccessfulConversion() {
        // Test conversion from USD to EUR
        double amount = 100.0;
        double converted = currencyConversionService.convert(amount, "USD", "EUR");
        assertEquals(85.0, converted, 0.001);

        // Test conversion from EUR to USD
        converted = currencyConversionService.convert(amount, "EUR", "USD");
        assertEquals(117.64705882352942, converted, 0.001);
    }

    /**
     * Tests conversion between the same currency.
     * Verifies that converting an amount to the same currency
     * returns the original amount unchanged.
     */
    @Test
    void convert_SameCurrency_ReturnsOriginalAmount() {
        double amount = 100.0;
        double converted = currencyConversionService.convert(amount, "USD", "USD");
        assertEquals(amount, converted, 0.001);
    }

    /**
     * Tests error handling when an invalid source currency is provided.
     * Verifies that the service throws an IllegalArgumentException
     * with the correct error message.
     */
    @Test
    void convert_InvalidFromCurrency_ThrowsException() {
        double amount = 100.0;
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            currencyConversionService.convert(amount, "INVALID", "USD");
        });
        assertEquals("Unsupported currency", exception.getMessage());
    }

    /**
     * Tests error handling when an invalid target currency is provided.
     * Verifies that the service throws an IllegalArgumentException
     * with the correct error message.
     */
    @Test
    void convert_InvalidToCurrency_ThrowsException() {
        double amount = 100.0;
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            currencyConversionService.convert(amount, "USD", "INVALID");
        });
        assertEquals("Unsupported currency", exception.getMessage());
    }

    /**
     * Tests conversion between two currencies that require cross-rate calculation.
     * Verifies that converting between GBP and JPY uses correct intermediate
     * calculations through the base currency (USD).
     * Exchange rates used:
     * - 1 GBP = 1/0.73 USD ≈ 1.37 USD
     * - 1 USD = 110.0 JPY
     * Therefore: 1 GBP = 110.0/0.73 JPY ≈ 150.68 JPY
     */
    @Test
    void convert_CrossCurrencyConversion_SuccessfulConversion() {
        // Test conversion from GBP to JPY
        double amount = 100.0;
        double converted = currencyConversionService.convert(amount, "GBP", "JPY");
        // Calculate expected value: 100 * (110.0/0.73) = 15068.49315068493
        assertEquals(15068.49315068493, converted, 0.001);
    }
}