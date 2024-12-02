package org.cpts422.Femininomenon.App.UnitTests.Controllers;

import org.cpts422.Femininomenon.App.Controllers.ScheduledTransactionController;
import org.cpts422.Femininomenon.App.Models.ScheduledTransactionModel;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.ScheduledTransactionService;
import org.cpts422.Femininomenon.App.Service.TransactionService;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ScheduledTransactionControllerTest {

    @Mock
    private ScheduledTransactionService scheduledTransactionService;
    @Mock
    private UsersService usersService;
    @Mock
    private TransactionService transactionService;
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
        @Test
        void testNormalViewScheduledTransactions() {
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            when(scheduledTransactionService.getTransactionsByUser("Bri")).thenReturn(List.of(new ScheduledTransactionModel(mockUser, "Weekly", LocalDateTime.now(), 100.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash")));
            String view = scheduledTransactionController.ViewScheduledTransactionsPage("Bri", model);
            assertEquals("viewScheduledTransactions", view);
        }

        @Test
        void testNullUseViewScheduledTransactions() {
            when(usersService.findByLogin("null")).thenReturn(null);
            String view = scheduledTransactionController.ViewScheduledTransactionsPage("Bri", model);
            verify(model).addAttribute("error", "User not found");
            assertEquals("error", view);
        }

        @Test
        void testNullTransactionsViewScheduledTransactions() {
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            when(scheduledTransactionService.getTransactionsByUser("Bri")).thenReturn(null);
            String view = scheduledTransactionController.ViewScheduledTransactionsPage("Bri", model);
            verify(model).addAttribute("message", "No scheduled transactions found for this user.");
            assertEquals("viewScheduledTransactions", view);
        }

        @Test
        void testEmptyTransactionsViewScheduledTransactions() {
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            when(scheduledTransactionService.getTransactionsByUser("Bri")).thenReturn(List.of());
            String view = scheduledTransactionController.ViewScheduledTransactionsPage("Bri", model);
            verify(model).addAttribute("message", "No scheduled transactions found for this user.");
            assertEquals("viewScheduledTransactions", view);
        }
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
            String view = scheduledTransactionController.submitScheduledTransaction("Bri", "Monthly", recentPayment, 100.0f, "Groceries", "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            verify(scheduledTransactionService, times(1)).saveTransaction(any(ScheduledTransactionModel.class));
            assertEquals("redirect:/viewScheduledTransactions?login=Bri", view);
        }

        @Test
        void testSubmitScheduledTransactionUserNotFound() {
            when(usersService.findByLogin("unknownUser")).thenReturn(null);
            String view = scheduledTransactionController.submitScheduledTransaction("unknownUser", "Weekly", recentPayment, 100.0f, "Groceries", "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            assertEquals("error", view);
        }

        @Test
        void testInvalidCategorySubmitScheduledTransaction() {
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            String view = scheduledTransactionController.submitScheduledTransaction("Bri", "Weekly", recentPayment, 100.0f, "Invalid", "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            assertEquals("error", view);
        }
    }

    @Nested
    class testEditScheduledTransactions {
        @Test
        void testNormalEditScheduledTransaction() {
            ScheduledTransactionModel scheduledTransaction = new ScheduledTransactionModel(mockUser, "Weekly", recentPayment, 100.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            when(scheduledTransactionService.getTransactionById(1L)).thenReturn(scheduledTransaction);
            String view = scheduledTransactionController.editTransactionPage(1L, "Bri", model);
            verify(model).addAttribute("scheduledTransaction", scheduledTransaction);
            verify(model).addAttribute("user", mockUser);
            assertEquals("editScheduledTransaction", view);
        }

        @Test
        void testWrongUserEditTransaction() {
            UserModel otherUser = new UserModel();
            otherUser.setLogin("OtherUser");
            otherUser.setPassword("password");
            otherUser.setId(2);
            ScheduledTransactionModel scheduledTransaction = new ScheduledTransactionModel();
            scheduledTransaction.setId(1L);
            scheduledTransaction.setUser(otherUser);
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            when(scheduledTransactionService.getTransactionById(1L)).thenReturn(scheduledTransaction);
            String view = scheduledTransactionController.editTransactionPage(1L, "Bri", model);
            assertEquals("error", view);
            verify(model).addAttribute("error", "Transaction does not belong to the user");
        }

        @Test
        void testNullUserEditTransaction() {
            when(usersService.findByLogin("Bri")).thenReturn(null);
            when(transactionService.getTransactionById(1L)).thenReturn(null);
            String view = scheduledTransactionController.editTransactionPage(1L, "Bri", model);
            verify(model).addAttribute("error", "User not found");
            assertEquals("error", view);
        }

        @Test
        void testNullTransactionEditTransaction() {
            ScheduledTransactionModel scheduledTransaction = new ScheduledTransactionModel(mockUser, "Weekly", recentPayment, 100.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            when(scheduledTransactionService.getTransactionById(1L)).thenReturn(null);
            String view = scheduledTransactionController.editTransactionPage(1L, "Bri", model);
            verify(model).addAttribute("error", "Transaction does not belong to the user");
            assertEquals("error", view);
        }
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
        @Test
        void testNormalUpdateScheduledTransaction() {
            ScheduledTransactionModel scheduledTransaction = new ScheduledTransactionModel(mockUser, "Weekly", recentPayment, 100.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            when(scheduledTransactionService.getTransactionById(1L)).thenReturn(scheduledTransaction);
            String view = scheduledTransactionController.updateScheduledTransaction(1L, "Bri", "Monthly", 10.f, "Utilities", "Updated utility bill", ScheduledTransactionModel.TransactionType.EXPENSE, "Bank", model);
            assertEquals("redirect:/viewScheduledTransactions?login=Bri", view);
            assertEquals(10.0f, scheduledTransaction.getAmount());
            assertEquals("Updated utility bill", scheduledTransaction.getDescription());
            assertEquals(ScheduledTransactionModel.CategoryType.UTILITIES, scheduledTransaction.getCategory());
        }

        @Test
        void testWrongUserUpdateTransaction() {
            UserModel otherUser = new UserModel();
            otherUser.setLogin("OtherUser");
            otherUser.setPassword("password");
            otherUser.setId(2);
            ScheduledTransactionModel scheduledTransaction = new ScheduledTransactionModel();
            scheduledTransaction.setId(1L);
            scheduledTransaction.setUser(otherUser);
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            when(scheduledTransactionService.getTransactionById(1L)).thenReturn(scheduledTransaction);
            String view = scheduledTransactionController.updateScheduledTransaction(1L, "Bri", "Monthly", 10.f, "Utilities", "Updated utility bill", ScheduledTransactionModel.TransactionType.EXPENSE, "Bank", model);
            assertEquals("error", view);
            verify(model).addAttribute("error", "Transaction does not belong to the user");
        }

        @Test
        void testNullTransactionUpdateTransaction() {
            ScheduledTransactionModel scheduledTransaction = new ScheduledTransactionModel(mockUser, "Weekly", recentPayment, 100.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            when(scheduledTransactionService.getTransactionById(1L)).thenReturn(null);
            String view = scheduledTransactionController.updateScheduledTransaction(1L, "Bri", "Monthly", 10.f, "Utilities", "Updated utility bill", ScheduledTransactionModel.TransactionType.EXPENSE, "Bank", model);
            verify(model).addAttribute("error", "Transaction does not belong to the user");
            assertEquals("error", view);
        }

        @Test
        void testInvalidCategoryUpdateTransaction() {
            ScheduledTransactionModel scheduledTransaction = new ScheduledTransactionModel(mockUser, "Weekly", recentPayment, 100.0f, ScheduledTransactionModel.CategoryType.GROCERIES, "Grocery shopping", ScheduledTransactionModel.TransactionType.EXPENSE, "Cash");
            when(usersService.findByLogin("Bri")).thenReturn(mockUser);
            String view = scheduledTransactionController.updateScheduledTransaction(1L, "Bri", "Monthly", 10.f, "Invalid_Category", "Updated utility bill", ScheduledTransactionModel.TransactionType.EXPENSE, "Bank", model);
            assertEquals("error", view);
        }
    }
}