# Femininomenon-TestCases

## Integration Tests
### STServiceAndTModelPairwiseTests
* 1 testConvertScheduledTypeToTransactionType
    * Parameterized Test that validates the mocked ScheduleTransactionModel is invoked and the transaction type is properly converted.
    * MethodSource: scheduledTransactionTypes, a stream of objects containing the ST and T type match ups

* 2 testConvertScheduledCategoryToTransactionCategory
  * Parameterized Test that validates the mocked ScheduleTransactionModel is invoked and the transaction category is properly converted.
  * MethodSource: scheduledTransactionCategories, a stream of objects containing the ST and T category match ups

## System Tests
### CurrencyConversionSeleniumTest
* 3 testDefaultCurrency
  * Tests that the currency selector displays correct default value.

* 4 testCurrencyConversionToEUR
  * Tests converting currency from USD to EUR.

### InboxMessageSeleniumTest
* 5 testInboxMessageExists
  * Tests that the expected messages appear after creating a large transaction.

* 6 testInboxAccess
  * Tests basic inbox access and verifies the page structure

* 7 testNavigateHomeFromInbox
  * Tests navigation back to home page from inbox

* 8 testMarkMessageAsRead
  * Tests marking a single message as read.

* 9 testInboxWithManyMessages
  * Tests inbox behavior with many messages

### ScheduledTransactionTests
* 10 createNormalScheduledTransactionTest
  * Tests the process of creating a scheduled transaction under expected conditions
  * Validates that the scheduled transaction is created correctly, as well as all related transactions

### SpendingOverviewSeleniumTest

### UserRegisterAndLoginSeleniumTest
* 11 testRegisterUser
  * Tests the process of registering a typical user account

* 12 testLoginMatthewUser
  * Tests user Matthew logging into the system

### UserSimpleTransactionSeleniumTest
* 13 testEditTransaction
  * Tests editing an existing transaction under normal conditions

* 14 testDeleteTransaction
  * Tests deleting an existing transaction under normal conditions

* 15 testAddTransaction
  * Tests adding a new transaction under normal conditions

## Unit Tests
### Controllers
#### InboxMessageControllerTest
* 16 viewInbox_ShouldReturnUserInboxViewWithMessages
  * Tests the viewInbox method of the controller
    * Verifying:
    1. The correct view name is returned
    2. The model is populated with required attributes
    3. The appropriate service methods are called
* 17 markMessageAsRead_ShouldMarkMessageAndRedirect
  * Tests the markMessagesAsRead method of the controller
    * Verifying:
    1. The correct redirect URL is returned
    2. The message is marked as read via the service
* 18 markAllMessagesAsRead_ShouldMarkAllMessagesAndRedirect
  * Tests the markAllMessagesAsRead method of the controller
    * Verifying:
    1. The correct redirect URL is returned
    2. The user is looked up
    3. All messages for the user are read

#### ScheduledTransactionControllerTest
* 19 testNormalViewScheduledTransactions
  * Tests viewing STs under normal conditions
* 20 testNullUseViewScheduledTransactions
  * Tests viewing STs with a null user
* 21 testNullTransactionsViewScheduledTransactions
  * Tests viewing a null ST
* 22 testEmptyTransactionsViewScheduledTransactions
  * Tests viewing an empty ST
* 23 testNormalAddScheduledTransaction
  * Tests adding an ST under normal conditions
* 24 testAddScheduledTransactionUserNotFound
  * Tests adding an ST to a user that does not exist
* 25 testNormalSubmitScheduledTransaction
  * Tests submitting an ST under normal conditions
* 26 testSubmitScheduledTransactionUserNotFound
  * Tests submitting an ST when the user cannot be found
* 27 testInvalidCategorySubmitScheduledTransaction
  * Tests submitting an ST with an invalid category
* 28 testNormalEditScheduledTransaction
  * Tests editing an ST under normal conditions
* 29 testWrongUserEditTransaction
  * Tests editing an ST that belongs to another user
