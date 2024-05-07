package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public void run(Meeting meeting) {
        boolean userIsNotParticipant = !meetingRepository.existsByChatAndPassphrase2(meeting.getChat(), meeting.getPassphrase());
        if (userIsNotParticipant) {
            meetingRepository.deleteExpiredMeetings();
            meetingRepository.deletePastDate();
            messageService.sendMessageTo(meeting.getChat(), "Найдена встреча:");
            setDateCredentials(meeting);
            LocalDate meetingDate = getUserLocalDate(meeting.getMonth());
            messageService.sendMessageTo(meeting.getChat(), meetingToStr(meeting.getPassphrase(), meetingDate));
            setNewDates(meeting.getChat(), meetingDate, meeting);
            messageService.sendMessageTo(meeting.getChat(), "Вы присоединились к встрече:");
            saveMeeting(meeting);
            notifyParticipants(meeting, meetingDate, "<b>" + meeting.getName() + "</b> присоединился(-лась) ко встече " +
                    "<b>" + meeting.getPassphrase().split("-")[0] + "</b>.\n");
        } else {
            messageService.sendMessageTo(meeting.getChat(), "Вы уже состоите в этой встрече!");
        }
        messageService.sendMessageTo(meeting.getChat(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }


    private void setNewDates(Long chatId, LocalDate userLocalDate, Meeting meeting) {
        do {
            messageService.sendMessageTo(chatId, "Введите даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться в формате 1 3 7-15:\n" +
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
                List<Integer> availabeDatesList = invertDates(busyDatesList, userLocalDate);
                meeting.setDates(availabeDatesList);
                meeting.setExpired(userLocalDate, availabeDatesList);
            }
            if (meeting.getDates().isEmpty()) {
                messageService.sendMessageTo(chatId, "Вы не оставили свободных дат другим учасникам!");
                messageService.sendMessageTo(chatId, "Введите новые даты даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться в формате 1 3 7-15:\n" +
                        "(Если таких дат нет, введите 0)");
            }
        } while (meeting.getDates().isEmpty());
    }
}
