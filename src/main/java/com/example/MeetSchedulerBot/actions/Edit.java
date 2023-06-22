package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Edit extends Action implements ActionInterface {

    @Override
    public String getActionKey() {
        return "/edit";
    }


    @Override
    public Answer setMeetingName(Answer answer) {
        String passphrase = answer.getMessage();
        if (meetingRepository.existsByPassphrase(passphrase)) {
            if (meetingRepository.existsByChatAndPassphrase(answer.getMeeting().getChat(), passphrase)) {
                answer.getMeeting().setMonth(meetingRepository.findMonthByPassphrase(passphrase));
                answer.getMeeting().setPassphrase(passphrase);
                answer.setState("getResult");
                answer.setQuestion("Введите новые даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться:");
                answer.setMessage("Найдена встреча <b>" + passphrase + "</b>\n" +
                        printMeeting(passphrase, answer.getMeeting().getUserLocalDate())
                );
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

        return answer;
    }


    @Override
    public Answer getResult(Answer answer) {
        String busyDates = answer.getMessage();
        answer.getMeeting().setDates(busyToAvailableConverter(busyDates, answer.getMeeting().getUserLocalDate()));
        boolean isUserOwner = meetingRepository.isUserOwner(answer.getMeeting().getChat(), answer.getMeeting().getPassphrase());
        if (isUserOwner) answer.getMeeting().setOwner(true);
        meetingRepository.deleteByChatAndPassphrase(answer.getMeeting().getChat(), answer.getMeeting().getPassphrase());
        meetingRepository.save(answer.getMeeting());
        answer.setMessage("Вы отредактировали даты своего участия во встрече <b>" + answer.getMeeting().getPassphrase() + "</b>: \n" +
                printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()));
        answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню");

        return answer;
    }
}