* 30 testNullUserEditTransaction
  * Tests attempting to edit a transaction that does not belong to a user
* 31 testNullTransactionEditTransaction
  * Tests attempting to edit a null transaction
* 32 testNormalDeleteScheduledTransaction
  * Tests deleting an ST under normal conditions
* 33 testCannotFindTransactionToDelete
  * Tests deleting an ST that cannot be found
* 34 testScheduledTransactionBelongsToAnotherUser
  * Tests attempting to delete an ST that belongs to another user
* 35 testNormalUpdateScheduledTransaction
  * Tests updating an ST under normal conditions
* 36 testWrongUserUpdateTransaction
  * Tests updating the wrong user's ST
* 37 testNullTransactionUpdateTransaction
  * Tests updating a null ST
* 38 testInvalidCategoryUpdateTransaction
  * Tests updating an ST with an invalid category

#### TransactionControllerTest
* 39 testLogin
  * Tests login functionality
* 40 testAddTransactionUserExists
  * Tests adding transaction to existing user
* 41 testAddTransactionUserNotFound
  * Tests adding transaction to invalid user
* 42 testSubmitTransaction
  * Tests submitting a transaction under normal conditions
* 43 testSubmitTransactionUserNotFound
  * Tests submitting a transaction when a user does not exist
* 44 testEditTransaction
  * Tests editing transactions
* 45 testEditTransactionNotBelongToUser
  * Tests editing transactions that do not belong to the designated user
* 46 testChangeCurrency
  * Tests changing a user's currency
* 47 testChangeCurrencyUserError
  * Tests changing currency on an invalid user
* 48 testHomePage
  * Tests the homepage
* 49 testHomePageUserError
  * Tests the homepage of an invalid user
* 50 testDeleteTransaction
  * Tests deleting transactions under normal conditions
* 51 testDeleteTransactionError
  * Tests deleting invalid transaction
* 52 testDeleteTransactionDoesntBelong
  * Tests deleting a transaction that does not belong to the designated user
* 53 testUpdateTransaction
  * Tests updating transactions under normal conditions
* 54 testUpdateNullTransaction
  * Tests updating invalid transaction
* 55 testUpdateTransactionErrorCategory
  * Tests updating transaction with a bad category
* 56 testUpdateTransactionErrorUser
  * Tests updating a transactions with the wrong user
* 57 testEditTransactionErrorUser
  * Tests editing a transaction with the wrong user
* 58 testEditTransactionErrorNull
  * Tests editing a null transaction

#### UserControllerTest
* 59 testRegisterPage
  * this test function will test if it can get to the register page
* 60 testRegisterCorrect
  * this test will check if it can register a new user.
* 61 testRegisterFalse
  * this will test the error handling of the register page
* 62 testIndexPage
  * this will test the login page
* 63 testLoginSuccessful
  * this will test the login if it's successful
* 64 testLoginFailed
  * this will test if the login failed like username or password wrong

#### UserRuleControllerTest
* 65 testViewRules
  * Tests viewing all current user's rules
* 66 testAddRuleCompare
  * Tests adding a comparison rule under normal conditions
* 67 testAddRuleMaximumSpending
  * Tests adding a maximum spending rule under normal conditions

### Models
#### InboxMessageModelTest
* 68 defaultConstructor_ShouldInitializeWithDefaultValues
  * Tests the default constructor to ensure it properly initializes
    * all fields with appropriate default values.
    * Expected behavior:
    * - Timestamp should be initialized (not null)
    * - isRead should default to false
    * - Message content should be null
    * - User reference should be null
* 69 parameterizedConstructor_ShouldInitializeWithGivenValues
  * Tests the parameterized constructor to ensure it correctly
    * initializes fields with provided values while maintaining
    * appropriate defaults for other fields.
* 70 setAndGetId_ShouldWorkCorrectly
  * Tests the ID getter and setter to ensure proper
    * persistence identifier handling.
