package org.cpts422.Femininomenon.App.Utils;

import org.hibernate.annotations.Parameter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BankHolidayTest {

    @Nested
    class testGetListOfBankHolidays {

        // stream object to be used in parameterized tests for validating GetListOfBankHolidays
        // contains the year and day of the week that Veterans Day would occur on
        static Stream<Object[]> veteransDays() {
            return Stream.of(
                    new Object[]{2023, DayOfWeek.SATURDAY},
                    new Object[]{2025, DayOfWeek.TUESDAY},
                    new Object[]{2026, DayOfWeek.WEDNESDAY},
                    new Object[]{2027, DayOfWeek.THURSDAY},
                    new Object[]{2029, DayOfWeek.SUNDAY},
                    new Object[]{2030, DayOfWeek.MONDAY},
                    new Object[]{2033, DayOfWeek.FRIDAY}
            );
        }

        // parameterized tests for valid years, all possible dates for days of the week
        // uses Veterans Day as the control holiday that could occur on each day of the week
        // takes a stream of objects containing the year and day of the week that Veterans Day occurs on
        @ParameterizedTest
        @MethodSource("veteransDays")
        void testValidYears(int year, DayOfWeek VeteransDay) {
            LocalDateTime testVeterans = LocalDateTime.of(year, Month.JANUARY, 1, 0, 0, 0);
            List<LocalDateTime> listOfBankHolidays = BankHolidays.getListOfBankHolidays(year);

            for (LocalDateTime bankHoliday : listOfBankHolidays) {
                if (bankHoliday.getMonth() == Month.NOVEMBER && bankHoliday.getDayOfMonth() == 11) {
                    testVeterans = bankHoliday;
                    break;
                }
            }

            // ensures Veterans Day exists and exists on the correct day of the week
            assertEquals(VeteransDay, testVeterans.getDayOfWeek());
        }

        // test how function handles invalid years
        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 1581})
        void testInvalidYear(int invalidYears) {
            assertThrows(IllegalArgumentException.class, () -> BankHolidays.getListOfBankHolidays(invalidYears));
        }

    }

    @Nested
    class testFindSpecificHoliday {
        // test with starting date beginning of month
        @Test
        void testBeginningOfMonth() {

        }

        // test with starting date end of month
        @Test
        void testEndOfMonth() {

        }

        // test with invalid start date
        @Test
        void testInvalidStartDate() {

        }
    }

    @Nested
    class testAdjustForBankClosures {
        // stream object to be used in parameterized tests for validating AdjustForBankClosures
        // contains a collection of normal days the bank would be open
        static Stream<LocalDateTime> normalDays() {
            return Stream.of(
                    LocalDateTime.of(2024, Month.JANUARY, 2,0,0),
                    LocalDateTime.of(2024,Month.JANUARY, 31, 0, 0),
                    LocalDateTime.of(2024, Month.FEBRUARY, 29, 0, 0),
                    LocalDateTime.of(2023, Month.FEBRUARY, 28, 0, 0),
                    LocalDateTime.of(2024, Month.MARCH, 15, 0, 0),
                    LocalDateTime.of(2024, Month.MAY, 1, 0, 0),
                    LocalDateTime.of(2024, Month.JULY, 3, 0, 0),
                    LocalDateTime.of(2024, Month.DECEMBER, 31, 0, 0)
            );
        }

        // stream object to be used in parameterized tests for validating AdjustForBankClosures
        // contains a collection of non-holiday sundays, when banks are closed
        static Stream<LocalDateTime> holidays() {
            return Stream.of(
                    LocalDateTime.of(2024, Month.JANUARY, 1,0,0),
                    LocalDateTime.of(2024, Month.JANUARY, 15, 0, 0),
                    LocalDateTime.of(2024, Month.FEBRUARY, 19, 0,0),
                    LocalDateTime.of(2024, Month.MAY, 27, 0, 0),
                    LocalDateTime.of(2024, Month.JUNE, 19, 0, 0),
                    LocalDateTime.of(2024, Month.JULY, 4, 0, 0),
                    LocalDateTime.of(2024, Month.SEPTEMBER, 2,0,0),
                    LocalDateTime.of(2024, Month.OCTOBER, 14, 0, 0),
                    LocalDateTime.of(2024, Month.NOVEMBER, 11, 0,0),
                    LocalDateTime.of(2024, Month.NOVEMBER, 28, 0, 0),
                    LocalDateTime.of(2024, Month.DECEMBER, 25, 0, 0)
                    );
        }

        // normal days, no conflicts or adjustments
        @ParameterizedTest
        @MethodSource("normalDays")
        void testNormalDay(LocalDateTime averageDay) {
            assertEquals(averageDay, BankHolidays.adjustForBankClosures(averageDay));
        }

        // test handling Sundays
        @Test
        void testSunday() {
            LocalDateTime sunday = LocalDateTime.of(2024, Month.JANUARY, 7, 0, 0, 0);
            assertEquals(sunday.plusDays(1), BankHolidays.adjustForBankClosures(sunday));
        }

        // test handling Saturdays
        @Test
        void testSaturday() {
            LocalDateTime saturday = LocalDateTime.of(2024, Month.OCTOBER, 5, 0, 0, 0);
            assertEquals(saturday.plusDays(2), BankHolidays.adjustForBankClosures(saturday));
        }

        // parameterized test handling all holidays
        @ParameterizedTest
        @MethodSource("holidays")
        void testHolidays(LocalDateTime holiday) {
            if (holiday.getDayOfWeek() == DayOfWeek.FRIDAY) {
                assertEquals(holiday.plusDays(3), BankHolidays.adjustForBankClosures(holiday));
            }
            else if (holiday.getDayOfWeek() == DayOfWeek.SATURDAY) {
                assertEquals(holiday.plusDays(2), BankHolidays.adjustForBankClosures(holiday));
            }
            else {
                assertEquals(holiday.plusDays(1), BankHolidays.adjustForBankClosures(holiday));
            }
        }
    }
}
