package org.cpts422.Femininomenon.App.IntegrationTests;

import org.cpts422.Femininomenon.App.Models.ScheduledTransactionModel;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Repository.ScheduledTransactionRepository;
import org.cpts422.Femininomenon.App.Service.ScheduledTransactionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class STServiceAndTModelPairwiseTests {

    @Mock
    private ScheduledTransactionModel mockScheduledTransactionModel;

    @Mock
    private ScheduledTransactionRepository scheduledTransactionRepository;

    @InjectMocks
    ScheduledTransactionService scheduledTransactionService;
    TransactionModel transactionModel;

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
            when(mockScheduledTransactionModel.getType()).thenReturn(stType);
            TransactionModel.TransactionType testType = scheduledTransactionService.convertScheduledTypeToTransactionType(mockScheduledTransactionModel);
            assertEquals(tType, testType);
            verify(mockScheduledTransactionModel).getType();
        }

        @ParameterizedTest
        @MethodSource("scheduledCategoryTypes")
        void testConvertScheduledCategoryToTransactionCategory(ScheduledTransactionModel.CategoryType stCategory, TransactionModel.CategoryType tCategory) {
            when(mockScheduledTransactionModel.getCategory()).thenReturn(stCategory);
            TransactionModel.CategoryType testCategory = scheduledTransactionService.convertScheduledCategoryToTransactionCategory(mockScheduledTransactionModel);
            assertEquals(tCategory, testCategory);
            verify(mockScheduledTransactionModel).getCategory();
        }
    }
}