* 71 setAndGetUser_ShouldWorkCorrectly
  * Tests the user reference getter and setter to ensure
    * proper relationship management between messages and users.
* 72 setAndGetMessage_ShouldWorkCorrectly
  * Tests the message content getter and setter to ensure
    * proper message text handling.
* 73 setAndGetTimestamp_ShouldWorkCorrectly
  * Tests the timestamp getter and setter to ensure
    * proper temporal data handling.
* 74 setAndGetIsRead_ShouldWorkCorrectly
  * Tests the read status getter and setter to ensure proper
    * message state management. Verifies both setting to true
    * and false to ensure complete toggle functionality.
* 75 defaultConstructor_ShouldSetTimestampToCurrentTime
  * Tests that the default constructor sets the timestamp
    * to the current time, within reasonable bounds.
    * Uses before/after timestamps to verify the initialization
    * time falls within the expected window.

#### ScheduledTransactionModelTest
* 76 testDefaultConstructor
  * Tests the default constructor under normal conditions
* 77 testParameterizedConstructor
  * Tests constructor with input values
* 78 testSetID
  * Tests setting an ID
* 79 testSetUser
  * Tests setting a user
* 80 testSetFrequency
  * Tests setting the frequency
* 81 testSetRecentPayment
  * Tests setting recent payment date
* 82 testSetAmount
  * Tests setting the amount
* 83 testSetDescription
  * Tests setting description
* 84 testSetCategory
  * Tests setting the category
* 85 testSetTransactionType
  * Tests setting the transaction type
* 86 testSetAccount
  * Tests setting the account type

#### TransactionModelTest
* 87 testDefaultConstructor
  * Tests the default (no-args) constructor.
    * Verifies that a transaction can be created without parameters.
* 88 testParameterizedConstructor
  * Tests the parameterized constructor.
    * Verifies that all fields are correctly initialized with provided values.
* 89 testIdGetterAndSetter
  * Tests the ID getter and setter.
    * Verifies that the ID can be set and retrieved correctly.
* 90 testUserGetterAndSetter
  * Tests the user reference getter and setter.
    * Verifies that the user reference can be updated and retrieved.
* 91 testDateGetterAndSetter
  * Tests the transaction date getter and setter.
    * Verifies that the date can be updated and retrieved correctly.
* 92 testAmountGetterAndSetter
  * Tests the amount getter and setter.
    * Verifies that the transaction amount can be updated and retrieved.
* 93 testCategoryGetterAndSetter
  * Tests the category getter and setter.
    * Verifies that the transaction category can be updated and retrieved.
* 94 testDescriptionGetterAndSetter
  * Tests the description getter and setter.
    * Verifies that the transaction description can be updated and retrieved.
* 95 testTypeGetterAndSetter
  * Tests the transaction type getter and setter.
    * Verifies that the transaction type can be updated and retrieved.
* 96 testAccountGetterAndSetter
  * Tests the account getter and setter.
    * Verifies that the account information can be updated and retrieved.
* 97 testEqualsAllBranches
  * Comprehensive test of the equals method covering all branches:
    * - Self equality
    * - Null handling
    * - Type comparison
    * - Field-by-field comparison
    * - Null field handling for all the fields
    * Tests various combinations of equal and unequal transactions,
    * including cases where individual fields differ and cases with
    * null values in different fields.
* 98 testHashCode
  * Tests the hashCode implementation:
    * - Consistent hash codes for same object
    * - Equal objects produce equal hash codes
    * - Hash code generation with null fields
* 99 testToString
  * Tests the toString implementation:
    1. Verifies all fields are included in string representation
    2. Checks handling of null fields
    3. Verifies the format of the output
* 100 testAllCategoryTypes
  * Tests that all defined category types can be correctly
    * set and retrieved from a transaction.
    * Verifies completeness of CategoryType enum handling.
