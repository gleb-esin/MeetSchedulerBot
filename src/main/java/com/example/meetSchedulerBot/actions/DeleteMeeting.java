package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;

@Slf4j
@Component("deletemeeting")
public class DeleteMeeting extends Action implements ActionInterface {
    public DeleteMeeting(MessageService messageService, MeetingRepository meetingRepository) {
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
            deleteMeeting(meeting);
        }
        messageService.sendMessageTo(message.getChatId(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }

    private void deleteMeeting(Meeting meeting) {
        String[] buttonStr = {"Да-yes", "Нет-no"};
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkupBuilder(buttonStr);
        messageService.sendInlineKeyboardTo(meeting.getChat(), "Вы точно хоите удалить эту встречу?", inlineKeyboardMarkup);
        String answer = messageService.receiveMessageFrom(meeting.getChat());
        if (answer.equals("yes")) {
            notifyParticipants(meeting, getUserLocalDate(meeting.getMonth()), "<b>" + meeting.getName() + "</b> удалил(-ла) встречу:");
            meetingRepository.deleteByPassphrase(meeting.getPassphrase());
            messageService.sendMessageTo(meeting.getChat(), "Встреча " + meeting.getPassphrase() + " успешно удалена!");
        }
    }

    protected boolean ifMeetingIsFound(Meeting meeting) {
        if (meeting != null) {
            boolean userIsParticipant = meetingRepository.existsByChatAndPassphrase(meeting.getChat(), meeting.getPassphrase());
            if (userIsParticipant) {
                boolean userIsOwner = meetingRepository.isUserOwner(meeting.getChat(), meeting.getPassphrase());
                if (userIsOwner) {
                    return true;
                }
            }
            messageService.sendMessageTo(meeting.getChat(), "Вы не можете удалить эту встречу");
        }
        return false;
    }
}