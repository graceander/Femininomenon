package org.cpts422.Femininomenon.App.Service;

import org.cpts422.Femininomenon.App.Models.ScheduledTransactionModel;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Repository.ScheduledTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScheduledTransactionServiceTest {

    @Mock
    private UserModel userModel;

    @Mock
    private ScheduledTransactionRepository scheduledTransactionRepository;

    @InjectMocks
    ScheduledTransactionService scheduledTransactionService;

    @BeforeEach
    void setUp() {
        scheduledTransactionService = new ScheduledTransactionService(scheduledTransactionRepository);
    }

    @Nested
    class testRepository {

        @Test
        void testGetTransactionsByUser() {
            String login = "Bri";

            ScheduledTransactionModel testSTM1 = new ScheduledTransactionModel();
            ScheduledTransactionModel testSTM2 = new ScheduledTransactionModel();
            when(scheduledTransactionRepository.findByUserLogin(login)).thenReturn(Arrays.asList(testSTM1, testSTM2));
            List<ScheduledTransactionModel> result = scheduledTransactionService.getTransactionsByUser(login);
            assertEquals(Arrays.asList(testSTM1, testSTM2), result);
        }

        @Test
        void testSaveTransaction() {
            ScheduledTransactionModel testSTM1 = new ScheduledTransactionModel();

            scheduledTransactionService.saveTransaction(testSTM1);

            verify(scheduledTransactionRepository).save(testSTM1);
        }

        @Test
        void testRemoveTransaction() {
            ScheduledTransactionModel testSTM1 = new ScheduledTransactionModel();

            scheduledTransactionService.removeTransaction(testSTM1);

            verify(scheduledTransactionRepository).delete(testSTM1);
        }

        @Test
        void testGetTransactionByID() {
            Long id = 1L;
            ScheduledTransactionModel expectedScheduledTransaction = new ScheduledTransactionModel();
            when(scheduledTransactionRepository.findById(id)).thenReturn(Optional.of(expectedScheduledTransaction));

            ScheduledTransactionModel result = scheduledTransactionService.getTransactionById(id);

            assertEquals(expectedScheduledTransaction, result);
        }
    }

    @Nested
    class testEnumTypeConverters {

        // MethodSource for transaction types
        private static Stream<Object> scheduledTransactionTypes() {
            return Stream.of(
                    new Object[]{ScheduledTransactionModel.TransactionType.INCOME, TransactionModel.TransactionType.INCOME},
                    new Object[]{ScheduledTransactionModel.TransactionType.EXPENSE, TransactionModel.TransactionType.EXPENSE}
            );
        }

        // MethodSource for category types
        private static Stream<Object> scheduledCategoryTypes() {
            return Stream.of(
                    new Object[]{ScheduledTransactionModel.CategoryType.UTILITIES, TransactionModel.CategoryType.UTILITIES},
                    new Object[]{ScheduledTransactionModel.CategoryType.GROCERIES, TransactionModel.CategoryType.GROCERIES},
                    new Object[]{ScheduledTransactionModel.CategoryType.OTHER, TransactionModel.CategoryType.OTHER},
                    new Object[]{ScheduledTransactionModel.CategoryType.SALARY, TransactionModel.CategoryType.SALARY},
                    new Object[]{ScheduledTransactionModel.CategoryType.TRANSPORTATION, TransactionModel.CategoryType.TRANSPORTATION},
                    new Object[]{ScheduledTransactionModel.CategoryType.ENTERTAINMENT, TransactionModel.CategoryType.ENTERTAINMENT},
                    new Object[]{ScheduledTransactionModel.CategoryType.HEALTHCARE, TransactionModel.CategoryType.HEALTHCARE}
            );
        }

        @ParameterizedTest
        @MethodSource("scheduledTransactionTypes")
        void testConvertScheduledTypeToTransactionType(ScheduledTransactionModel.TransactionType stType, TransactionModel.TransactionType tType) {
            ScheduledTransactionModel testSTM1 = new ScheduledTransactionModel();
            testSTM1.setType(stType);
            TransactionModel.TransactionType testType = scheduledTransactionService.convertScheduledTypeToTransactionType(testSTM1);
            assertEquals(tType, testType);
        }

        @Test
        void testConvertScheduledTypeToTransactionTypeThrowsError() {
            ScheduledTransactionModel testSTM1 = new ScheduledTransactionModel();
            assertThrows(IllegalArgumentException.class, () -> scheduledTransactionService.convertScheduledTypeToTransactionType(testSTM1));
        }

        @ParameterizedTest
        @MethodSource("scheduledCategoryTypes")
        void testConvertScheduledCategoryToTransactionCategory(ScheduledTransactionModel.CategoryType stCategory, TransactionModel.CategoryType tCategory) {
            ScheduledTransactionModel testSTM1 = new ScheduledTransactionModel();
            testSTM1.setCategory(stCategory);
            TransactionModel.CategoryType testCategory = scheduledTransactionService.convertScheduledCategoryToTransactionCategory(testSTM1);
            assertEquals(tCategory, testCategory);
        }

        @Test
        void testConvertScheduledCategoryToTransactionCategoryThrowsError() {
            ScheduledTransactionModel testSTM1 = new ScheduledTransactionModel();
            assertThrows(IllegalArgumentException.class, () -> scheduledTransactionService.convertScheduledCategoryToTransactionCategory(testSTM1));
        }
    }

    @Nested
    class testCreatingTransactions {
        // MethodSource for finding next payment date
        private static Stream<String> frequencies() {
            return Stream.of(
                    "Weekly",
                    "Biweekly",
                    "Monthly",
                    "Custom",
                    "Invalid"
            );
        }

        @ParameterizedTest
        @MethodSource("frequencies")
        void testFindNextPaymentDate(String frequency) {
            ScheduledTransactionModel testSTM1 = new ScheduledTransactionModel();
            LocalDateTime testPaymentDate = LocalDateTime.of(2024, Month.JANUARY, 2, 0, 0, 0);
            testSTM1.setFrequency(frequency);
            testSTM1.setRecentPayment(testPaymentDate);

            if (frequency.equals("Weekly")){
                assertEquals(testPaymentDate.plusWeeks(1), scheduledTransactionService.findNextPaymentDate(testSTM1));
            }
            else if (frequency.equals("Biweekly")){
                assertEquals(testPaymentDate.plusWeeks(2), scheduledTransactionService.findNextPaymentDate(testSTM1));
            }
            else if (frequency.equals("Monthly") || frequency.equals("Custom")){
                assertEquals(testPaymentDate.plusMonths(1), scheduledTransactionService.findNextPaymentDate(testSTM1));
            }
            else {
                assertThrows(IllegalArgumentException.class, () -> scheduledTransactionService.findNextPaymentDate(testSTM1));
            }
        }

        @Test
        void testOnCreateScheduledTransaction() {
            ScheduledTransactionModel testSTM1 = new ScheduledTransactionModel(userModel, "Monthly", LocalDateTime.of(2024, Month.JANUARY, 2, 0, 0, 0), 1.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "test", ScheduledTransactionModel.TransactionType.EXPENSE, "checking");
            List<TransactionModel> testYear = new ArrayList<>();
            for (Month month : Month.values()) {
                LocalDateTime transactionDate = LocalDateTime.of(2024, month, 2, 0, 0, 0);
                TransactionModel testTransaction = new TransactionModel(userModel, transactionDate, 1.0f, TransactionModel.CategoryType.GROCERIES, "Scheduled Transaction: test", TransactionModel.TransactionType.EXPENSE, "checking" );
                testYear.add(testTransaction);
            }

            assertEquals(testYear, scheduledTransactionService.onCreateScheduledTransaction(testSTM1));

        }

        @Test
        void testCreateTransaction() {
            LocalDateTime transactionDate = LocalDateTime.of(2024, Month.JANUARY, 2, 0, 0, 0);
            ScheduledTransactionModel testSTM1 = new ScheduledTransactionModel(userModel, "Weekly", transactionDate, 1.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "test", ScheduledTransactionModel.TransactionType.EXPENSE, "checking");
            TransactionModel testTransaction = new TransactionModel(userModel, transactionDate.plusWeeks(1), 1.0f, TransactionModel.CategoryType.GROCERIES, "Scheduled Transaction: test", TransactionModel.TransactionType.EXPENSE, "checking" );
            assertEquals(testTransaction, scheduledTransactionService.createTransaction(testSTM1));
        }
    }
}
