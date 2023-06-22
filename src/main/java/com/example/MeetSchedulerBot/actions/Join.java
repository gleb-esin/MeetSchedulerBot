package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Join extends Action implements ActionInterface {

    @Override
    public String getActionKey() {
        return "/join";
    }


    @Override
    public Answer setMeetingName(Answer answer) {
        String passphrase = answer.getMessage();
        if (meetingRepository.existsByPassphrase(passphrase)) {
            if (meetingRepository.existsByChatAndPassphrase(answer.getMeeting().getChat(),passphrase)){
                answer.setMessage("Вы уже состоите в этой встрече. Чтобы редактировать даты, выберите соответствующий пункт в меню.");
                answer.setState("Error");
                return answer;
            } else {
                answer.getMeeting().setMonth(meetingRepository.findMonthByPassphrase(passphrase));
                answer.getMeeting().setPassphrase(passphrase);
                answer.setQuestion("Введите даты в которые Вы НЕ МОЖЕТЕ встретиться:");
                answer.setState("getResult");
                answer.setMessage("Найдена встреча <b>" + passphrase + "</b>\n" +
                        printMeeting(passphrase, answer.getMeeting().getUserLocalDate())
                );
                return answer;
            }
        } else {
            answer.setMessage("Встреча не найдена, попробуйте ввести другое название");
            answer.setState("Error");
            return answer;
        }
    }


    @Override
    public Answer getResult(Answer answer) {
        String busyDates = answer.getMessage();
        answer.getMeeting().setDates(busyToAvailableConverter(busyDates, answer.getMeeting().getUserLocalDate()));

        meetingRepository.save(answer.getMeeting());
        answer.setState("finnish");
        answer.setMessage("Вы присоединились к встрече <b>" + answer.getMeeting().getPassphrase() + "</b>: \n" +
                printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()));
        answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню");
        answer.setState("finnish");
        return answer;    }
}
