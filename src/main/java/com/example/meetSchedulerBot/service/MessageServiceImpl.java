package com.example.meetSchedulerBot.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

/**
 * A service implementation class for sending and receiving messages.
 */
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageServiceImpl implements MessageService {
    MessageMonitor messageMonitor;
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void sendMessageTo(Long chatId, String message) {
        publishEvent(new SendMessage(chatId.toString(), message));
    }

    @Override
    public String receiveMessageFrom(Long chatId) {
        return messageMonitor.requestIncomingMessage(chatId);
    }

    @Override
    public void sendInlineKeyboardTo(Long chatId, String message, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(message);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        publishEvent(sendMessage);
    }

    private void publishEvent(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}
