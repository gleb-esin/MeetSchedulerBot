package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Edit extends Action implements ActionInterface, ListableInterface {
    @Autowired
    public Edit(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Meeting meeting) {
        meetingRepository.deleteExpiredMeetings();
        meetingRepository.deletePastDate();
        String meetings = getMeetingsList(meeting, "edit");
        messageService.sendMessageTo(meeting.getChat(), meetings);
    }

    @Override
    protected void setNewDates(Meeting meeting) {
        String[] buttonStr = {"Свободные дни-addFreeDays", "Занятые дни-addBusyDays"};
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkupBuilder(buttonStr);
        messageService.sendInlineKeyboardTo(meeting.getChat(), "Выберите действие, чтобы изменить:", inlineKeyboardMarkup);
        String answer = messageService.receiveMessageFrom(meeting.getChat());
        if (answer.equals("addFreeDays")) {
            editFreeDays(meeting);
        } else if (answer.equals("addBusyDays")) {
            editBusyDays(meeting);
        }
    }

    protected void editFreeDays(Meeting meeting) {
        LocalDate meetingLocalDate = getUserLocalDate(meeting.getMonth());
        List<Integer> usersFreeDates;
        List<Integer> usersBusyDates;
        List<Integer> usersFreeDatesList = new ArrayList<>();
        messageService.sendMessageTo(meeting.getChat(), "Ваши <b>свободные</b> дни, которые можно изменить:");
        messageService.sendMessageTo(meeting.getChat(), calendarPrinter(removePassedDays(meeting.getDates(), meetingLocalDate), meetingLocalDate));
        messageService.sendMessageTo(meeting.getChat(), "Выберите даты выше, которые хотите <b>изменить на занятые</b>\n(в формате 1 3 7-15:)\n"
                + "(Если таких дат нет, введите 0)");
        do {
            usersFreeDatesList.clear();
            usersFreeDates = meeting.getDates();
            usersBusyDates = invertDates(usersFreeDates, meetingLocalDate);
            String newBusyDatesStr = messageService.receiveMessageFrom(meeting.getChat());
            usersBusyDates.addAll(datesParser(newBusyDatesStr, meetingLocalDate));
            usersFreeDatesList = invertDates(usersBusyDates, meetingLocalDate);
            if (usersFreeDatesList.isEmpty()) {
                messageService.sendMessageTo(meeting.getChat(), "Вы не оставили свободных дат другим учасникам, повторите попытку!");
            }
        } while (usersFreeDatesList.isEmpty());
        meeting.setDates(usersFreeDatesList);
        meeting.setExpired(meetingLocalDate, meeting.getDates());
        messageService.sendMessageTo(meeting.getChat(), "Занятые дни во встрече успешно обновлены: ");
    }

    protected void editBusyDays(Meeting meeting) {
        LocalDate meetingLocalDate = getUserLocalDate(meeting.getMonth());
        List<Integer> usersFreeDates = meetingRepository.findDatesByPassphraseAndChat(meeting.getPassphrase(), meeting.getChat());
        List<Integer> usersBusyDates = invertDates(usersFreeDates, meetingLocalDate);
        messageService.sendMessageTo(meeting.getChat(), "Ваши <b>занятые</b> дни, которые можно изменить:");
        messageService.sendMessageTo(meeting.getChat(), calendarPrinter(usersBusyDates, meetingLocalDate));
        messageService.sendMessageTo(meeting.getChat(), "Выберите даты выше, которые хотите <b>изменить на свободные</b>\n(в формате 1 3 7-15:)\n"
                + "(Если таких дат нет, введите 0)");
        String newFreeDatesStr = messageService.receiveMessageFrom(meeting.getChat());
        List<Integer> usersFreeDatesList = datesParser(newFreeDatesStr, meetingLocalDate);
        if (!usersFreeDatesList.contains(0)) {
            usersFreeDates.addAll(usersFreeDatesList);
        }
        meeting.setDates(usersFreeDates);
        meeting.setExpired(meetingLocalDate, meeting.getDates());
        messageService.sendMessageTo(meeting.getChat(), "Свободные дни во встрече успешно обновлены: ");
    }

    @Override
    public void handleMeeting(Meeting meeting) {
        meeting = meetingRepository.findByChatAndPassphrase(meeting.getChat(), meeting.getPassphrase());
        messageService.sendMessageTo(meeting.getChat(), "Найдена встреча: ");
        messageService.sendMessageTo(meeting.getChat(), meetingToStr(meeting.getPassphrase(), getUserLocalDate(meeting.getMonth())));
        setNewDates(meeting);
        messageService.sendMessageTo(meeting.getChat(), "Встреча " + meeting.getPassphrase().split("-")[0] + " успешно изменена!\n");
        saveMeeting(meeting);
        String notification = meeting.getName() + " изменил свои даты участия во встрече <b>" + meeting.getPassphrase().split("-")[0] + "</b>:";
        notifyParticipants(meeting, getUserLocalDate(meeting.getMonth()), notification);
        messageService.sendMessageTo(meeting.getChat(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }
}