* 101 testAllTransactionTypes
  * Tests that all defined transaction types can be correctly
    * set and retrieved from a transaction.
    * Verifies completeness of TransactionType enum handling.

#### UserModelTest
* 102 testGetAndSetId
  * Tests setting user id
* 103 testDefaultCurrency
  * Tests default currency value
* 104 testSetCurrency
  * Tests changing currency type
* 105 testGetAndSetLogin
  * Tests setting the login info
* 106 testGetAndSetPassword
  * Tests setting a password
* 107 testGetAndSetEmail
  * Tests setting email address
* 108 testGetAndSetFirstName
  * Tests setting user first name
* 109 testGetAndSetLastName
  * Tests setting user last name
* 110 testGetAndSetSpendingLimits
  * Tests setting user spending limits
* 111 testGetAndSetRules
  * Tests setting user rules

#### UserRuleModelTest
* 112 testValues
  * Validates getters
* 113 testConstructorAndGetters
  * Validates constructor, and getters
* 114 testGetAndSetUser
  * Tests setting a new user 
* 115 testGetAndSetCategory
  * Tests setting a category
* 116 testGetAndSetLimitAmount
  * Tests setting a limit amount
* 117 testGetAndSetFrequency
  * Tests setting a rule frequency
* 118 testGetAndSetRuleType
  * Tests setting the rule type
* 119 testGetAndSetAdditionalCategory
  * Tests setting secondary categories
* 120 testGetAndSetId
  * Tests rule ID setting

### Service
#### CurrencyConversionServiceTest
* 121 convert_ValidCurrencies_SuccessfulConversion
  * Tests basic currency conversion between USD and EUR in both directions.
    * Verifies that:
    1. Converting from USD to EUR uses the correct exchange rate
    2. Converting from EUR to USD uses the inverse rate correctly
    * Expected rates: 1 USD = 0.85 EUR, therefore 1 EUR ≈ 1.1765 USD

* 122 convert_SameCurrency_ReturnsOriginalAmount
  * Tests conversion between the same currency.
    * Verifies that converting an amount to the same currency
    * returns the original amount unchanged.

* 123 convert_InvalidFromCurrency_ThrowsException
  * Tests error handling when an invalid source currency is provided.
    * Verifies that the service throws an IllegalArgumentException
    * with the correct error message.

* 124 convert_InvalidToCurrency_ThrowsException
  * Tests error handling when an invalid target currency is provided.
    * Verifies that the service throws an IllegalArgumentException
    * with the correct error message.

* 125 convert_CrossCurrencyConversion_SuccessfulConversion
  * Tests conversion between two currencies that require cross-rate calculation.
    * Verifies that converting between GBP and JPY uses correct intermediate
    * calculations through the base currency (USD).
    * Exchange rates used:
    * - 1 GBP = 1/0.73 USD ≈ 1.37 USD
    * - 1 USD = 110.0 JPY
    * Therefore: 1 GBP = 110.0/0.73 JPY ≈ 150.68 JPY

#### InboxMessageServiceTest
* 126 getInboxMessages_ShouldReturnMessages
  * Tests retrieval of inbox messages for a specific user.

* 127 markMessageAsRead_ShouldMarkMessageAsRead
  * Tests marking a single message as read.

* 128 markMessageAsRead_ShouldThrowException_WhenMessageNotFound
  * Tests exception handling when marking a non-existent message as read.

* 129 markAllMessagesAsRead_ShouldMarkAllMessagesAsRead
  * Tests marking all unread messages as read for a specific user.

* 130 checkSpendingRules_ShouldCreateAlert_WhenMaximumSpendingExceeded
  * Tests alert creation when maximum spending limit is exceeded.

* 131 checkSpendingRules_ShouldCreateAlert_WhenMinimumSavingsNotMet
  * Tests alert creation when minimum savings target is not met.

* 132 checkSpendingRules_ShouldCreateAlert_WhenCategoryExceedsComparison
  * Tests alert creation when category spending exceeds comparison category.

