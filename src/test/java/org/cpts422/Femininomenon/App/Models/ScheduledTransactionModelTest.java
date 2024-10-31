package org.cpts422.Femininomenon.App.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduledTransactionModelTest {
    @Mock
    UserModel userModel;
    @InjectMocks
    ScheduledTransactionModel scheduledTransaction;

    @BeforeEach
    void setUp() {
        scheduledTransaction = new ScheduledTransactionModel(userModel, "Weekly", LocalDateTime.now(), 1.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "test", ScheduledTransactionModel.TransactionType.EXPENSE, "checking");
    }
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

    @Nested
    class testSetters {
        @Test
        void testSetID() {
            assertDoesNotThrow(() -> scheduledTransaction.setId(1L));
            assertEquals(1L, scheduledTransaction.getId());
        }

        @Test
        void testSetUser() {
            assertDoesNotThrow(() -> scheduledTransaction.setUser(userModel));
            assertEquals(userModel, scheduledTransaction.getUser());
        }

        @Test
        void testSetFrequency() {
            assertDoesNotThrow(() -> scheduledTransaction.setFrequency("Monthly"));
            assertEquals("Monthly", scheduledTransaction.getFrequency());
        }

        @Test
        void testSetRecentPayment() {
            assertDoesNotThrow(() -> scheduledTransaction.setRecentPayment(LocalDateTime.now()));
            assertEquals(LocalDateTime.now(), scheduledTransaction.getRecentPayment());
        }

        @Test
        void testSetAmount() {
            assertDoesNotThrow(() -> scheduledTransaction.setAmount(1.0f));
            assertEquals(1.0f, scheduledTransaction.getAmount());
        }

        @Test
        void testSetDescription() {
            assertDoesNotThrow(() -> scheduledTransaction.setDescription("test"));
            assertEquals("test", scheduledTransaction.getDescription());
        }

        @Test
        void testSetCategory() {
            assertDoesNotThrow(() -> scheduledTransaction.setCategory(ScheduledTransactionModel.CategoryType.GROCERIES));
            assertEquals(ScheduledTransactionModel.CategoryType.GROCERIES, scheduledTransaction.getCategory());
        }

        @Test
        void testSetTransactionType() {
            assertDoesNotThrow(() -> scheduledTransaction.setType(ScheduledTransactionModel.TransactionType.EXPENSE));
            assertEquals(ScheduledTransactionModel.TransactionType.EXPENSE, scheduledTransaction.getType());
        }

        @Test
        void testSetAccount() {
            assertDoesNotThrow(() -> scheduledTransaction.setAccount("Savings"));
            assertEquals("Savings", scheduledTransaction.getAccount());
        }
    }
}
