package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import com.example.MeetSchedulerBot.service.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class New extends Action implements ActionInterface {

    @Autowired
    MeetingRepository meetingRepository;

    @Override
    public Answer setMeetingName(Answer answer) {
        answer.getMeeting().setOwner(true);
        String passphrase = answer.getMessage();
        if (meetingRepository.existsByPassphrase(passphrase)) {
            var text = "Это название уже занято, попробуйте ввести другое название";
            answer.setMessage(text);
            answer.setState("Error");
            return answer;
        } else {
            answer.getMeeting().setPassphrase(passphrase);
            var text = "Встреча " + passphrase + " успешно создана!";
            answer.setQuestion("Введите название месяца");
            answer.setState("setMonth");
            answer.setMessage(text);
            return answer;
        }
    }

    public Answer setMonth(Answer answer) {
        String month = answer.getMessage();
        answer.getMeeting().setStringToMonth(month);
        answer.setMessage(calendarPrinter(wholeMonth(answer.getMeeting().getUserLocalDate()), answer.getMeeting().getUserLocalDate()));
        answer.setQuestion("Введите даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться:");
        answer.setState("getResult");
        return answer;
    }

    @Override
    public Answer getResult(Answer answer) {
        String busyDates = answer.getMessage();
        answer.getMeeting().setDates(busyToAvailableConverter(busyDates, answer.getMeeting().getUserLocalDate()));
        answer.setMessage(calendarPrinter(busyToAvailableConverter(busyDates, answer.getMeeting().getUserLocalDate()), answer.getMeeting().getUserLocalDate()));
        meetingRepository.save(answer.getMeeting());
        answer.setMessage("Создана встреча <b>" + answer.getMeeting().getPassphrase() + "</b>: " +
                "\n" +
                printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()) +
                "Чтобы пригласть кого-нибудь, просто перешли им название этой встречи.\n\n" +
                "Помни, что название  - ключ к вашей встрече. " +
                "Пересылай название этой встречи, только тем, кого хочешь пригласть на эту встречу:\n" +
                "<b>" + answer.getMeeting().getPassphrase() + "</b>");
        answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню");
        answer.setState("finnish");
        return answer;
    }


    @Override
    public String getActionKey() {
        return "/new";
    }
}