* 133 checkForOverallOverspending_ShouldCreateAlerts_WhenExpensesExceedIncome
  * Tests alert creation when overall expenses exceed income.

* 134 getStartDate_ShouldThrowException_WhenFrequencyIsNull
  * Tests exception handling when frequency is null in getStartDate method.
    * @throws Exception when method reflection or invocation fails

* 135 getStartDate_ShouldHandleDaily
  * Tests handling of daily frequency in getStartDate method.
    * @throws Exception when method reflection or invocation fails

* 136 getStartDate_ShouldHandleWeekly
  * Tests handling of weekly frequency in getStartDate method.
    * @throws Exception when method reflection or invocation fails

* 137 getStartDate_ShouldHandleUnsupportedFrequency
  * Tests handling of unsupported frequency in getStartDate method.
    * @throws Exception when method reflection or invocation fails

* 138 checkForOverallOverspending_ShouldHandleEmptyTransactions
  * Tests overspending check with empty transaction list.

* 139 checkForOverallOverspending_ShouldHandleNoOverspending
  * Tests overspending check when expenses don't exceed income.

* 140 checkSpendingRules_ShouldSkipWhenMessageExists
  * Tests spending rule check when alert message already exists.

* 141 checkForOverallOverspending_ShouldNotCreateAlert_WhenNoLargeExpense
  * Tests overspending check when no large expenses are present.

* 142 addMessage_ShouldHandleExistingMessage
  * Tests message addition when message already exists.

* 143 checkSpendingRules_ShouldHandleNullAdditionalCategory
  * Tests spending rule check with null additional category.

* 144 getStartDate_ShouldThrowException_ForUnsupportedFrequencyValue
  * Tests exception handling for unsupported frequency value in getStartDate.
    * @throws Exception when method reflection or invocation fails

* 145 checkSpendingRules_ShouldNotCreateAlert_WhenNotExceedCategoryAndAmountLower
  * Tests that no alert is created when category spending is lower than comparison.

* 146 checkSpendingRules_ShouldHandleNullCategory
  * Tests spending rule check with null category.

* 147 checkSpendingRules_ShouldNotCreateAlert_WhenMinimumSavingsMet
  * Tests that no alert is created when minimum savings target is met.

* 148 checkSpendingRules_ShouldHandleMaximumSpendingWithNoTransactions
  * Tests maximum spending check with empty transaction list.

* 149 checkSpendingRules_ShouldHandleEmptyRulesList
  * Tests spending rule check with empty rules list.

* 150 checkForOverallOverspending_ShouldHandleZeroIncome
  * Tests overspending check when no income transactions are present.

* 151 checkLargeExpenses_WithNoIncome
  * Tests large expense check when no income transactions are present.
    * @throws InvocationTargetException if the invoked method throws an exception
    * @throws IllegalAccessException if the method is inaccessible
    * @throws NoSuchMethodException if the method doesn't exist

* 152 checkLargeExpenses_WithIncomeButOverThreshold
  * Tests large expense check when expenses exceed income threshold.
    * @throws InvocationTargetException if the invoked method throws an exception
    * @throws IllegalAccessException if the method is inaccessible
    * @throws NoSuchMethodException if the method doesn't exist

* 153 checkLargeExpenses_WithIncomeAndUnderThreshold
  * Tests large expense check when expenses are below income threshold.
    * @throws InvocationTargetException if the invoked method throws an exception
    * @throws IllegalAccessException if the method is inaccessible
    * @throws NoSuchMethodException if the method doesn't exist

* 154 checkLargeExpenses_WithNoExpenses
  * Tests large expense check when no expense transactions are present.
    * @throws InvocationTargetException if the invoked method throws an exception
    * @throws IllegalAccessException if the method is inaccessible
    * @throws NoSuchMethodException if the method doesn't exist

#### ScheduledTransactionServiceTest
* 155 testGetTransactionsByUser
  * Tests retrieving all scheduled transactions by a given user under normal conditions
