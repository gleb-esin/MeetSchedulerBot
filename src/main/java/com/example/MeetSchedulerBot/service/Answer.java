package com.example.MeetSchedulerBot.service;

import com.example.MeetSchedulerBot.actions.ActionInterface;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@ToString
@Getter
@Setter
public class Answer {
    private Meeting meeting;
    private String message;
    private String state;
    private ActionInterface action;
    private String question;
    private String notification;
    private List<Long> mustBeNotified = new ArrayList<>();
    private InlineKeyboardMarkup inlineButtons;
}
