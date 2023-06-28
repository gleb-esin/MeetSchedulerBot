package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Feedback implements ActionInterface {
    @Value("${feedbackChatID}")
    private Long feedbackChatID;

    @Override
    public String getActionKey() {
        return "/feedback";
    }

    @Override
    public Answer getResult(Answer answer) {
        answer.setNotification("Feedback from <b>" +
                answer.getMeeting().getName() + " (" +
                answer.getMeeting().getChat() + ")"
                +
                "</b>:\n" +
                answer.getMessage());
        answer.setState("notify");
        answer.getMustBeNotified().add(feedbackChatID);
/**DEBUG*/System.out.println(" answer.getMustBeNotified() " + answer.getMustBeNotified());
        answer.setMessage("Спасибо за Ваш отзыв!");
        answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню");
        return answer;
    }
}
