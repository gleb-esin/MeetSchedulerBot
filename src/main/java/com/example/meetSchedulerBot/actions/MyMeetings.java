package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import org.springframework.stereotype.Component;

@Component("mymeetings")
public class MyMeetings extends Action implements ActionInterface {
    public MyMeetings(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Meeting meeting) {
        meetingRepository.deleteExpiredMeetings();
        meetingRepository.deletePastDate();
        if (meetingRepository.existsByChat(meeting.getChat())) {
            String meetings = getMeetingsList(meeting, "find");
            messageService.sendMessageTo(meeting.getChat(), meetings);
        } else {
            messageService.sendMessageTo(meeting.getChat(), "Вы пока не состоите ни в одной встрече");
        }
        messageService.sendMessageTo(meeting.getChat(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }
}