* 156 testSaveTransaction
  * Tests saving a scheduled transaction to the repository under normal conditions
* 157 testRemoveTransaction
  * Tests removing a scheduled transaction under normal conditions
* 158 testGetTransactionByID
  * Tests retrieving a scheduled transaction from the repository using its id
* 159 testConvertScheduledTypeToTransactionType
  * Parameterized test that validates converting between transaction enum and scheduled transaction enum types for transaction types
* 160 testConvertScheduledTypeToTransactionTypeThrowsError
  * Tests converter with invalid input
* 161 testConvertScheduledCategoryToTransactionCategory
  * Parameterized test that validates converting between transaction enum and scheduled transaction enum types for transaction categories
* 162 testConvertScheduledCategoryToTransactionCategoryThrowsError
  * Tests converter with invalid input
* 163 testFindNextPaymentDate
  * Parameterized test that tests whether the next payment dates are calculated correctly based on different frequencies
* 164 testOnCreateScheduledTransaction
  * Tests the creation process of scheduled transactions, verifies that a whole year's worth of transactions are created
* 165 testCreateTransaction
  * Tests the creation process of an individual transaction from a scheduled transaction

#### TransactionServiceTest
* 166 getSpendingByCategory_MonthlyBreakdown_CalculatesCorrectly
  * Tests monthly category spending calculation for a single category. Verifies that transactions within the same category are correctly summed.

* 167 getSpendingByCategory_MultipleCategories_CalculatesCorrectly
  * Tests spending calculation across multiple categories. Verifies that transactions are correctly grouped and summed by category.

* 168 getSpendingByCategory_EmptyTransactions_ReturnsEmptyMap
  * Tests behavior when no transactions exist. Verifies that an empty map is returned rather than null.

* 169 getTotalSpendingForMonth_CalculatesCorrectly
  * Tests monthly total spending calculation. verifies that all expenses within the month are correctly summed.

* 170 getSpendingByPeriod_HandlesDifferentPeriods
  * Tests spending calculation for different time periods. verifies that the same transaction is correctly counted in different period contexts.

* 171 saveTransaction_SavesAndChecksRules
  * Tests that saving a transaction triggers appropriate rule checks. Verifies that:
    1. The transaction is saved to the repository
    2. Spending rules are checked for the user
    3. Overall overspending is evaluated

* 172 removeTransaction_DeletesTransaction
  * Tests the basic transaction deletion functionality. Verifies that the repository's delete method is called with the correct transaction.

* 173 getTransactionById_ExistingTransaction_ReturnsTransaction
  * Tests retrieving an existing transaction by ID. Verifies that the correct transaction is returned when it exists in the repository.

* 174 getTransactionById_NonexistentTransaction_ReturnsNull
  * Tests handling of non-existent transaction retrieval. Verifies that null is returned when attempting to retrieve a transaction that doesn't exist.

* 175 getTotalSpending_AllPeriods_CalculatesCorrectly
  * Tests spending calculation across all time periods. Verifies that the same expense is correctly calculated regardless of the period specified, including handling of invalid period values.

* 176 getTransactionsByUser_ReturnsUserTransactions
  * Tests basic retrieval of user transactions. Verifies that the service returns the exact list of transactions provided by the repository.

* 177 getTotalSpendingForMonth_OutsideMonthTransactions_ExcludesThemFromTotal
  * Tests monthly spending calculation with transactions outside the target month. Verifies that only transactions within the specified month are included in the total, while transactions from adjacent months are correctly excluded.

* 178 getTotalSpendingForMonth_NonExpenseTransactions_ExcludesThemFromTotal
  * Tests monthly spending calculation with mixed transaction types. Verifies that non-expense transactions (ex: income) are properly excluded from the total spending calculation.

* 179 getTotalSpending_MixedTransactionTypes_FiltersCorrectly
  * Tests spending calculation with mixed transaction types. Verifies that only EXPENSE transactions are included in the total, while other transaction types are correctly filtered out.

