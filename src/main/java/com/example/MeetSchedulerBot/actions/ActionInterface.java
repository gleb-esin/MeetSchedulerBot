package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;

public interface ActionInterface {
    String getActionKey();
    Answer setMeetingName(Answer answer);

    default Answer setMonth(Answer answer) {
        return null;
    }

    default Answer getResult(Answer answer) {
        return null;
    }

}
