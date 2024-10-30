/**
 * Grace Anderson
 * 11759304
 */
package org.cpts422.Femininomenon.App.Controllers;

// Imports
import org.cpts422.Femininomenon.App.Models.InboxMessageModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.InboxMessageService;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Test class for the InboxMessageController class using JUnit 5 and Mockito
 * Tests the functionality of handling inbox messages
 */
@ExtendWith(MockitoExtension.class)
class InboxMessageControllerTest {

    // Mock dependencies used by the controller
    @Mock
    private InboxMessageService inboxMessageService;

    @Mock
    private UsersService usersService;

    @Mock
    private Model model;

    // The controller instance to be tested
    private InboxMessageController inboxMessageController;

    /**
     * Set up method to run before each test
     * Initializes a new controller instance with mocked dependencies
     */
    @BeforeEach
    void setUp() {
        inboxMessageController = new InboxMessageController(inboxMessageService, usersService);
    }

    /**
     * Tests the viewInbox method of the controller
     * Verifying:
     * 1. The correct view name is returned
     * 2. The model is populated with required attributes
     * 3. The appropriate service methods are called
     */
    @Test
    void viewInbox_ShouldReturnUserInboxViewWithMessages() {
        String userLogin = "testUser";
        UserModel user = new UserModel();
        List<InboxMessageModel> messages = new ArrayList<>();

        // Configure mock behavior
        when(usersService.findByLogin(userLogin)).thenReturn(user);
        when(inboxMessageService.getInboxMessages(user)).thenReturn(messages);

        String viewName = inboxMessageController.viewInbox(userLogin, model);

        assertEquals("userInbox", viewName);
        verify(model).addAttribute("messages", messages);
        verify(model).addAttribute("userLogin", userLogin);
        verify(usersService).findByLogin(userLogin);
        verify(inboxMessageService).getInboxMessages(user);
    }

    /**
     * Tests the markMessagesAsRead method of the controller
     * Verifying:
     * 1. The correct redirect URL is returned
     * 2. The message is marked as read via the service
     */
    @Test
    void markMessageAsRead_ShouldMarkMessageAndRedirect() {
        Long messageId = 1L;
        String userLogin = "testUser";

        String redirectUrl = inboxMessageController.markMessageAsRead(messageId, userLogin);

        assertEquals("redirect:/user/inbox/view?userLogin=" + userLogin, redirectUrl);
        verify(inboxMessageService).markMessageAsRead(messageId);
    }

    /**
     * Tests the markAllMessagesAsRead method of the controller
     * Verifying:
     * 1. The correct redirect URL is returned
     * 2. The user is looked up
     * 2. All messages for the user are read
     */
    @Test
    void markAllMessagesAsRead_ShouldMarkAllMessagesAndRedirect() {
        String userLogin = "testUser";
        UserModel user = new UserModel();

        // Configure mock behavior
        when(usersService.findByLogin(userLogin)).thenReturn(user);

        String redirectUrl = inboxMessageController.markAllMessagesAsRead(userLogin);

        assertEquals("redirect:/user/inbox/view?userLogin=" + userLogin, redirectUrl);
        verify(usersService).findByLogin(userLogin);
        verify(inboxMessageService).markAllMessagesAsRead(user);
    }
}