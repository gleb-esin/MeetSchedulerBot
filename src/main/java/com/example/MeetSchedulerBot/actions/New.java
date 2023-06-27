package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import com.example.MeetSchedulerBot.service.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

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
        final List<String> months = Arrays.asList("январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь");
        String month = answer.getMessage();
        if (months.contains(month.toLowerCase())) {
            answer.getMeeting().setStringToMonth(month);
            answer.setMessage(calendarPrinter(wholeMonth(answer.getMeeting().getUserLocalDate()), answer.getMeeting().getUserLocalDate()));
            answer.setQuestion("Введите новые даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться в формате 1 3 7-15:\n" +
                    "(Если таких дат нет, введите 0)");
            answer.setState("getResult");
            return answer;
        } else {
            answer.setMessage("Месяц не распознан. Напечатайте название месяца на руссокм языке (январь, февраль):");
            answer.setState("Error");
            return answer;
        }
    }

    @Override
    public Answer getResult(Answer answer) {
        LocalDate userLocaLDate = answer.getMeeting().getUserLocalDate();
        String busyDates = answer.getMessage();
        List<Integer> busyDatesList = datesParser(busyDates, userLocaLDate);
        List<Integer> availabeDatesList = busyToAvailableConverter(busyDatesList, userLocaLDate);
        if (availabeDatesList.isEmpty()) {
            answer.setMessage("Не распознал числа, повторите, пожалуйста ввод.");
            answer.setQuestion("Введите даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться:");
            answer.setState("getResult");
            return answer;
        } else {
            answer.getMeeting().setDates(availabeDatesList);
            answer.setMessage(calendarPrinter(availabeDatesList, userLocaLDate));
            meetingRepository.deleteExpiredMeetings();
            meetingRepository.save(answer.getMeeting());
            answer.setMessage("Создана встреча <b>" + answer.getMeeting().getPassphrase() + "</b>: " +
                    "\n" +
                    printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()) +
                    "Чтобы пригласть кого-нибудь, просто перешли им название этой встречи.\n\n" +
                    "Помни, что название  - ключ к вашей встрече. " +
                    "Пересылай название этой встречи, только тем, кого хочешь пригласть на эту встречу.\n" +
                    "Чтобы пригласить кого-нибудь просто прешли им это сообщение:");
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню");
            answer.setState("notify");
            answer.setNotification("Привет, это <b>" + answer.getMeeting().getName() + "</b>! Присоединяйся к моей встрече " +
                    "<b>" + answer.getMeeting().getPassphrase() + "</b>" +
                    " через @MeetSchedulerbot!");
            answer.getMustBeNotified().add(answer.getMeeting().getChat());
            return answer;
        }
    }


    @Override
    public String getActionKey() {
        return "/new";
    }
}
