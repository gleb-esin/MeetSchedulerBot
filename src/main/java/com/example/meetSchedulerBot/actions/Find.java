package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@Scope("prototype")
public class Find extends Action implements ActionInterface {
    public Find(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Message message) {
        Meeting meeting = findMeeting(message);
        if (ifMeetingIsFound(meeting)) {
            messageService.sendMessageTo(message.getChatId(), "Найдена встреча: ");
            setCredentials(message, meeting);
            messageService.sendMessageTo(meeting.getChat(), meetingToStr(meeting.getPassphrase(), getUserLocalDate(meeting.getMonth())));
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
}