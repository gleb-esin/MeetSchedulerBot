package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;

@Slf4j
@Component("deletemeeting")
public class DeleteMeeting extends Action implements ActionInterface, ListableInterface {
    public DeleteMeeting(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Meeting meeting) {
        String meetings;
        if (meetingRepository.existsByChatAndOwner(meeting.getChat(), true)) {
            meetings = getMeetingsList(meeting, "deletemeeting");
            messageService.sendMessageTo(meeting.getChat(), meetings);
        }else {
            messageService.sendMessageTo(meeting.getChat(), "Вы пока не организовали ни одной встречи!");
            messageService.sendMessageTo(meeting.getChat(), "Чтобы продолжить, выбери что-нибудь из меню.");
        }
    }

    private void deleteMeeting(Meeting meeting) {
        String[] buttonStr = {"Да-yes", "Нет-no"};
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkupBuilder(buttonStr);
        messageService.sendInlineKeyboardTo(meeting.getChat(), "Вы точно хоите удалить эту встречу?", inlineKeyboardMarkup);
        String answer = messageService.receiveMessageFrom(meeting.getChat());
        if (answer.equals("yes")) {
            notifyParticipants(meeting, getUserLocalDate(meeting.getMonth()), "<b>" + meeting.getName() + "</b> удалил(-ла) встречу:");
            meetingRepository.deleteByPassphrase(meeting.getPassphrase());
            messageService.sendMessageTo(meeting.getChat(), "Встреча " + meeting.getPassphrase().split("-")[0] + " успешно удалена!");
        }
    }

    @Override
    public void handleMeeting(Meeting meeting) {
        meetingRepository.deleteExpiredMeetings();
        meetingRepository.deletePastDate();
        messageService.sendMessageTo(meeting.getChat(), "Найдена встреча:");
        setDateCredentials(meeting);
        LocalDate meetingDate = getUserLocalDate(meeting.getMonth());
        messageService.sendMessageTo(meeting.getChat(), meetingToStr(meeting.getPassphrase(), meetingDate));
        deleteMeeting(meeting);
        messageService.sendMessageTo(meeting.getChat(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }
}