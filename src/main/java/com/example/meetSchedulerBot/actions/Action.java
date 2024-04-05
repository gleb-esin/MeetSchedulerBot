package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class Action {
    MessageService messageService;
    MeetingRepository meetingRepository;
    @NonFinal
    @Value("${bot.name}")
    private String telegramBotUsername;

    @Autowired
    protected Action(MessageService messageService, MeetingRepository meetingRepository) {
        this.messageService = messageService;
        this.meetingRepository = meetingRepository;
    }

    public String calendarPrinter(List<Integer> availableDates, LocalDate userLocalDate) {
        StringBuilder calendar = new StringBuilder();
        int firstDayOfWeek = userLocalDate.getDayOfWeek().getValue();
        int monthLength = userLocalDate.lengthOfMonth();

        // Append the month and year to the calendar string
        String monthName = userLocalDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru"));
        calendar.append("<code>");
        calendar.append(monthName.substring(0, 1).toUpperCase()).append(monthName.substring(1)).append(" ").append(userLocalDate.getYear());
        calendar.append("\n");

        // Append the days of the week to the calendar string
        calendar.append("|  Пн|  Вт|  Ср|  Чт|  Пт|  Сб|  Вс\n");

        // Append the empty spaces for the days before the first day of the month
        calendar.append("     ".repeat(Math.max(0, firstDayOfWeek - 1)));
        // Append the calendar days
        for (int i = 1; i <= monthLength; i++) {
            if (!(availableDates.contains(i))) {
                calendar.append("|    ");
            } else {
                if (i < 10) {
                    calendar.append("|  0").append(i);
                } else {
                    calendar.append("|  ").append(i);
                }
            }
            if ((i + firstDayOfWeek - 1) % 7 == 0) {
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
    public List<Integer> wholeMonth(LocalDate userLocalDate) {
        int firstDayOfMonth = userLocalDate.getDayOfMonth();
        int monthLength = userLocalDate.lengthOfMonth();
        List<Integer> wholeMonth = new ArrayList<>();
        for (int day = firstDayOfMonth; day <= monthLength; day++) {
            wholeMonth.add(day);
        }
        return wholeMonth;
    }

    /**
     * This method convert string with dates from user's input String to List of Integer.
     *
     * @param inputDates    - String from user's input with dates.
     * @param userLocalDate - LocalDate of meeting;
     * @return List of Integer with dates.
     */
    public List<Integer> datesParser(String inputDates, LocalDate userLocalDate) {
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
        for (int i = 0; i < stringToParseArray.size(); i++) {
            String testString = String.valueOf(stringToParseArray.get(i));
            matcher1 = regex1.matcher(testString);
            //if matcher.Find() therefore it's interval
            if (matcher1.find()) {
                boolean isStartDayHasNotMinValue = Integer.parseInt(stringToParseArray.get(i - 1)) > Integer.parseInt(stringToParseArray.get((i + 1)));
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
    public List<Integer> invertDates(List<Integer> parsedDaysList, LocalDate userLocalDate) {
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
        List<Integer> commonDates = new ArrayList<>(lists.get(0));
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
    public String meetingToStr(String passphrase, LocalDate userLocalDate) {
        StringBuilder meeting = new StringBuilder();
        meeting.append("Название: <b>").append(passphrase).append("</b>\n");
        try {
            meeting.append("Владелец: <b>").append(meetingRepository.findOwnerByPassphrase(passphrase));
        } catch (DataAccessException e) {
            log.error("findOwnerByPassphrase(passphrase) " + e.getMessage());
        }
        try {
            meeting.append("</b>\nУчасники: <b>").append(meetingRepository.concatenateFirstNamesByPassphrase(passphrase)).append("</b>\n");
        } catch (DataAccessException e) {
            log.error("concatenateFirstNamesByPassphrase(passphrase) " + e.getMessage());
        }
        meeting.append("\nДаты:\n");
        try {
            meeting.append(calendarPrinter(commonDates(meetingRepository.concatenateDatesByPassphrase(passphrase)), userLocalDate));
        } catch (DataAccessException e) {
            log.error("concatenateDatesByPassphrase(passphrase) " + e.getMessage());
        }
        meeting.append("\n");
        return meeting.toString();
    }

    public LocalDate getUserLocalDate(int month) {
        LocalDate userLocalDate;
        if (month == LocalDate.now().getMonthValue()) {
            userLocalDate = LocalDate.now();
        } else if (month > LocalDate.now().getMonthValue()) {
            userLocalDate = LocalDate.of(LocalDate.now().getYear(), month, 1);
        } else {
            userLocalDate = LocalDate.of(LocalDate.now().plusYears(1).getYear(), month, 1);
        }
        return userLocalDate;
    }

    protected Meeting findMeeting(Message message) {
        Meeting meeting = new Meeting();
        String passphrase;
        if (message.getText().contains("passphrase=")) {
            passphrase = message.getText().split("=")[1];
        } else if (message.getText().contains("/start ")) {
            passphrase = message.getText().split(" ")[1];
            byte[] chars = Base64.getDecoder().decode(passphrase);
            passphrase = new String(chars);
        } else {
            messageService.sendMessageTo(message.getChatId(), "Введите название встречи: ");
            passphrase = messageService.receiveMessageFrom(message.getChatId());
        }
        boolean meetingIsNotFound = !meetingRepository.existsByPassphrase(passphrase);
        if (meetingIsNotFound) {
            messageService.sendMessageTo(message.getChatId(), "Такая встреча не найдена");
            meeting = null;
        } else {
            meeting.setChat(message.getChatId());
            meeting.setPassphrase(passphrase);
        }
        return meeting;
    }


    /**
     * Sets new dates for a meeting.
     *
     * @param meeting the meeting object to update the dates for
     */
    protected void setNewDates(Meeting meeting) {
        Long chatId = meeting.getChat();
        messageService.sendMessageTo(chatId, "Введите новые даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться в формате 1 3 7-15:\n" + "(Если таких дат нет, введите 0)");
        LocalDate userLocalDate = getUserLocalDate(meeting.getMonth());
        String datesString = messageService.receiveMessageFrom(chatId);
        List<Integer> busyDates = datesParser(datesString, userLocalDate);
        if (busyDates.isEmpty()) {
            while (busyDates.isEmpty()) {
                messageService.sendMessageTo(chatId, "Не распознал числа, повторите, пожалуйста ввод.");
                messageService.sendMessageTo(chatId, "Введите даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться:");
                busyDates = datesParser(messageService.receiveMessageFrom(chatId), userLocalDate);
            }
        }
        meeting.setDates(invertDates(busyDates, userLocalDate), userLocalDate);
    }

    protected void saveMeeting(Meeting meeting) {
        meetingRepository.deleteExpiredMeetings();
        meetingRepository.save(meeting);
        messageService.sendMessageTo(meeting.getChat(), meetingToStr(meeting.getPassphrase(), getUserLocalDate(meeting.getMonth())));
    }

    protected void notifyParticipants(Meeting meeting, LocalDate meetingDate, String notification) {
        String notifiedStr = meetingRepository.listOfNotified(meeting.getPassphrase());
        String[] notifiedArr = notifiedStr.split(" ");
        List<Long> notifiedList = new ArrayList<>();
        for (String string : notifiedArr) {
            notifiedList.add(Long.valueOf(string));
        }
        notifiedList.remove(meeting.getChat());
        notifiedList.forEach(chat -> messageService.sendMessageTo(chat, notification + "\n" + meetingToStr(meeting.getPassphrase(), meetingDate)));
    }

    protected void setCredentials(Message message, Meeting meeting) {
        meeting.setName(message.getFrom().getFirstName());
        meeting.setMonth(meetingRepository.findMonthByPassphrase(meeting.getPassphrase()));
        boolean isUserParticipant = meetingRepository.existsByChatAndPassphrase(meeting.getChat(), meeting.getPassphrase());
        boolean isUserOwner = false;
        if (isUserParticipant) {
            isUserOwner = meetingRepository.isUserOwner(meeting.getChat(), meeting.getPassphrase());
        }
        meeting.setOwner(isUserOwner);
        List<Integer> dates = commonDates(meetingRepository.concatenateDatesByPassphrase(meeting.getPassphrase()));
        meeting.setDates(dates, getUserLocalDate(meeting.getMonth()));
    }


    protected InlineKeyboardMarkup inlineKeyboardMarkupBuilder(String[] buttonStr) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttonRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (String string : buttonStr) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            String[] buttonArr = string.split("-");
            inlineKeyboardButton.setText(buttonArr[0]);
            inlineKeyboardButton.setCallbackData(buttonArr[1]);
            row.add(inlineKeyboardButton);
        }
        buttonRows.add(row);
        inlineKeyboardMarkup.setKeyboard(buttonRows);
        return inlineKeyboardMarkup;
    }

    protected String linkCreator(Meeting meeting, String description) {
        byte[] byteArray = meeting.getPassphrase().getBytes();
        return "<a href=\"https://t.me/" + telegramBotUsername + "?start=" + Base64.getEncoder().encodeToString(byteArray) + "\">" + description + "</a>";
    }
}
