package org.cpts422.Femininomenon.App.Controllers;

import org.cpts422.Femininomenon.App.Models.ScheduledTransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.ScheduledTransactionService;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ScheduledTransactionControllerTest {

    @Mock
    private ScheduledTransactionService scheduledTransactionService;
    @Mock
    private UsersService usersService;
    @Mock
    private Model model;
    @InjectMocks
    private ScheduledTransactionController scheduledTransactionController;
    private UserModel mockUser;

    private LocalDateTime recentPayment;

    @BeforeEach
    void setUp() {
        recentPayment = LocalDateTime.of(2024, Month.JANUARY, 2, 0, 0, 0);
        MockitoAnnotations.openMocks(this);
        mockUser = new UserModel();
        mockUser.setLogin("Bri");
        mockUser.setPassword("password");
        mockUser.setId(1);
    }

    @Nested
    class testViewScheduledTransactions {

    }

    @Nested
    class testAddScheduledTransactions {
        @Test
        void testNormalAddScheduledTransaction() {
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            String view = scheduledTransactionController.AddScheduledTransactionPage("Bri", model);
            verify(model).addAttribute("user", mockUser);
            assertEquals("addScheduledTransaction", view);
        }

        @Test
        void testAddScheduledTransactionUserNotFound() {
            when(usersService.findByLogin("unknownUser")).thenReturn(null);
            String view = scheduledTransactionController.AddScheduledTransactionPage("unknownUser", model);
            verify(model).addAttribute("error", "User not found");
            assertEquals("error", view);
        }
    }

    @Nested
    class testSubmitScheduledTransactions {
        @Test
        void testNormalSubmitScheduledTransaction() {
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            String view = scheduledTransactionController.submitScheduledTransaction("Bri", "Weekly", recentPayment, 100.0f, "Groceries", "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            verify(scheduledTransactionService, times(1)).saveTransaction(any(ScheduledTransactionModel.class));
            assertEquals("redirect:/viewScheduledTransactions?login=Bri", view);
        }

        @Test
        void testSubmitScheduledTransactionUserNotFound() {
            when(usersService.findByLogin("unknownUser")).thenReturn(null);
            String view = scheduledTransactionController.submitScheduledTransaction("unknownUser", "Weekly", recentPayment, 100.0f, "Groceries", "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            assertEquals("error", view);
        }
    }

    @Nested
    class testEditScheduledTransactions {

    }

    @Nested
    class testDeleteScheduledTransactions {
        @Test
        void testNormalDeleteScheduledTransaction() {
            Long transactionId = 1L;
            String userLogin = "Bri";
            UserModel user = new UserModel();
            user.setLogin(userLogin);
            ScheduledTransactionModel scheduledTransaction = new ScheduledTransactionModel(user, "Weekly", LocalDateTime.now(), 100.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            when(scheduledTransactionService.getTransactionById(transactionId)).thenReturn(scheduledTransaction);
            String view = scheduledTransactionController.deleteScheduledTransaction(transactionId, userLogin);
            verify(scheduledTransactionService).removeTransaction(scheduledTransaction);
            assertEquals("redirect:/viewScheduledTransactions?login=Bri", view);
        }

        @Test
        void testCannotFindTransactionToDelete() {
            Long transactionId = 1L;
            String userLogin = "Bri";
            when(scheduledTransactionService.getTransactionById(transactionId)).thenReturn(null);
            String view = scheduledTransactionController.deleteScheduledTransaction(transactionId, userLogin);
            verify(scheduledTransactionService, never()).removeTransaction(any());
            assertEquals("redirect:/viewScheduledTransactions?login=Bri", view);
        }

        @Test
        void testScheduledTransactionBelongsToAnotherUser() {
            Long transactionId = 1L;
            String userLogin = "Bri";
            UserModel otherUser = new UserModel();
            otherUser.setLogin("OtherUser");
            ScheduledTransactionModel scheduledTransaction = new ScheduledTransactionModel(otherUser, "Weekly", LocalDateTime.now(), 100.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            when(scheduledTransactionService.getTransactionById(transactionId)).thenReturn(scheduledTransaction);
            String view = scheduledTransactionController.deleteScheduledTransaction(transactionId, userLogin);
            verify(scheduledTransactionService, never()).removeTransaction(any());
            assertEquals("redirect:/viewScheduledTransactions?login=Bri", view);
        }
    }

    @Nested
    class testUpdateScheduledTransactions {
    }

}