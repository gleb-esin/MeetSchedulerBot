package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Edit extends Action implements ActionInterface {
    @Autowired
    public Edit(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Message message) {
        Meeting meeting = findMeeting(message);
        if (ifMeetingIsFound(meeting)) {
            messageService.sendMessageTo(message.getChatId(), "Найдена встреча: ");
            messageService.sendMessageTo(meeting.getChat(), meetingToStr(meeting.getPassphrase(), getUserLocalDate(meeting.getMonth())));
            setNewDates(meeting);
            messageService.sendMessageTo(meeting.getChat(), "Встреча " + meeting.getPassphrase() + " успешно изменена!\n");
            saveMeeting(meeting);
            String notification = meeting.getName() + " изменил свои даты участия во встрече <b>" + meeting.getPassphrase() + "</b>:";
            notifyParticipants(meeting, getUserLocalDate(meeting.getMonth()), notification);
        }
        messageService.sendMessageTo(message.getChatId(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }

    protected boolean ifMeetingIsFound(Meeting meeting) {
        if (meeting != null) {
            boolean userIsNotParticipant = !meetingRepository.existsByChatAndPassphrase(meeting.getChat(), meeting.getPassphrase());
            if (userIsNotParticipant) {
                messageService.sendMessageTo(meeting.getChat(), "Вы не состоите в этой встрече");
                return false;
            } else {
                return true;
            }
        }
        return false;
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

    protected void editBusyDays(Meeting meeting) {
        LocalDate meetingLocalDate = getUserLocalDate(meeting.getMonth());
        List<Integer> usersFreeDates = meetingRepository.findDatesByPassphraseAndChat(meeting.getPassphrase(), meeting.getChat());
        List<Integer> usersBusyDates = invertDates(usersFreeDates, meetingLocalDate);
        messageService.sendMessageTo(meeting.getChat(), "Ваши свободные дни, которые можно изменить:");
        messageService.sendMessageTo(meeting.getChat(), calendarPrinter(usersFreeDates, meetingLocalDate));
        messageService.sendMessageTo(meeting.getChat(), "Добавьте новые занятые дни в формате 1 3 7-15:\n"
                + "(Если таких дат нет, введите 0)");
        String newBusyDatesStr = messageService.receiveMessageFrom(meeting.getChat());
        List<Integer> newBusyDates = datesParser(newBusyDatesStr, meetingLocalDate);
        newBusyDates.addAll(usersBusyDates);
        List<Integer> newFreeDates = invertDates(newBusyDates, meetingLocalDate);
        meeting.setDates(newFreeDates);
        messageService.sendMessageTo(meeting.getChat(), "Занятые дни во встрече успешно обновлены: ");
    }

    protected void editFreeDays(Meeting meeting) {
        LocalDate meetingLocalDate = getUserLocalDate(meeting.getMonth());
        List<Integer> usersFreeDates = meetingRepository.findDatesByPassphraseAndChat(meeting.getPassphrase(), meeting.getChat());
        List<Integer> usersBusyDates = invertDates(usersFreeDates, meetingLocalDate);
        messageService.sendMessageTo(meeting.getChat(), "Ваши занятые дни, которые можно изменить:");
        messageService.sendMessageTo(meeting.getChat(), calendarPrinter(usersBusyDates, meetingLocalDate));
        messageService.sendMessageTo(meeting.getChat(), "Добавьте новые свободные даты в формате 1 3 7-15:\n"
                + "(Если таких дат нет, введите 0)");
        String newFreeDatesStr = messageService.receiveMessageFrom(meeting.getChat());
        List<Integer> newFreeDates = datesParser(newFreeDatesStr, meetingLocalDate);
        newFreeDates.addAll(usersFreeDates);
        meeting.setDates(newFreeDates);
        messageService.sendMessageTo(meeting.getChat(), "Свободные дни во встрече успешно обновлены: ");
    }
}