* 180 getSpendingByCategory_PeriodBased_MixedTransactions
  * Tests category-based spending calculation with mixed transaction types. Verifies that:
    1. Only EXPENSE transactions are included in category totals
    2. Categories with only non-expense transactions are excluded
    3. Multiple transactions in the same category are correctly summed

* 181 getSpendingByCategory_MonthlyFiltering_AllCombinations
  * Tests comprehensive monthly category spending calculations. Verifies handling of:
    1. Transactions at month boundaries
    2. Mixed transaction types within the month
    3. Multiple categories
    4. Transactions outside the month

* 182 getTotalSpendingForMonth_AllDateConditions
  * Tests monthly spending calculation with various date conditions. Verifies the handling of transactions:
    1. Before the month starts
    2. At month start (excluded)
    3. Within the month
    4. At month end (excluded)
    5. After month ends
    * Also verifies correct handling of non-expense transactions.

* 183 getTotalSpendingForMonth_BoundaryExclusion
  * Tests the exclusion of transactions exactly at month boundaries. Verifies that transactions occurring exactly at the start or end of a month are excluded from the month's total spending.

* 184 getTotalSpendingForMonth_SingleDayAfterStart
  * Tests transaction inclusion for dates immediately after month start. Verifies the boundary condition where a transaction occurs one second after the start of the month, which should be included in the monthly total. This tests the "greater than" part of the date range comparison.

* 185 getTotalSpendingForMonth_SingleDayBeforeEnd
  * Tests transaction inclusion for dates immediately before month end.
    * Verifies the boundary condition where a transaction occurs one second
    * before the end of the month, which should be included in the monthly total.
    * This tests the "less than" part of the date range comparison.

* 186 getTotalSpendingForMonth_ExactlyAtEndDate
  * Tests transaction exclusion for dates exactly at month end.
    * Verifies that a transaction occurring exactly at the end of the month
    * (last second) is excluded from the monthly total. This tests the strict
    * boundary exclusion at the end of the time range.

* 187 getSpendingByCategory_EmptyTransactionsAfterFiltering
  * Tests category spending calculation when all transactions are filtered out.
    * Verifies that when all transactions are non-expense types (income in this case),
    * the resulting category map is empty rather than containing zero values.
    * This test is important for ensuring proper handling of edge cases where:
    1. All transactions are filtered out due to type
    2. The system returns an empty map instead of a map with zero values
    3. No categories are included in the result, even if transactions exist

#### UserRuleServiceTest
* 188 testGetRulesByUserLogin
  * Tests retrieving rules associated with a user under normal conditions

* 189 testSaveRule
  * Tests saving a new rule for a user under normal conditions

#### UsersServiceTest
* 190 testRegisterUser
  * Tests registering a new user under normal conditions

* 191 testRegisterUserNullLogin
  * Tests registering a user with no username, returns error

* 192 testRegisterUserNullPassword
  * Tests registering a user with no password, returns error

* 193 testSaveUser
  * Tests saving a user model to the repository

* 194 testAuthenticateUSer
  * Tests the authentication process for validating a user's login info

* 195 testFindByLogin
  * Tests locating a user in the repository via their login info

### Utils
#### BankHolidayTest
* 196 testValidYears
  * Parameterized tests for valid years, all possible dates for days of the week. Uses Veterans Day as the control holiday that could occur on each day of the week. Takes a stream of objects containing the year and day of the week that Veterans Day occurs on

* 197 testInvalidYear
  * test how function handles invalid years

* 198 testAllHolidays
  * test with starting date beginning of month

* 199 testInvalidStartDate
  * test with invalid start date

* 200 testNormalDay
  * tests normal days with no conflicts or adjustments

* 201 testSunday
  * Tests holiday adjustments for days that land on Sunday

* 202 testSaturday
  * Tests holiday adjustments for days that land on Saturdays

* 203 testHolidays
  *  parameterized test handling all holidays