package org.cpts422.Femininomenon.App.IntegrationTests;

import org.cpts422.Femininomenon.App.Controllers.InboxMessageController;
import org.cpts422.Femininomenon.App.Models.*;
import org.cpts422.Femininomenon.App.Repository.*;
import org.cpts422.Femininomenon.App.Service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InboxMessageContServTest {

    @Mock
    private InboxMessageRepository inboxMessageRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRuleService userRuleService;

    @Mock
    private UsersService usersService;

    @Mock
    private Model model;

    private InboxMessageService inboxMessageService;
    private InboxMessageController inboxMessageController;
    private UserModel testUser;


    /**
     * Sets up the test environment before each test method execution.
     * Initializes:
     * 1. InboxMessageService with necessary repository and service dependencies
     * 2. InboxMessageController with the message service and user service
     * 3. A test user with basic properties
     * This setup ensures each test starts with a clean and consistent state
     */
    @BeforeEach
    void setUp() {
        inboxMessageService = new InboxMessageService(
                inboxMessageRepository,
                transactionRepository,
                userRuleService
        );
        inboxMessageController = new InboxMessageController(inboxMessageService, usersService);

        testUser = new UserModel();
        testUser.setLogin("testUser");
    }

    /**
     * Tests the inbox viewing functionality.
     * Verifies that:
     * 1. The correct user's messages are retrieved from the repository
     * 2. The messages are properly added to the model
     * 3. The user login is added to the model
     * 4. The correct view name is returned
     * This test ensures the basic inbox viewing functionality works as expected with multiple messages present.
     */
    @Test
    void testViewInbox() {
        InboxMessageModel message1 = new InboxMessageModel(testUser, "Test message 1");
        message1.setId(1L);
        InboxMessageModel message2 = new InboxMessageModel(testUser, "Test message 2");
        message2.setId(2L);

        when(usersService.findByLogin("testUser")).thenReturn(testUser);
        when(inboxMessageRepository.findByUser(testUser))
                .thenReturn(Arrays.asList(message1, message2));

        String viewName = inboxMessageController.viewInbox("testUser", model);

        verify(usersService).findByLogin("testUser");
        verify(inboxMessageRepository).findByUser(testUser);
        verify(model).addAttribute(eq("messages"), eq(Arrays.asList(message1, message2)));
        verify(model).addAttribute("userLogin", "testUser");
        assertEquals("userInbox", viewName);
    }

    /**
     * Tests marking a single message as read.
     * Verifies that:
     * 1. The correct message is retrieved from the repository
     * 2. The message's read status is updated
     * 3. The updated message is saved back to the repository
     * 4. The user is redirected to the correct URL
     * This test ensures proper handling of marking individual messages as read,
     * including verification of the message's state change.
     */
    @Test
    void testMarkMessageAsRead() {
        InboxMessageModel message = new InboxMessageModel(testUser, "Test message");
        message.setId(1L);
        message.setRead(false);

        when(inboxMessageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(inboxMessageRepository.save(any(InboxMessageModel.class))).thenReturn(message);

        String redirectUrl = inboxMessageController.markMessageAsRead(1L, "testUser");

        verify(inboxMessageRepository).findById(1L);
        verify(inboxMessageRepository).save(argThat(savedMessage ->
                savedMessage.getId() == 1L && savedMessage.isRead()
        ));
        assertEquals("redirect:/user/inbox/view?userLogin=testUser", redirectUrl);
    }

    /**
     * Tests marking all unread messages as read for a specific user.
     * Verifies that:
     * 1. All unread messages for the user are retrieved
     * 2. Each message's read status is updated
     * 3. All updated messages are saved back to the repository
     * 4. The user is redirected to the correct URL
     * This test ensures batch operations on messages work correctly and
     * maintains data consistency across multiple message updates.
     */
    @Test
    void testMarkAllMessagesAsRead() {
        // Setup
        InboxMessageModel message1 = new InboxMessageModel(testUser, "Message 1");
        InboxMessageModel message2 = new InboxMessageModel(testUser, "Message 2");
        when(usersService.findByLogin("testUser")).thenReturn(testUser);
        when(inboxMessageRepository.findByUserAndIsReadFalse(testUser)).thenReturn(Arrays.asList(message1, message2));

        String redirectUrl = inboxMessageController.markAllMessagesAsRead("testUser");

        verify(usersService).findByLogin("testUser");
        verify(inboxMessageRepository).findByUserAndIsReadFalse(testUser);
        verify(inboxMessageRepository).saveAll(argThat(messages -> {
            if (messages instanceof Collection) {
                return ((Collection<?>) messages).stream()
                        .allMatch(msg -> msg instanceof InboxMessageModel &&
                                ((InboxMessageModel) msg).isRead());
            }
            return false;
        }));
        assertEquals("redirect:/user/inbox/view?userLogin=testUser", redirectUrl);
    }

    /**
     * Tests error handling when attempting to mark a non-existent message as read.
     * Verifies that:
     * 1. Attempting to mark a non-existent message throws a RuntimeException
     * 2. The repository is queried with the correct ID
     * 3. No save operation is attempted on the repository
     * This test ensures proper error handling and system stability when dealing with invalid or non-existent message IDs.
     */
    @Test
    void testMarkMessageAsReadNonExistent() {
        when(inboxMessageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            inboxMessageController.markMessageAsRead(999L, "testUser");
        });
        verify(inboxMessageRepository).findById(999L);
        verify(inboxMessageRepository, never()).save(any());
    }
}