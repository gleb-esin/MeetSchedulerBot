package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;

@Component("removeme")
public class RemoveMe extends Action implements ActionInterface {
    public RemoveMe(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Message message) {
        Meeting meeting = findMeeting(message);
        if (ifMeetingIsFound(meeting)) {
            meetingRepository.deleteExpiredMeetings();
            meetingRepository.deletePastDate();
            messageService.sendMessageTo(message.getChatId(), "Найдена встреча:");
            setCredentials(message, meeting);
            LocalDate meetingDate = getUserLocalDate(meeting.getMonth());
            messageService.sendMessageTo(meeting.getChat(), meetingToStr(meeting.getPassphrase(), meetingDate));
            deleteMeeting(meeting);
            notifyParticipants(meeting, getUserLocalDate(meeting.getMonth()), "<b>" + meeting.getName() + "</b> не будет участвовать во встече:");
        }
        messageService.sendMessageTo(message.getChatId(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }

    private void deleteMeeting(Meeting meeting) {
        String[] buttonStr = {"Да-yes", "Нет-no"};
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkupBuilder(buttonStr);
        messageService.sendInlineKeyboardTo(meeting.getChat(), "Вы точно хоите удалить свое участие в этой встрече?", inlineKeyboardMarkup);
        String answer = messageService.receiveMessageFrom(meeting.getChat());
        if (answer.equals("yes")) {
            Long nextOwner = meetingRepository.whoWillBeNextOwner(meeting.getPassphrase());
            meetingRepository.setNextOwner(nextOwner, meeting.getPassphrase());
            meetingRepository.deleteByChatAndPassphrase(meeting.getChat(), meeting.getPassphrase());
            messageService.sendMessageTo(meeting.getChat(), "Вы больше не участвуете во встрече " + meeting.getPassphrase());
        }
    }

    protected boolean ifMeetingIsFound(Meeting meeting) {
        if (meeting != null) {
            boolean userIsParticipant = meetingRepository.existsByChatAndPassphrase(meeting.getChat(), meeting.getPassphrase());
            if (userIsParticipant) {
                return true;
            }
            messageService.sendMessageTo(meeting.getChat(), "Вы не состоите в этой встрече");
        }
        return false;
    }
}