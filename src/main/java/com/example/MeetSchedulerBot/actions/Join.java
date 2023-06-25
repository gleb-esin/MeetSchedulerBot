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
        if (meetingRepository.existsByPassphrase(passphrase)) {
            if (meetingRepository.existsByChatAndPassphrase(answer.getMeeting().getChat(), passphrase)) {
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
        List<String> stringToParseArray = busyDatesParser(busyDates);
        if (stringToParseArray.isEmpty()) {
            answer.setMessage("Не распознал числа, повторите, пожалуйста ввод.");
            answer.setQuestion("Введите новые даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться в формате 1 3 7-15:\n" +
                    "(Если таких дат нет, введите 0)");
            answer.setState("getResult");
            return answer;
        } else {
            answer.getMeeting().setDates(busyToAvailableConverter(stringToParseArray, answer.getMeeting().getUserLocalDate()));
            answer.getMeeting().setExpired(LocalDate.of(
                    answer.getMeeting().getUserLocalDate().getYear(),
                    answer.getMeeting().getMonth(),
                    answer.getMeeting().getLastDay()));
            meetingRepository.save(answer.getMeeting());
            answer.setMessage("Вы присоединились к встрече <b>" + answer.getMeeting().getPassphrase() + "</b>: \n" +
                    printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()));
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню");
            answer.setState("notify");

            List<String> notifiedStr = meetingRepository.listOfNotified(answer.getMeeting().getPassphrase());
            for (int i = 0; i < notifiedStr.size(); i++) {
                answer.getMustBeNotified().add(Long.valueOf(notifiedStr.get(i)));
            }
            answer.getMustBeNotified().remove(answer.getMeeting().getChat());

            answer.setNotification("<b>" + answer.getMeeting().getName() + "</b> присоединился(-лась) ко встече " +
                    "<b>" + answer.getMeeting().getPassphrase() + "</b>.\n" +
                    printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()));
            return answer;
        }
    }
}
