package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;

public interface ActionInterface {
    String getActionKey();

    default Answer setMeetingName(Answer answer) {
        return null;
    }

    default Answer setMonth(Answer answer) {
        return null;
    }

    default Answer getResult(Answer answer) {
        return null;
    }

    default Answer setBusyDates(Answer answer) {
        return null;
    }
}
