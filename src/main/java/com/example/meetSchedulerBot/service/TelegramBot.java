package com.example.meetSchedulerBot.service;

import com.example.meetSchedulerBot.config.BotConfig;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TelegramBot extends TelegramWebhookBot {
    BotConfig config;
    MessageHandler messageHandler;

    @Autowired
    public TelegramBot(BotConfig config, MessageHandler messageHandler) {
        super(config.getBotToken());
        this.config = config;
        this.messageHandler = messageHandler;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void registerMenu() {
        List<BotCommand> menu = new ArrayList<>();
        menu.add(new BotCommand("/new", "Создание новой встечи"));
        menu.add(new BotCommand("/mymeetings", "Найти все свои встречи"));
        menu.add(new BotCommand("/edit", "Редактировать даты"));
        menu.add(new BotCommand("/removeme", "Удалить свое участие"));
        menu.add(new BotCommand("/deletemeeting", "Удалить свою встречу"));
        menu.add(new BotCommand("/feedback", "Оставить отзыв, предложение или замечение"));
        try {
            execute(new SetMyCommands(menu, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list" + e.getMessage());
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() || update.hasCallbackQuery()) {
            messageHandler.handle(update);
        }
        return null;
    }

    @Override
    public String getBotPath() {
        return config.getWebHookPath();
    }

    @Override
    public String getBotUsername() {
        return config.getUserName();
    }


    @Override
    public void onRegister() {}

    /**
     * A listener method for handling outgoing messages to the server.
     *
     * @param sendMessage the event to be handled, expects a SendMessage object
     */
    @EventListener(SendMessage.class)
    private void messageSenderOnEventListener(SendMessage sendMessage) {
        try {
            sendMessage.enableHtml(true);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}