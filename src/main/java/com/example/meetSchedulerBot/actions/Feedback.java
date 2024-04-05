package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class Feedback extends Action implements ActionInterface {
    @Value("${feedbackChatID}")
    Long feedbackChatID;

    public Feedback(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Message message) {
        messageService.sendMessageTo(message.getChatId(), "Напишите Ваши замечения, предложения, пожелания:");
        String feedback = messageService.receiveMessageFrom(message.getChatId());
        messageService.sendMessageTo(feedbackChatID, message.getChat().getFirstName() + " оставил отзыв: " + feedback);
        messageService.sendMessageTo(message.getChatId(), "Спасибо за Ваш отзыв!");
    }
}