package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Join extends Action implements ActionInterface {
    @Autowired
    public Join(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Message message) {
        Meeting meeting = findMeeting(message);
        if (ifMeetingIsFound(meeting)) {
            meetingRepository.deletePastDate();
            messageService.sendMessageTo(message.getChatId(), "Найдена встреча:");
            setCredentials(message, meeting);
            LocalDate meetingDate = getUserLocalDate(meeting.getMonth());
            messageService.sendMessageTo(meeting.getChat(), meetingToStr(meeting.getPassphrase(), meetingDate));
            setNewDates(meeting.getChat(), meetingDate, meeting);
            messageService.sendMessageTo(meeting.getChat(), "Вы присоединились к встрече:");
            saveMeeting(meeting);
            notifyParticipants(meeting, meetingDate, "<b>" + meeting.getName() + "</b> присоединился(-лась) ко встече " +
                    "<b>" + meeting.getPassphrase() + "</b>.\n");
        }
        messageService.sendMessageTo(message.getChatId(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }


    private void setNewDates(Long chatId, LocalDate userLocalDate, Meeting meeting) {
        messageService.sendMessageTo(chatId, "Введите новые даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться в формате 1 3 7-15:\n" +
                "(Если таких дат нет, введите 0)");
        String usersBusyDates = messageService.receiveMessageFrom(chatId);
        List<Integer> busyDatesList = datesParser(usersBusyDates, userLocalDate);
        if (busyDatesList.isEmpty()) {
            while (busyDatesList.isEmpty()) {
                messageService.sendMessageTo(chatId, "Не распознал числа, повторите, пожалуйста ввод.");
                messageService.sendMessageTo(chatId, "Введите даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться:");
                busyDatesList = datesParser(messageService.receiveMessageFrom(chatId), userLocalDate);
            }
        } else {
            List<List<Integer>> meetingAvailableDates = meetingRepository.concatenateDatesByPassphrase(meeting.getPassphrase());
            List<Integer> commonDates = commonDates(meetingAvailableDates);
            List<Integer> meetingBusyDates = invertDates(commonDates, userLocalDate);
            busyDatesList.addAll(meetingBusyDates);
            List<Integer> availabeDatesList = invertDates(busyDatesList, userLocalDate);
            meeting.setDates(availabeDatesList);
        }
    }
    protected boolean ifMeetingIsFound(Meeting meeting) {
        if (meeting != null) {
            boolean userAlreadyInMeetings = meetingRepository.existsByChatAndPassphrase(meeting.getChat(), meeting.getPassphrase());
            if (userAlreadyInMeetings) {
                messageService.sendMessageTo(meeting.getChat(), "Вы уже состоите в этой встрече");
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
