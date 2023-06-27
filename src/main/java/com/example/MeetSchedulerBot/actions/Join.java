package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

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
        Long chat = answer.getMeeting().getChat();
        answer.getMeeting().setMonth(meetingRepository.findMonthByPassphrase(passphrase));
        LocalDate usersLocalDate = answer.getMeeting().getUserLocalDate();
        if (meetingRepository.existsByPassphrase(passphrase)) {
            if (meetingRepository.existsByChatAndPassphrase(chat, passphrase)) {
                answer.setMessage("Вы уже состоите в этой встрече. Чтобы редактировать даты, выберите соответствующий пункт в меню.");
                answer.setState("Error");
                return answer;
            } else {
                answer.getMeeting().setMonth(meetingRepository.findMonthByPassphrase(passphrase));
                answer.getMeeting().setPassphrase(passphrase);
                answer.setQuestion("Введите даты в которые Вы НЕ МОЖЕТЕ встретиться:");
                answer.setState("getResult");
                answer.setMessage("Найдена встреча <b>" + passphrase + "</b>\n" +
                        calendarPrinter(wholeMonth(usersLocalDate), usersLocalDate)
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
        LocalDate userLocalDate = answer.getMeeting().getUserLocalDate();
        String busyDates = answer.getMessage();
        List<Integer> busyDatesList = datesParser(busyDates, userLocalDate);
        List<Integer> availabeDatesList = busyToAvailableConverter(busyDatesList, userLocalDate);
        if (availabeDatesList.isEmpty()) {
            answer.setMessage("Не распознал числа, повторите, пожалуйста ввод.");
            answer.setQuestion("Введите даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться:");
            answer.setState("getResult");
            return answer;
        } else {
            answer.getMeeting().setDates(availabeDatesList);
            answer.setMessage(calendarPrinter(availabeDatesList, userLocalDate));

            meetingRepository.deleteExpiredMeetings();
            meetingRepository.save(answer.getMeeting());
            answer.setMessage("Вы присоединились к встрече <b>" + answer.getMeeting().getPassphrase() + "</b>: \n" +
                    printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()));
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню");
            answer.setState("notify");
            String notifiedStr = meetingRepository.listOfNotified(answer.getMeeting().getPassphrase());
            String[] notifiedArr = notifiedStr.split(" ");
            for (int i = 0; i < notifiedArr.length; i++) {
                answer.getMustBeNotified().add(Long.valueOf(notifiedArr[i]));
            }
            answer.getMustBeNotified().remove(answer.getMeeting().getChat());

            answer.setNotification("<b>" + answer.getMeeting().getName() + "</b> присоединился(-лась) ко встече " +
                    "<b>" + answer.getMeeting().getPassphrase() + "</b>.\n" +
                    printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()));
            return answer;
        }
    }
}
