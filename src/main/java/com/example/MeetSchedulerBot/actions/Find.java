package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
@Component
@Scope("prototype")
public class Find extends AbstractAction implements ActionInterface {

    @Override
    public String getActionKey() {
        return "/find";
    }


    @Override
    public Answer setMeetingName(Answer answer) {
        return null;
    }


    @Override
    public Answer setMonth(Answer answer) {
        return null;
    }


    @Override
    public Answer setDates(Answer answer) {
        return null;
    }


    @Override
    public Answer getResult(Answer answer) {
        return null;
    }
}
