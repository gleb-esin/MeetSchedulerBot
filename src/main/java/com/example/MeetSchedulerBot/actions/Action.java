package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Action {
    @Autowired
    MeetingRepository meetingRepository;
    /**
     * This method performs transformation List with available dates to suitable String for print and add year and name of month .
     *
     * @param dates         List<String> with available dates.
     * @param userLocalDate user's LocalDate from user.getLocalDate().
     * @return suitable String for using in printer() method.
     */
    public static String calendarPrinter(List<String> dates, LocalDate userLocalDate) {
        StringBuilder calendar = new StringBuilder();

        // Get the day of the week for the first day of the next month
        int firstDayOfWeek = userLocalDate.getDayOfWeek().getValue() - 1;
        int firstDayOfMonth = userLocalDate.getDayOfMonth();
        int monthLength = userLocalDate.lengthOfMonth();

        // Append the month and year to the calendar string
        String monthName = userLocalDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru"));
        calendar.append("<code>\n");
        calendar.append(monthName.substring(0, 1).toUpperCase() + monthName.substring(1)).append(" ").append(userLocalDate.getYear());
        calendar.append("\n");

        // Append the days of the week to the calendar string
        calendar.append("|  Пн|  Вт|  Ср|  Чт|  Пт|  Сб|  Вс\n");

        // Append the empty spaces for the days before the first day of the next month
        for (int i = 1; i <= firstDayOfWeek; i++) {
            calendar.append("     ");
        }
        // Append the calendar days
        for (int i = 1; i <= monthLength; i++) {
            if (!(dates.contains(String.valueOf(i)))) {
                calendar.append("|    ");
            } else {
                if (i < 10) {
                    calendar.append("|  0" + i);
                } else {
                    calendar.append("|  " + i);
                }
            }
            if ((i + firstDayOfWeek) % 7 == 0) {
                calendar.append("\n");
            }
        }
        calendar.append("</code>");

        // Return the generated calendar as a string
        return calendar.toString();
    }

    /**
     * This method creates List<String> with whole user's month dates.
     *
     * @param userLocalDate user's LocalDate from user.getLocalDate().
     * @return List of Strings with whole users' month dates.
     */
    public static List<String> wholeMonth(LocalDate userLocalDate) {
        int firstDayOfMonth = userLocalDate.getDayOfMonth();
        int monthLength = userLocalDate.lengthOfMonth();
        List<String> wholeMonth = new ArrayList<>();
        for (int day = firstDayOfMonth; day <= monthLength; day++) {
            wholeMonth.add(String.valueOf(day));
        }
        return wholeMonth;
    }

    /**
     * This method convert busy dates from user's input to List<String> with available dates.
     *
     * @param busyDates     - String from user's input with busy dates.
     * @param userLocalDate user's LocalDate from user.getLocalDate().
     * @return List of Strings with available dates.
     */
    public List<String> busyToAvailableConverter(String busyDates, LocalDate userLocalDate) {
        int firstDayOfMonth = userLocalDate.getDayOfMonth();

        //creating stringToParseArray
        List<String> stringToParseArray = new ArrayList<>();
        String pattern = "\\b\\d+|[-‐‑‒−–⁃۔➖˗﹘Ⲻ]";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(busyDates);

        while (matcher.find()) {
            String match = matcher.group();
            stringToParseArray.add(match);
        }

        //creating busyDaysList
        List<String> busyDaysList = new ArrayList<>();

        int monthLength = userLocalDate.lengthOfMonth();
        String pattern1 = "[-‐‑‒−–⁃۔➖˗﹘Ⲻ]";
        Pattern regex1 = Pattern.compile(pattern1);
        Matcher matcher1;

        for (int i = 0; i < stringToParseArray.size(); i++) {
            String testString = stringToParseArray.get(i);
            matcher1 = regex1.matcher(testString);
            //if matcher.Find() therefore this interval
            if (matcher1.find()) {

                boolean isStartDayHasNotMinValue = Integer.valueOf(stringToParseArray.get(i - 1)) > Integer.valueOf(stringToParseArray.get((i + 1)));
                int startDay = Integer.parseInt(stringToParseArray.get(i - 1)) - 1;
                int endDay = Integer.parseInt(stringToParseArray.get(i + 1)) + 1;
                if (isStartDayHasNotMinValue) {
                    startDay = Integer.parseInt(stringToParseArray.get(i + 1)) - 1;
                    endDay = Integer.parseInt(stringToParseArray.get(i - 1)) + 1;
                }

                for (int j = startDay; j < endDay; j++) {
                    if (j > monthLength) continue;
                    busyDaysList.add(String.valueOf(j));
                }
            } else {
                if (Integer.parseInt(stringToParseArray.get(i)) > monthLength) continue;
                busyDaysList.add(stringToParseArray.get(i));
            }
        }

        //create availableDaysList
        List<String> availableDaysList = new ArrayList<>();
        for (int i = 1; i <= monthLength; i++) {
            if (i >= firstDayOfMonth) availableDaysList.add(String.valueOf(i));
        }

        for (String i :
                busyDaysList) {
            availableDaysList.remove(i);
        }

        return availableDaysList;
    }

    public List<String> commonDates(String input) {
        String[] parts = input.split("----");
        List<List<String>> lists = new ArrayList<>();
        for (String part : parts) {
            String[] numbers = part.split(", ");
            List<String> innerList = new ArrayList<>();
            for (String number : numbers) {
                innerList.add(number);
            }
            lists.add(innerList);
        }
        ArrayList<String> commonDates = new ArrayList<>();
        for (String i : lists.get(0)) {
            commonDates.add(i);
        }
        for (int i = 1; i < lists.size(); i++) {
            commonDates.retainAll(lists.get(i));
        }
        return commonDates;
    }
    /**
     * This method returns meeting.toString with a calendar view.
     *
     * @param passphrase String with passphrase.
     * @return meeting.toString.
     */
    public String printMeeting(String passphrase,LocalDate userLocalDate) {
        StringBuilder meeting = new StringBuilder();
        meeting.append("Владелец: <b>" + meetingRepository.findOwnerByPassphrase(passphrase));
        meeting.append("</b>\nУчасники: <b>" + meetingRepository.concatenateFirstNamesByPassphrase(passphrase) + "</b>\n");
        meeting.append(calendarPrinter(
                commonDates(meetingRepository.concatenateDatesByPassphrase(passphrase)),
                userLocalDate
        ));
        meeting.append("\n");
        return meeting.toString();
    }

}
