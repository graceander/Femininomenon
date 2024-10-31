package org.cpts422.Femininomenon.App.Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for TransactionModel class.
 * Tests all aspects of the Transaction entity including:
 * - Constructors
 * - Getters and setter
 * - toString
 * - Equals and hashCode methods
 * - Enum handling
 * - Null value handling
 */
class TransactionModelTest {

    @Mock
    private UserModel mockUser;

    private TransactionModel transaction;
    private LocalDateTime testDate;

    /**
     * Sets up the test environment before each test method.
     * Mocks and creates a standard transaction instance
     * with known test values for consistent testing.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testDate = LocalDateTime.now();
        transaction = new TransactionModel(
                mockUser,
                testDate,
                100.0f,
                TransactionModel.CategoryType.GROCERIES,
                "Test Description",
                TransactionModel.TransactionType.EXPENSE,
                "Test Account"
        );
        transaction.setId(1L);
    }

    /**
     * Tests the default (no-args) constructor.
     * Verifies that a transaction can be created without parameters.
     */
    @Test
    void testDefaultConstructor() {
        TransactionModel emptyTransaction = new TransactionModel();
        assertNotNull(emptyTransaction);
    }

    /**
     * Tests the parameterized constructor.
     * Verifies that all fields are correctly initialized with provided values.
     */
    @Test
    void testParameterizedConstructor() {
        assertNotNull(transaction);
        assertEquals(mockUser, transaction.getUser());
        assertEquals(testDate, transaction.getDate());
        assertEquals(100.0f, transaction.getAmount());
        assertEquals(TransactionModel.CategoryType.GROCERIES, transaction.getCategory());
        assertEquals("Test Description", transaction.getDescription());
        assertEquals(TransactionModel.TransactionType.EXPENSE, transaction.getType());
        assertEquals("Test Account", transaction.getAccount());
    }

    /**
     * Tests the ID getter and setter.
     * Verifies that the ID can be set and retrieved correctly.
     */
    @Test
    void testIdGetterAndSetter() {
        Long testId = 1L;
        transaction.setId(testId);
        assertEquals(testId, transaction.getId());
    }

    /**
     * Tests the user reference getter and setter.
     * Verifies that the user reference can be updated and retrieved.
     */
    @Test
    void testUserGetterAndSetter() {
        UserModel newUser = mock(UserModel.class);
        transaction.setUser(newUser);
        assertEquals(newUser, transaction.getUser());
    }

    /**
     * Tests the transaction date getter and setter.
     * Verifies that the date can be updated and retrieved correctly.
     */
    @Test
    void testDateGetterAndSetter() {
        LocalDateTime newDate = LocalDateTime.now().plusDays(1);
        transaction.setDate(newDate);
        assertEquals(newDate, transaction.getDate());
    }

    /**
     * Tests the amount getter and setter.
     * Verifies that the transaction amount can be updated and retrieved.
     */
    @Test
    void testAmountGetterAndSetter() {
        float newAmount = 200.0f;
        transaction.setAmount(newAmount);
        assertEquals(newAmount, transaction.getAmount());
    }

    /**
     * Tests the category getter and setter.
     * Verifies that the transaction category can be updated and retrieved.
     */
    @Test
    void testCategoryGetterAndSetter() {
        TransactionModel.CategoryType newCategory = TransactionModel.CategoryType.ENTERTAINMENT;
        transaction.setCategory(newCategory);
        assertEquals(newCategory, transaction.getCategory());
    }

    /**
     * Tests the description getter and setter.
     * Verifies that the transaction description can be updated and retrieved.
     */
    @Test
    void testDescriptionGetterAndSetter() {
        String newDescription = "New Description";
        transaction.setDescription(newDescription);
        assertEquals(newDescription, transaction.getDescription());
    }

    /**
     * Tests the transaction type getter and setter.
     * Verifies that the transaction type can be updated and retrieved.
     */
    @Test
    void testTypeGetterAndSetter() {
        TransactionModel.TransactionType newType = TransactionModel.TransactionType.INCOME;
        transaction.setType(newType);
        assertEquals(newType, transaction.getType());
    }

