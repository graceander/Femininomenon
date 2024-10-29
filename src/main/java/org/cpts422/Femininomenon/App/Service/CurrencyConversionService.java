package org.cpts422.Femininomenon.App.Service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyConversionService {
    private final Map<String, Double> exchangeRates;

    public CurrencyConversionService() {
        exchangeRates = new HashMap<>();
        // Exchange rates relative to USD (as of a certain date)
        exchangeRates.put("USD", 1.0);
        exchangeRates.put("EUR", 0.85);
        exchangeRates.put("GBP", 0.73);
        exchangeRates.put("JPY", 110.0);
        // Add more currencies as needed
    }

    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (!exchangeRates.containsKey(fromCurrency) || !exchangeRates.containsKey(toCurrency)) {
            throw new IllegalArgumentException("Unsupported currency");
        }
        double fromRate = exchangeRates.get(fromCurrency);
        double toRate = exchangeRates.get(toCurrency);
        return amount * (toRate / fromRate);
    }
}
