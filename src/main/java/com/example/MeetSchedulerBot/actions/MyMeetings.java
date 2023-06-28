package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class MyMeetings extends Action implements ActionInterface {

    @Override
    public String getActionKey() {
        return "/mymeetings";
    }

    @Override
    public Answer getResult(Answer answer) {
        Long chat = answer.getMeeting().getChat();
        if (meetingRepository.existsByChat(chat)){
            answer.setMessage(meetingRepository.findByChat(chat).toString());
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню.");
            answer.setState("finish");
            return answer;
        } else {
            answer.setMessage("Вы ни в одной встрече не состоите.");
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню.");
            answer.setState("finish");
            return answer;
        }
    }
}