package com.example.MeetSchedulerBot.service;

import com.example.MeetSchedulerBot.actions.ActionInterface;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@ToString
@Getter
@Setter
public class Answer {
    Meeting meeting;
    String message;
    String state;
    ActionInterface action;
    String question;
    String debug;
    List<Long> mustBeNotified = new ArrayList<>();
}
