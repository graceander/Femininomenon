package org.cpts422.Femininomenon.App.UnitTests.Controllers;

import org.cpts422.Femininomenon.App.Controllers.TransactionController;
import org.cpts422.Femininomenon.App.Models.TransactionModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.CurrencyConversionService;
import org.cpts422.Femininomenon.App.Service.TransactionService;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TransactionControllerTest {
    @Mock
    private TransactionService transactionService;
    @Mock
    private UsersService usersService;
    @Mock
    private CurrencyConversionService currencyConversionService;
    @Mock
    private Model model;
    @InjectMocks
    private TransactionController transactionController;
    private UserModel mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new UserModel();
        mockUser.setLogin("Matt");
        mockUser.setPassword("password");
        mockUser.setId(1);
    }
    @Test
    void testLogin() {
        String login = "Matt";
        String view = transactionController.goHome(login, model);
        assertEquals("redirect:/home?login=Matt", view);
    }

    @Test
    void testAddTransactionUserExists() {
        when(usersService.findByLogin("Matt")).thenReturn(mockUser);
        String view = transactionController.AddTransactionPage("Matt", model);
        verify(model).addAttribute("user", mockUser);
        assertEquals("addTransaction", view);
    }

    @Test
    void testAddTransactionUserNotFound() {
        when(usersService.findByLogin("unknownUser")).thenReturn(null);
        String view = transactionController.AddTransactionPage("unknownUser", model);
        verify(model).addAttribute("error", "User not found");
        assertEquals("error", view);
    }

    @Test
    void testSubmitTransaction() {
        when(usersService.findByLogin("Matt")).thenReturn(mockUser);
        String view = transactionController.submitTransaction("Matt", 100.0f, "Groceries", "Grocery shopping", TransactionModel.TransactionType.EXPENSE, "Cash");
        verify(transactionService, times(1)).saveTransaction(any(TransactionModel.class));
        assertEquals("redirect:/home?login=Matt", view);
    }

    @Test
    void testSubmitTransactionUserNotFound() {
        when(usersService.findByLogin("unknownUser")).thenReturn(null);
        String view = transactionController.submitTransaction("unknownUser", 100.0f, "Groceries", "Grocery shopping", TransactionModel.TransactionType.EXPENSE, "Cash");
        assertEquals("error", view);
    }

    @Test
    void testEditTransaction() {
        TransactionModel transaction = new TransactionModel(mockUser, LocalDateTime.now(), 100.0f, TransactionModel.CategoryType.GROCERIES, "Grocery shopping", TransactionModel.TransactionType.EXPENSE, "Cash");
        when(usersService.findByLogin("Matt")).thenReturn(mockUser);
        when(transactionService.getTransactionById(1L)).thenReturn(transaction);
        String view = transactionController.editTransactionPage(1L, "Matt", model);
        verify(model).addAttribute("transaction", transaction);
        verify(model).addAttribute("user", mockUser);
        assertEquals("editTransaction", view);
    }

    @Test
    void testEditTransactionNotBelongToUser() {
        UserModel mockUser = new UserModel();
        mockUser.setLogin("Matt");
        mockUser.setPassword("password");
        mockUser.setId(1);
        UserModel otherUser = new UserModel();
        otherUser.setLogin("OtherUser");
        otherUser.setPassword("password");
        otherUser.setId(2);
        TransactionModel transaction = new TransactionModel();
        transaction.setId(1L);
        transaction.setUser(otherUser);
        when(usersService.findByLogin("Matt")).thenReturn(mockUser);
        when(transactionService.getTransactionById(1L)).thenReturn(transaction);
        String view = transactionController.editTransactionPage(1L, "Matt", model);
        assertEquals("error", view);
        verify(model).addAttribute("error", "Transaction does not belong to the user");
    }


    @Test
    void testChangeCurrency() {
        when(usersService.findByLogin("Matt")).thenReturn(mockUser);
        String view = transactionController.changeCurrency("Matt", "EUR");
        verify(usersService).saveUser(mockUser);
        assertEquals("EUR", mockUser.getCurrency());
        assertEquals("redirect:/home?login=Matt", view);
    }
    @Test
    void testChangeCurrencyUserError() {
        when(usersService.findByLogin("UnknownUser")).thenReturn(null);
        String view = transactionController.changeCurrency("UnknownUser", "EUR");
        assertEquals("redirect:/home?login=UnknownUser", view);
    }
    @Test
    void testHomePage() {
        when(usersService.findByLogin("Matt")).thenReturn(mockUser);
        when(transactionService.getSpendingByCategory("Matt", "overall")).thenReturn(Map.of(TransactionModel.CategoryType.GROCERIES, 100.0));
        when(transactionService.getTotalSpending("Matt", "overall")).thenReturn(200.0);
        when(transactionService.getTransactionsByUser("Matt")).thenReturn(List.of(new TransactionModel(mockUser, LocalDateTime.now(), 100.0f, TransactionModel.CategoryType.GROCERIES, "Grocery shopping", TransactionModel.TransactionType.EXPENSE, "Cash")));
        when(currencyConversionService.convert(100.0, "USD", "USD")).thenReturn(100.0);
        when(currencyConversionService.convert(200.0, "USD", "USD")).thenReturn(200.0);
        String view = transactionController.homePage("Matt", "overall", model);
        verify(model).addAttribute("user", mockUser);
        verify(model).addAttribute("spendingByCategory", Map.of(TransactionModel.CategoryType.GROCERIES, 100.0));
        verify(model).addAttribute("totalSpending", 200.0);
        assertEquals("home", view);
    }

    @Test
    void testHomePageUserError() {
        when(usersService.findByLogin("UnknownUser")).thenReturn(null);
        String view = transactionController.homePage("UnknownUser", "overall", model);
        verify(model).addAttribute("error", "User not found");
        assertEquals("error", view);
    }

    @Test
    void testDeleteTransaction() {
        Long transactionId = 1L;
        String userLogin = "Matt";
        UserModel user = new UserModel();
        user.setLogin(userLogin);
        TransactionModel transaction = new TransactionModel(user, LocalDateTime.now(), 100.0f, TransactionModel.CategoryType.GROCERIES, "Grocery shopping", TransactionModel.TransactionType.EXPENSE, "Cash");
        when(transactionService.getTransactionById(transactionId)).thenReturn(transaction);
        String view = transactionController.deleteTransaction(transactionId, userLogin);
        verify(transactionService).removeTransaction(transaction);
        assertEquals("redirect:/home?login=Matt", view);
    }

    @Test
    void testDeleteTransactionError() {
        Long transactionId = 1L;
        String userLogin = "Matt";
        when(transactionService.getTransactionById(transactionId)).thenReturn(null);
        String view = transactionController.deleteTransaction(transactionId, userLogin);
        verify(transactionService, never()).removeTransaction(any());
        assertEquals("redirect:/home?login=Matt", view);
    }
    @Test
    void testDeleteTransactiondoesntBelong() {
        Long transactionId = 1L;
        String userLogin = "Matt";
        UserModel otherUser = new UserModel();
        otherUser.setLogin("OtherUser");
        TransactionModel transaction = new TransactionModel(otherUser, LocalDateTime.now(), 100.0f, TransactionModel.CategoryType.GROCERIES, "Grocery shopping", TransactionModel.TransactionType.EXPENSE, "Cash");
        when(transactionService.getTransactionById(transactionId)).thenReturn(transaction);
        String view = transactionController.deleteTransaction(transactionId, userLogin);
        verify(transactionService, never()).removeTransaction(any());
        assertEquals("redirect:/home?login=Matt", view);
    }


    @Test
    void testUpdateTransaction() {
        String userLogin = "Matt";
        Long transactionId = 1L;
        UserModel user = new UserModel();
        user.setLogin(userLogin);
        TransactionModel transaction = new TransactionModel(user, LocalDateTime.now(), 100.0f, TransactionModel.CategoryType.GROCERIES, "Grocery shopping", TransactionModel.TransactionType.EXPENSE, "Cash");
        when(transactionService.getTransactionById(transactionId)).thenReturn(transaction);
        String view = transactionController.updateTransaction(transactionId, userLogin, "2024-09-10T10:15:30", 200.0f, "Utilities", "Updated utility bill", TransactionModel.TransactionType.EXPENSE, "Bank", model);
        assertEquals("redirect:/home?login=Matt", view);
        assertEquals(200.0f, transaction.getAmount());
        assertEquals("Updated utility bill", transaction.getDescription());
        assertEquals(TransactionModel.CategoryType.UTILITIES, transaction.getCategory());
    }


    @Test
    void testUpdateNullTransaction() {
        Long transactionId = 1L;
        String userLogin = "Matt";
        when(transactionService.getTransactionById(transactionId)).thenReturn(null);
        String view = transactionController.updateTransaction(transactionId, userLogin, "2024-09-10T10:15:30", 200.0f, "Utilities", "Utility bill", TransactionModel.TransactionType.EXPENSE, "Bank", model);
        verify(model).addAttribute("error", "Transaction does not belong to the user");
        assertEquals("error", view);
    }

    @Test
    void testUpdateTransactionErrorCategory() {
        Long transactionId = 1L;
        String userLogin = "Matt";
        String invalidCategory = "NonExistentCategory";
        UserModel user = new UserModel();
        user.setLogin(userLogin);
        TransactionModel transaction = new TransactionModel(user, LocalDateTime.now(), 100.0f, TransactionModel.CategoryType.GROCERIES, "Grocery shopping", TransactionModel.TransactionType.EXPENSE, "Cash");
        when(transactionService.getTransactionById(transactionId)).thenReturn(transaction);
        String view = transactionController.updateTransaction(transactionId, userLogin, "2024-09-10T10:15:30", 200.0f, invalidCategory, "Utility bill", TransactionModel.TransactionType.EXPENSE, "Bank", model);
        verify(model).addAttribute("error", "Invalid category: " + invalidCategory);
        assertEquals("error", view);
    }



    @Test
    void testUpdateTransactionErrorUser() {
        UserModel otherUser = new UserModel();
        otherUser.setLogin("OtherUser");
        TransactionModel transaction = new TransactionModel(otherUser, LocalDateTime.now(), 100.0f,
                TransactionModel.CategoryType.GROCERIES, "Grocery shopping",
                TransactionModel.TransactionType.EXPENSE, "Cash");
        when(transactionService.getTransactionById(1L)).thenReturn(transaction);
        String view = transactionController.updateTransaction(1L, "Matt", "2024-09-10T10:15:30",
                200.0f, "Utilities", "Utility bill", TransactionModel.TransactionType.EXPENSE, "Bank", model);
        verify(model).addAttribute("error", "Transaction does not belong to the user");
        assertEquals("error", view);
    }

    @Test
    void testEditTransactionErrorUser() {
        Long transactionId = 1L;
        String login = "NonExistentUser";
        when(usersService.findByLogin(login)).thenReturn(null);
        String view = transactionController.editTransactionPage(transactionId, login, model);
        verify(model).addAttribute("error", "User not found");
        assertEquals("error", view);
    }

    @Test
    void testEditTransactionErrorNull() {
        Long transactionId = 1L;
        String login = "Matt";
        UserModel user = new UserModel();
        user.setId(1);
        when(usersService.findByLogin(login)).thenReturn(user);
        when(transactionService.getTransactionById(transactionId)).thenReturn(null);
        String view = transactionController.editTransactionPage(transactionId, login, model);
        verify(model).addAttribute("error", "Transaction does not belong to the user");
        assertEquals("error", view);
    }

}