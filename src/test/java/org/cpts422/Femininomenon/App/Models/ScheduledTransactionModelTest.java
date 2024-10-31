package org.cpts422.Femininomenon.App.Models;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduledTransactionModelTest {
    @Mock
    UserModel userModel;
    @InjectMocks
    ScheduledTransactionModel scheduledTransactionModel;

    @Test
    void testDefaultConstructor() {
        ScheduledTransactionModel scheduledTransactionModel = new ScheduledTransactionModel();
        assertNotNull(scheduledTransactionModel);
    }
    @Test
    void testParameterizedConstructor() {
        ScheduledTransactionModel scheduledTransactionModel = new ScheduledTransactionModel(userModel, "Weekly", LocalDateTime.now(), 1.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "test", ScheduledTransactionModel.TransactionType.EXPENSE, "checking");
        assertAll( () -> {
            assertEquals(userModel, scheduledTransactionModel.getUser());
            assertEquals("Weekly", scheduledTransactionModel.getFrequency());
            assertNotNull(scheduledTransactionModel.getRecentPayment());
            assertEquals(1.0f, scheduledTransactionModel.getAmount());
            assertEquals("Scheduled Transaction: test", scheduledTransactionModel.getDescription());
            assertEquals(ScheduledTransactionModel.CategoryType.GROCERIES, scheduledTransactionModel.getCategory());
            assertEquals(ScheduledTransactionModel.TransactionType.EXPENSE, scheduledTransactionModel.getType());
            assertEquals("checking", scheduledTransactionModel.getAccount());
        });
    }
}
