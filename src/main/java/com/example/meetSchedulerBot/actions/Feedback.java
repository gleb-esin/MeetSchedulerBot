package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Feedback extends Action implements ActionInterface {
    @Value("${feedbackChatID}")
    Long feedbackChatID;

    public Feedback(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Meeting meeting) {
        messageService.sendMessageTo(meeting.getChat(), "Напишите Ваши замечения, предложения, пожелания:");
        String feedback = messageService.receiveMessageFrom(meeting.getChat());
        messageService.sendMessageTo(feedbackChatID, meeting.getName() + " оставил отзыв: " + feedback);
        messageService.sendMessageTo(meeting.getChat(), "Спасибо за Ваш отзыв!");
    }
}