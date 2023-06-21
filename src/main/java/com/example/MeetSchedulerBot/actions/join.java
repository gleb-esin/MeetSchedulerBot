package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class join extends AbstractAction implements ActionInterface {

    @Override
    public String getActionKey() {
        return "/join";
    }


    @Override
    public Answer setMeetingName(Answer answer) {
        String passphrase = answer.getMessage();
        if (meetingRepository.existsByPassphrase(passphrase)) {
            answer.getMeeting().setMonth(meetingRepository.findMonthByPassphrase(passphrase));
            answer.getMeeting().setPassphrase(passphrase);
            answer.setMessage("Найдена встреча <b>" + passphrase + "</b>\n" +
                    printMeeting(passphrase, answer.getMeeting().getUserLocalDate())
            );
            return answer;
        } else {
            answer.setMessage("Встреча не найдена, попробуйте ввести другое название");
            return answer;
        }
    }


    @Override
    public Answer setMonth(Answer answer) {
    return null;
    }


    @Override
    public Answer setDates(Answer answer) {
        String busyDates = answer.getMessage();
        answer.getMeeting().setDates(busyToAvailableConverter(busyDates, answer.getMeeting().getUserLocalDate()));
        answer.setMessage(calendarPrinter(busyToAvailableConverter(busyDates, answer.getMeeting().getUserLocalDate()), answer.getMeeting().getUserLocalDate()));
        return answer;    }


    @Override
    public Answer getResult(Answer answer) {
        meetingRepository.save(answer.getMeeting());
//        String passphrase = answer.getMeeting().getPassphrase();
//        answer.setMeeting(meetingRepository.findMeetingByPassphrase(passphrase));
        answer.setMessage("Вы присоединились к встрече <b>" + answer.getMeeting().getPassphrase() + "</b>: \n" +
                printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()));
        return answer;    }
}
