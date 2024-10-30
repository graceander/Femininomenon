package org.cpts422.Femininomenon.App.Utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class BankHolidays {

    // Method to generate the list of bank holidays for a given year
    public static List<LocalDateTime> getListOfBankHolidays(int year) {
        if (year < 1582) {
            throw new IllegalArgumentException("The year must be after 1582, the start of the current calendar format");
        }

        List<LocalDateTime> bankHolidays = new ArrayList<>();
        // add dates that are always the same number
        bankHolidays.add(LocalDateTime.of(year, Month.JANUARY, 1, 0, 0)); // New Year 1/1
        bankHolidays.add(LocalDateTime.of(year, Month.JUNE, 19, 0, 0)); // Juneteenth 6/19
        bankHolidays.add(LocalDateTime.of(year, Month.JULY, 4, 0, 0)); // 4th of July 7/6
        bankHolidays.add(LocalDateTime.of(year, Month.NOVEMBER, 11, 0, 0)); // Veterans Day 11/11
        bankHolidays.add(LocalDateTime.of(year, Month.DECEMBER, 25, 0, 0)); // Christmas 12/25

        // Civil Rights Day - the third Monday of January
        LocalDateTime civilRightsDay = findSpecificHoliday(LocalDateTime.of(year, Month.JANUARY, 1, 0, 0), DayOfWeek.MONDAY,2);
        bankHolidays.add(civilRightsDay);

        // President's Day - the third Monday of February
        LocalDateTime presidentsDay = findSpecificHoliday(LocalDateTime.of(year, Month.FEBRUARY, 1, 0, 0), DayOfWeek.MONDAY,2);
        bankHolidays.add(presidentsDay);

        // Memorial Day - the last Monday of May
        LocalDateTime memorialDay = findSpecificHoliday(LocalDateTime.of(year, Month.MAY, 31, 0, 0), DayOfWeek.MONDAY,0);
        bankHolidays.add(memorialDay);

        // Labor Day - the first Monday of September
        LocalDateTime laborDay = findSpecificHoliday(LocalDateTime.of(year, Month.SEPTEMBER, 1, 0, 0), DayOfWeek.MONDAY,0);
        bankHolidays.add(laborDay);

        // Indigenous People's Day - the second Monday of October
        LocalDateTime indigenousPeoplesDay = findSpecificHoliday(LocalDateTime.of(year, Month.OCTOBER, 1, 0, 0), DayOfWeek.MONDAY,1);
        bankHolidays.add(indigenousPeoplesDay);

        // Thanksgiving - the fourth Thursday of November
        LocalDateTime thanksgiving = findSpecificHoliday(LocalDateTime.of(year, Month.NOVEMBER, 1, 0, 0), DayOfWeek.THURSDAY,3);
        bankHolidays.add(thanksgiving);

        return bankHolidays;
    }

    // Method to find a holiday where the date is determined by the day of the week and the week number of the month rather than being a regularly set date
    public static LocalDateTime findSpecificHoliday(LocalDateTime startingDay, DayOfWeek holidayDayOfWeek, int weekNumOffset) {
        int weekdayOffset = (startingDay.getDayOfWeek().getValue() - holidayDayOfWeek.getValue() + 7) % 7; // weekday offset calculation
        if (startingDay.getDayOfMonth() == 1) { // if you are given the first of the month
            return startingDay.plusDays(weekdayOffset).plusWeeks(weekNumOffset);
        }
        else if (startingDay.getDayOfMonth() == startingDay.toLocalDate().lengthOfMonth()) { // if you are given the end of the month
            return startingDay.minusDays(weekdayOffset).minusWeeks(weekNumOffset);
        }
        else {
            throw new IllegalArgumentException("Illegal Argument: starting date to use for calculation must be the first or the last day of the month");
        }
    }

    // Method to identify the next business day after a bank closure day
    public static LocalDateTime adjustForBankClosures(LocalDateTime scheduledDate) {
        LocalDateTime adjustedDate = scheduledDate; // initialize to given date
        int paymentYear = scheduledDate.getYear();
        List<LocalDateTime> bankHolidays = getListOfBankHolidays(paymentYear);

        if (adjustedDate.getDayOfWeek() == DayOfWeek.SUNDAY) { // if the date is a Sunday
            adjustedDate = adjustedDate.plusDays(1); // increment one day to Monday
        }
        else if (adjustedDate.getDayOfWeek() == DayOfWeek.SATURDAY) { // if the date is a Saturday
            adjustedDate = adjustedDate.plusDays(2); // increment two days to Monday
        }

        for (LocalDateTime bankHoliday : bankHolidays) {
            if (adjustedDate.getMonth() == bankHoliday.getMonth() && adjustedDate.getDayOfMonth() == bankHoliday.getDayOfMonth()) {
                return adjustedDate.plusDays(1);
            }
        }

        return adjustedDate;
    }
}
