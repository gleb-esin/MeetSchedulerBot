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
        String passphrase = answer.getMessage();
        if (meetingRepository.existsByPassphrase(passphrase)) {
            if (meetingRepository.existsByChatAndPassphrase(answer.getMeeting().getChat(),passphrase)){
                answer.getMeeting().setMonth(meetingRepository.findMonthByPassphrase(passphrase));
                answer.getMeeting().setPassphrase(passphrase);
                answer.setMessage("Найдена встреча <b>" + passphrase + "</b>\n" +
                        printMeeting(passphrase, answer.getMeeting().getUserLocalDate())
                );
                answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню");
                return answer;
            } else {
                answer.setMessage("Такая встреча с Вашим участием не найдена. Попробуйте ввести другое название.");
                answer.setState("Error");
                return answer;
            }
        } else {
            answer.setMessage("Встреча не найдена, попробуйте ввести другое название");
            answer.setState("Error");
            return answer;
        }
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
