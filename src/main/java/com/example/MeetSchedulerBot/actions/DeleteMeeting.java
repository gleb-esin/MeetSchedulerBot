package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class DeleteMeeting extends Action implements ActionInterface {

    @Override
    public String getActionKey() {
        return "/deletemeeting";
    }


    @Override
    public Answer setMeetingName(Answer answer) {
        String passphrase = answer.getMessage();
        meetingRepository.deleteExpiredMeetings();
        meetingRepository.deletePastDate();
        if (meetingRepository.existsByPassphrase(passphrase)) {
            if (meetingRepository.existsByChatAndPassphrase(answer.getMeeting().getChat(), passphrase) && meetingRepository.isUserOwner(answer.getMeeting().getChat(), passphrase)) {
                answer.getMeeting().setMonth(meetingRepository.findMonthByPassphrase(passphrase));
                answer.getMeeting().setPassphrase(passphrase);
                answer.setState("getResult");
                answer.setQuestion("Вы точно хоите удалить свою встречу?\nНапечатайте ответ для подтвеждения (<b>ДА</b>, <b>НЕТ</b>):");
                answer.setMessage("Найдена встреча <b>" + passphrase + "</b>\n" +
                        printMeeting(passphrase, answer.getMeeting().getUserLocalDate())
                );
                return answer;
            } else {
                answer.setMessage("Такая Ваша встреча не найдена. Попробуйте ввести другое название.");
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
    public Answer getResult(Answer answer) {
        if (answer.getMessage().equalsIgnoreCase("да")) {

            String notifiedStr = meetingRepository.listOfNotified(answer.getMeeting().getPassphrase());
            String[] notifiedArr = notifiedStr.split(" ");
            for (int i = 0; i < notifiedArr.length; i++) {
                answer.getMustBeNotified().add(Long.valueOf(notifiedArr[i]));
            }
            answer.getMustBeNotified().remove(answer.getMeeting().getChat());

            meetingRepository.deleteByPassphrase(answer.getMeeting().getPassphrase());
            answer.setState("notify");
            answer.setMessage("Вы удалили свою встречу <b>" + answer.getMeeting().getPassphrase() + "</b>. \n" +
                    "Но надо будет как-нибудь создать новую.");
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню.");
            answer.setNotification("<b>"+answer.getMeeting().getName() + "</b> удалил(-а) вашу встречу <b>" + answer.getMeeting().getPassphrase()+ "</b>.\n" +
                    "Но можно создать свою.");
            return answer;
        } else if (answer.getMessage().equalsIgnoreCase("нет")) {
            answer.setMessage("Удаление встречи отменено.");
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню.");
            answer.setState("finnish");
            return answer;

        } else {
            answer.setState("getResult");
            answer.setMessage("Ответ не распознан.\nНапишите <b>ДА</b> или <b>НЕТ</b>.");
            return answer;

        }
    }
}
