package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import com.example.MeetSchedulerBot.service.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class newMeeting extends AbstractAction implements ActionInterface {

    @Autowired
    MeetingRepository meetingRepository;
//    @Autowired
//    Answer answer;
//    @Autowired
//    Meeting meeting;

    @Override
    public Answer setMeetingName(Answer answer) {
        answer.getMeeting().setOwner(true);
        String passphrase = answer.getMessage();
        if (meetingRepository.existsByPassphrase(passphrase)) {
            var text = "Это название уже занято, попробуйте ввести другое название";
            answer.setMessage(text);
            return answer;
        } else {
            answer.getMeeting().setPassphrase(passphrase);
            var text = "Встреча " + passphrase + " успешно создана!";
            answer.setMessage(text);
            return answer;
        }
    }

    public Answer setMonth(Answer answer) {
        String month = answer.getMessage();
        answer.getMeeting().setStringToMonth(month);
        answer.setMessage(printer(wholeMonth(answer.getMeeting().getUserLocalDate()), answer.getMeeting().getUserLocalDate()));
        return answer;
    }


    @Override
    public Answer setDates(Answer answer) {
        String busyDates = answer.getMessage();
        busyToAvailableConverter(busyDates, answer.getMeeting().getUserLocalDate());
        answer.getMeeting().setDates(busyToAvailableConverter(busyDates, answer.getMeeting().getUserLocalDate()));
        answer.setMessage(printer(busyToAvailableConverter(busyDates, answer.getMeeting().getUserLocalDate()), answer.getMeeting().getUserLocalDate()));
        return answer;
    }

    @Override
    public Answer getResult(Answer answer) {
        meetingRepository.save(answer.getMeeting());
        String passphrase = answer.getMeeting().getPassphrase();
        answer.setMeeting(meetingRepository.findMeetingByPassphrase(passphrase));
        answer.setMessage("Создана встреча <b>" + answer.getMeeting().getPassphrase() + "</b>: " +
                "\n" +
                printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()) +
                "Чтобы пригласть кого-нибудь, просто перешли им название этой встречи.\n\n" +
                "Помни, что название  - ключ к вашей встрече. " +
                "Пересылай название этой встречи, только тем, кого хочешь пригласть на эту встречу:\n" +
                "<b>" + answer.getMeeting().getPassphrase() + "</b>"
        );
        return answer;
    }


    @Override
    public String getActionKey() {
        return "/new";
    }
}
