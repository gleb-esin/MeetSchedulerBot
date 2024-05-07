package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Find extends Action implements ActionInterface {
    public Find(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Meeting meeting) {
        meetingRepository.deleteExpiredMeetings();
        meetingRepository.deletePastDate();
        messageService.sendMessageTo(meeting.getChat(), "Найдена встреча: ");
        setDateCredentials(meeting);
        messageService.sendMessageTo(meeting.getChat(), meetingToStr(meeting.getPassphrase(), getUserLocalDate(meeting.getMonth())));
        messageService.sendMessageTo(meeting.getChat(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }
}