    /**
     * Tests the account getter and setter.
     * Verifies that the account information can be updated and retrieved.
     */
    @Test
    void testAccountGetterAndSetter() {
        String newAccount = "New Account";
        transaction.setAccount(newAccount);
        assertEquals(newAccount, transaction.getAccount());
    }

    /**
     * Comprehensive test of the equals method covering all branches:
     * - Self equality
     * - Null handling
     * - Type comparison
     * - Field-by-field comparison
     * - Null field handling for all the fields
     *
     * Tests various combinations of equal and unequal transactions,
     * including cases where individual fields differ and cases with
     * null values in different fields.
     */
    @Test
    void testEqualsAllBranches() {
        // Test self equality
        assertTrue(transaction.equals(transaction));

        // Test null
        assertFalse(transaction.equals(null));

        // Test different class
        assertFalse(transaction.equals(new Object()));

        // Test equal transactions
        TransactionModel equalTransaction = new TransactionModel(
                mockUser,
                testDate,
                100.0f,
                TransactionModel.CategoryType.GROCERIES,
                "Test Description",
                TransactionModel.TransactionType.EXPENSE,
                "Test Account"
        );
        equalTransaction.setId(transaction.getId());
        assertTrue(transaction.equals(equalTransaction));

        // Test different ID
        TransactionModel differentIdTransaction = createEqualTransaction();
        differentIdTransaction.setId(2L);
        assertFalse(transaction.equals(differentIdTransaction));

        // Test different user
        TransactionModel differentUserTransaction = createEqualTransaction();
        differentUserTransaction.setUser(mock(UserModel.class));
        assertFalse(transaction.equals(differentUserTransaction));

        // Test null user
        TransactionModel nullUserTransaction = createEqualTransaction();
        nullUserTransaction.setUser(null);
        assertFalse(transaction.equals(nullUserTransaction));

        transaction.setUser(null);
        TransactionModel bothNullUserTransaction = createEqualTransaction();
        bothNullUserTransaction.setUser(null);
        assertTrue(transaction.equals(bothNullUserTransaction));

        // Reset user
        transaction.setUser(mockUser);

        // Test different date
        TransactionModel differentDateTransaction = createEqualTransaction();
        differentDateTransaction.setDate(testDate.plusDays(1));
        assertFalse(transaction.equals(differentDateTransaction));

        // Test null date
        TransactionModel nullDateTransaction = createEqualTransaction();
        nullDateTransaction.setDate(null);
        assertFalse(transaction.equals(nullDateTransaction));

        transaction.setDate(null);
        TransactionModel bothNullDateTransaction = createEqualTransaction();
        bothNullDateTransaction.setDate(null);
        assertTrue(transaction.equals(bothNullDateTransaction));

        // Reset date
        transaction.setDate(testDate);

        // Test different amount
        TransactionModel differentAmountTransaction = createEqualTransaction();
        differentAmountTransaction.setAmount(200.0f);
        assertFalse(transaction.equals(differentAmountTransaction));

        // Test different category
        TransactionModel differentCategoryTransaction = createEqualTransaction();
        differentCategoryTransaction.setCategory(TransactionModel.CategoryType.ENTERTAINMENT);
        assertFalse(transaction.equals(differentCategoryTransaction));

        // Test null category
        TransactionModel nullCategoryTransaction = createEqualTransaction();
        nullCategoryTransaction.setCategory(null);
        assertFalse(transaction.equals(nullCategoryTransaction));

        transaction.setCategory(null);
        TransactionModel bothNullCategoryTransaction = createEqualTransaction();
        bothNullCategoryTransaction.setCategory(null);
        assertTrue(transaction.equals(bothNullCategoryTransaction));

        // Reset category
        transaction.setCategory(TransactionModel.CategoryType.GROCERIES);

        // Test different description
        TransactionModel differentDescriptionTransaction = createEqualTransaction();
        differentDescriptionTransaction.setDescription("Different Description");
        assertFalse(transaction.equals(differentDescriptionTransaction));

        // Test null description
        TransactionModel nullDescriptionTransaction = createEqualTransaction();
        nullDescriptionTransaction.setDescription(null);
        assertFalse(transaction.equals(nullDescriptionTransaction));

        transaction.setDescription(null);
        TransactionModel bothNullDescriptionTransaction = createEqualTransaction();
        bothNullDescriptionTransaction.setDescription(null);
        assertTrue(transaction.equals(bothNullDescriptionTransaction));

        // Reset description
        transaction.setDescription("Test Description");

        // Test different type
        TransactionModel differentTypeTransaction = createEqualTransaction();
        differentTypeTransaction.setType(TransactionModel.TransactionType.INCOME);
        assertFalse(transaction.equals(differentTypeTransaction));

        // Test different account
        TransactionModel differentAccountTransaction = createEqualTransaction();
        differentAccountTransaction.setAccount("Different Account");
        assertFalse(transaction.equals(differentAccountTransaction));

        // Test null account
        TransactionModel nullAccountTransaction = createEqualTransaction();
        nullAccountTransaction.setAccount(null);
        assertFalse(transaction.equals(nullAccountTransaction));

        transaction.setAccount(null);
        TransactionModel bothNullAccountTransaction = createEqualTransaction();
        bothNullAccountTransaction.setAccount(null);
        assertTrue(transaction.equals(bothNullAccountTransaction));
    }

