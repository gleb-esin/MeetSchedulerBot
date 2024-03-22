package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.MeetingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Slf4j

public class Action {
    @Autowired
    MeetingRepository meetingRepository;

    public static String calendarPrinter(List<Integer> availableDates, LocalDate userLocalDate) {
        StringBuilder calendar = new StringBuilder();
        int firstDayOfWeek = userLocalDate.getDayOfWeek().getValue();
        int monthLength = userLocalDate.lengthOfMonth();

        // Append the month and year to the calendar string
        String monthName = userLocalDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru"));
        calendar.append("<code>");
        calendar.append(monthName.substring(0, 1).toUpperCase() + monthName.substring(1)).append(" ").append(userLocalDate.getYear());
        calendar.append("\n");

        // Append the days of the week to the calendar string
        calendar.append("|  Пн|  Вт|  Ср|  Чт|  Пт|  Сб|  Вс\n");

        // Append the empty spaces for the days before the first day of the month
        for (int i = 2; i <= firstDayOfWeek; i++) {
            calendar.append("     ");
        }
        // Append the calendar days
        for (int i = 1; i <= monthLength; i++) {
            if (!(availableDates.contains(i))) {
                calendar.append("|    ");
            } else {
                if (i < 10) {
                    calendar.append("|  0" + i);
                } else {
                    calendar.append("|  " + i);
                }
            }
            if ((i + firstDayOfWeek-1) % 7 == 0) {
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
    public static List<Integer> wholeMonth(LocalDate userLocalDate) {
        int firstDayOfMonth = userLocalDate.getDayOfMonth();
        int monthLength = userLocalDate.lengthOfMonth();
        List<Integer> wholeMonth = new ArrayList<>();
        for (int day = firstDayOfMonth; day <= monthLength; day++) {
            wholeMonth.add(day);
        }
        return wholeMonth;
    }

    /**
     * This method convert string whith dates from user's input to Listof Integer with free dates.
     *
     * @param inputDates - String from user's input with dates.
     * @param userLocalDate  - user's LocalDate from answer.getMeeting()
     * @return List of Integer with dates.
     */
    public static List<Integer> datesParser(String inputDates, LocalDate userLocalDate) {
        //create List<String> of dates
        List<String> stringToParseArray = new ArrayList<>();
        String pattern = "\\b\\d+|[-‐‑‒−–⁃۔➖˗﹘Ⲻ]";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(inputDates);
        while (matcher.find()) {
            String match = matcher.group();
            stringToParseArray.add(match);
        }

        //remove extreme '-'
        pattern = "[-‐‑‒−–⁃۔➖˗﹘Ⲻ]";
        regex = Pattern.compile(pattern);
        matcher = regex.matcher(inputDates);
        for (int i = 0; i < stringToParseArray.size(); i++) {
            while (matcher.find()) {
                String match = matcher.group();
                // remove '-' from head
                while (!stringToParseArray.isEmpty() && stringToParseArray.get(0).equals(match)) {
                    stringToParseArray.remove(0);
                }

                // remove '-' from tail
                while (!stringToParseArray.isEmpty() && stringToParseArray.get(stringToParseArray.size() - 1).equals(match)) {
                    stringToParseArray.remove(stringToParseArray.size() - 1);
                }
            }
        }
        List<Integer> parsedDaysList = new ArrayList<>();
        int monthLength = userLocalDate.lengthOfMonth();
        String pattern1 = "[-‐‑‒−–⁃۔➖˗﹘Ⲻ]";
        Pattern regex1 = Pattern.compile(pattern1);
        Matcher matcher1;

        for(int i = 0; i<stringToParseArray.size();i++){
            String testString = String.valueOf(stringToParseArray.get(i));
            matcher1 = regex1.matcher(testString);
            //if matcher.Find() therefore this interval
            if (matcher1.find()) {
                boolean isStartDayHasNotMinValue = Integer.valueOf(stringToParseArray.get(i - 1)) > Integer.valueOf(stringToParseArray.get((i + 1)));
                int startDay = Integer.parseInt(stringToParseArray.get(i - 1)) + 1;
                int endDay = Integer.parseInt(stringToParseArray.get(i + 1));
                if (isStartDayHasNotMinValue) {
                    startDay = Integer.parseInt(stringToParseArray.get(i + 1)) + 1;
                    endDay = Integer.parseInt(stringToParseArray.get(i - 1));
                }
                for (int j = startDay; j < endDay; j++) {
                    if (j > monthLength) continue;
                    parsedDaysList.add(j);
                }

            } else {
                if (Integer.parseInt(stringToParseArray.get(i)) > monthLength) continue;
                parsedDaysList.add(Integer.parseInt(stringToParseArray.get(i)));
            }
        }
        Collections.sort(parsedDaysList);
        return parsedDaysList;
    }

    /**
     * If user input busy dates this method will convert available dates from datesParser() to List of Integer with free dates.
     *
     * @param parsedDaysList - List of Integer from datesParser().
     * @param userLocalDate  - user's LocalDate from answer.getMeeting()
     * @return List of Integer with available dates.
     */
    public static List<Integer> busyToAvailableConverter(List<Integer> parsedDaysList, LocalDate userLocalDate) {
        int firstDayOfMonth = userLocalDate.getDayOfMonth();
        int monthLength = userLocalDate.lengthOfMonth();
        List<Integer> availableDaysList = new ArrayList<>();
        for (int i = 1; i <= monthLength; i++) {
            if (i >= firstDayOfMonth) availableDaysList.add(i);
        }
        for (Integer i : parsedDaysList) {
            availableDaysList.remove(i);
        }
        return availableDaysList;
    }

    public List<Integer> commonDates(List<List<Integer>> lists) {
        List<Integer> commonDates = new ArrayList<>();
        commonDates.addAll(lists.get(0));
        for (int i = 1; i < lists.size(); i++){
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
    public String printMeeting(String passphrase, LocalDate userLocalDate) {
        StringBuilder meeting = new StringBuilder();
        try {
            meeting.append("Владелец: <b>" + meetingRepository.findOwnerByPassphrase(passphrase));
        }catch (DataAccessException e){
            log.error("findOwnerByPassphrase(passphrase) "+e.getMessage());
        }
        try {
            meeting.append("</b>\nУчасники: <b>" + meetingRepository.concatenateFirstNamesByPassphrase(passphrase) + "</b>\n");
        } catch (DataAccessException e){
            log.error("concatenateFirstNamesByPassphrase(passphrase) " + e.getMessage());
        }
        meeting.append("\nДаты:\n");
        try {
            meeting.append(calendarPrinter(commonDates(meetingRepository.concatenateDatesByPassphrase(passphrase)), userLocalDate));
        }  catch (DataAccessException e){
           log.error("concatenateDatesByPassphrase(passphrase) " + e.getMessage());
        }
        meeting.append("\n");
        return meeting.toString();
    }

}
