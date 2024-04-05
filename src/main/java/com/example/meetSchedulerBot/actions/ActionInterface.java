package com.example.meetSchedulerBot.actions;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface ActionInterface {
    void run(Message message);
}
