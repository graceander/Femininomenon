/**
 * Grace Anderson
 * 11759304
 */
package org.cpts422.Femininomenon.App.UnitTests.Models;

import org.cpts422.Femininomenon.App.Models.InboxMessageModel;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for InboxMessageModel using JUnit 5.
 * Tests the functionality of the inbox message entity model including:
 * - Constructor behavior
 * - Getter/setter operations
 * - Timestamp initialization
 * - Read/unread status management
 */
class InboxMessageModelTest {

    /**
     * Tests the default constructor to ensure it properly initializes
     * all fields with appropriate default values.
     * Expected behavior:
     * - Timestamp should be initialized (not null)
     * - isRead should default to false
     * - Message content should be null
     * - User reference should be null
     */
    @Test
    void defaultConstructor_ShouldInitializeWithDefaultValues() {
        InboxMessageModel message = new InboxMessageModel();

        assertNotNull(message.getTimestamp(), "Timestamp should be initialized");
        assertFalse(message.isRead(), "isRead should be initialized to false");
        assertNull(message.getMessage(), "Message should be null");
        assertNull(message.getUser(), "User should be null");
    }

    /**
     * Tests the parameterized constructor to ensure it correctly
     * initializes fields with provided values while maintaining
     * appropriate defaults for other fields.
     */
    @Test
    void parameterizedConstructor_ShouldInitializeWithGivenValues() {
        UserModel user = new UserModel();
        String messageText = "Test message";

        InboxMessageModel message = new InboxMessageModel(user, messageText);

        // Verify both provided values and default initializations
        assertNotNull(message.getTimestamp(), "Timestamp should be initialized");
        assertFalse(message.isRead(), "isRead should be initialized to false");
        assertEquals(messageText, message.getMessage(), "Message should match the input");
        assertEquals(user, message.getUser(), "User should match the input");
    }

    /**
     * Tests the ID getter and setter to ensure proper
     * persistence identifier handling.
     */
    @Test
    void setAndGetId_ShouldWorkCorrectly() {
        InboxMessageModel message = new InboxMessageModel();
        Long id = 1L;

        message.setId(id);

        assertEquals(id, message.getId(), "getId should return the set id");
    }

    /**
     * Tests the user reference getter and setter to ensure
     * proper relationship management between messages and users.
     */
    @Test
    void setAndGetUser_ShouldWorkCorrectly() {
        InboxMessageModel message = new InboxMessageModel();
        UserModel user = new UserModel();

        message.setUser(user);

        assertEquals(user, message.getUser(), "getUser should return the set user");
    }

    /**
     * Tests the message content getter and setter to ensure
     * proper message text handling.
     */
    @Test
    void setAndGetMessage_ShouldWorkCorrectly() {
        InboxMessageModel message = new InboxMessageModel();
        String messageText = "Test message";

        message.setMessage(messageText);

        assertEquals(messageText, message.getMessage(), "getMessage should return the set message");
    }

    /**
     * Tests the timestamp getter and setter to ensure
     * proper temporal data handling.
     */
    @Test
    void setAndGetTimestamp_ShouldWorkCorrectly() {
        InboxMessageModel message = new InboxMessageModel();
        LocalDateTime timestamp = LocalDateTime.now();

        message.setTimestamp(timestamp);

        assertEquals(timestamp, message.getTimestamp(), "getTimestamp should return the set timestamp");
    }

    /**
     * Tests the read status getter and setter to ensure proper
     * message state management. Verifies both setting to true
     * and false to ensure complete toggle functionality.
     */
    @Test
    void setAndGetIsRead_ShouldWorkCorrectly() {
        InboxMessageModel message = new InboxMessageModel();

        // Test setting to true
        message.setRead(true);
        assertTrue(message.isRead(), "isRead should return true after setting to true");

        // Test setting to false
        message.setRead(false);
        assertFalse(message.isRead(), "isRead should return false after setting to false");
    }

    /**
     * Tests that the default constructor sets the timestamp
     * to the current time, within reasonable bounds.
     * Uses before/after timestamps to verify the initialization
     * time falls within the expected window.
     */
    @Test
    void defaultConstructor_ShouldSetTimestampToCurrentTime() {
        LocalDateTime before = LocalDateTime.now();

        InboxMessageModel message = new InboxMessageModel();
        LocalDateTime after = LocalDateTime.now();

        LocalDateTime timestamp = message.getTimestamp();
        assertTrue(timestamp.isAfter(before) || timestamp.isEqual(before),
                "Timestamp should not be before construction");
        assertTrue(timestamp.isBefore(after) || timestamp.isEqual(after),
                "Timestamp should not be after construction");
    }
}