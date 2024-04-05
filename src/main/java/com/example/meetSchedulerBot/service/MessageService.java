package com.example.meetSchedulerBot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

/**Represents interface for message exchange with user and server
 * */
@Component
public interface MessageService {

    /**
     * Sends a message to the specified chat ID.
     *
     * @param  chatId   the ID of the chat to send the message to
     * @param  message  the message to send
     */
    void sendMessageTo(Long chatId, String message);


    /**
     * Retrieves a message from the specified chat ID.
     *
     * @param  chatId  the ID of the chat to retrieve the message from
     * @return         the received message as a string
     */
    String receiveMessageFrom(Long chatId);

    void sendInlineKeyboardTo(Long chatId, String message, InlineKeyboardMarkup inlineKeyboardMarkup);
}
