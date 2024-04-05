package com.example.meetSchedulerBot.service;

import com.example.meetSchedulerBot.actions.ActionInterface;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageHandler {
    MessageMonitor messageMonitor;
    Map<String, ActionInterface> actions;

    public void handle(Update update) {
        Long chatId;
        Message message = update.getMessage();
        String usersMessage;
        if(update.hasCallbackQuery()) {
            usersMessage = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getFrom().getId();
        } else {
            usersMessage = message.getText();
            chatId = message.getChatId();
        }
        if (messageContainsCommand(usersMessage)) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> actions.get(usersMessage.substring(1)).run(message));
            executorService.shutdown();
        } else if (usersMessage.contains("/start ")) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> actions.get("meetingID").run(message));
            executorService.shutdown();
        } else {
            messageMonitor.completeRequestedMessage(chatId, usersMessage);
        }
    }

    private boolean messageContainsCommand(String usersMessage) {
        if (!usersMessage.contains(" ") && usersMessage.charAt(0) == '/') {
            return actions.containsKey(usersMessage.substring(1));
        }
        return false;
    }
}
