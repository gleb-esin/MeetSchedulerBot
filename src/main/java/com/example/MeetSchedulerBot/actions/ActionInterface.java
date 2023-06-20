package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;

public interface ActionInterface {
    String getActionKey();
    Answer setMeetingName(Answer answer);
    Answer setMonth(Answer answer);
    Answer setDates(Answer answer);
    Answer getResult(Answer answer);

}
