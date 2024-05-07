package com.example.meetSchedulerBot.service;

import com.example.meetSchedulerBot.actions.ActionInterface;
import com.example.meetSchedulerBot.actions.ListableInterface;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64.Base64;
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
    Map<String, ListableInterface> listables;
    Map<String, ActionInterface> actions;
    MessageService messageService;
    MeetingRepository meetingRepository;

    public void handle(Update update) {
        Long chatId;
        Message message = update.getMessage();
        String firstName;
        String usersMessage;
        if (update.hasCallbackQuery()) {
            usersMessage = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getFrom().getId();
            firstName = update.getCallbackQuery().getFrom().getFirstName();
        } else {
            usersMessage = message.getText();
            chatId = message.getChatId();
            firstName = message.getChat().getFirstName();
        }
        Meeting meeting = new Meeting();
        meeting.setChat(chatId);
        meeting.setName(firstName);
        if (messageContainsCommand(usersMessage)) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> actions.get(usersMessage.substring(1)).run(meeting));
            executorService.shutdown();
        } else if (usersMessage.contains("/start ")) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> linkHandler(message));
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

    private void linkHandler(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText().substring(7);
        String decodedText = new String(Base64.decode(text.getBytes()));
        String action = decodedText.split("=")[0];
        Integer id = Integer.parseInt(decodedText.split("=")[1]);
        Meeting meeting = new Meeting();
        meeting.setChat(chatId);
        String passphrase = meetingRepository.findPassphraseById(id);
        meeting.setPassphrase(passphrase);
        meeting.setName(message.getChat().getFirstName());
        if (listables.containsKey(action)) {
            ListableInterface listableInterface = listables.get(action);
            listableInterface.handleMeeting(meeting);
        } else if (actions.containsKey(action)) {
            ActionInterface actionInterface = actions.get(action);
            actionInterface.run(meeting);
        } else {
            messageService.sendMessageTo(meeting.getChat(), "Что-то пошло не так, попробуйте ещё раз.");
        }
    }
}