    /**
     * Helper method to create a transaction with the same field values
     * as the test instance. Used for equality testing.
     */
    private TransactionModel createEqualTransaction() {
        TransactionModel equalTransaction = new TransactionModel(
                mockUser,
                testDate,
                100.0f,
                TransactionModel.CategoryType.GROCERIES,
                "Test Description",
                TransactionModel.TransactionType.EXPENSE,
                "Test Account"
        );
        equalTransaction.setId(transaction.getId());
        return equalTransaction;
    }

    /**
     * Tests the hashCode implementation:
     * - Consistent hash codes for same object
     * - Equal objects produce equal hash codes
     * - Hash code generation with null fields
     */
    @Test
    void testHashCode() {
        // Same object should have same hash code
        assertEquals(transaction.hashCode(), transaction.hashCode());

        // Equal objects should have same hash code
        TransactionModel equalTransaction = createEqualTransaction();
        assertEquals(transaction.hashCode(), equalTransaction.hashCode());

        // Test with null fields
        transaction.setUser(null);
        transaction.setDate(null);
        transaction.setCategory(null);
        transaction.setDescription(null);
        transaction.setAccount(null);
        assertNotNull(transaction.hashCode());
    }

    /**
     * Tests the toString implementation:
     * 1. Verifies all fields are included in string representation
     * 2. Checks handling of null fields
     * 3. Verifies the format of the output
     */
    @Test
    void testToString() {
        String toString = transaction.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("amount=" + transaction.getAmount()));
        assertTrue(toString.contains("category=" + transaction.getCategory()));
        assertTrue(toString.contains("description='" + transaction.getDescription() + "'"));
        assertTrue(toString.contains("type=" + transaction.getType()));
        assertTrue(toString.contains("account='" + transaction.getAccount() + "'"));

        // Test with null fields
        transaction.setUser(null);
        transaction.setDate(null);
        transaction.setCategory(null);
        transaction.setDescription(null);
        transaction.setAccount(null);
        String nullFieldsToString = transaction.toString();
        assertNotNull(nullFieldsToString);
    }

    /**
     * Tests that all defined category types can be correctly
     * set and retrieved from a transaction.
     * Verifies completeness of CategoryType enum handling.
     */
    @Test
    void testAllCategoryTypes() {
        for (TransactionModel.CategoryType category : TransactionModel.CategoryType.values()) {
            transaction.setCategory(category);
            assertEquals(category, transaction.getCategory());
        }
    }

    /**
     * Tests that all defined transaction types can be correctly
     * set and retrieved from a transaction.
     * Verifies completeness of TransactionType enum handling.
     */
    @Test
    void testAllTransactionTypes() {
        for (TransactionModel.TransactionType type : TransactionModel.TransactionType.values()) {
            transaction.setType(type);
            assertEquals(type, transaction.getType());
        }
    }
}
