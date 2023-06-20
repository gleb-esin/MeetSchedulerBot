package com.example.MeetSchedulerBot.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class Answer {
    Meeting meeting;
    String message;
}
