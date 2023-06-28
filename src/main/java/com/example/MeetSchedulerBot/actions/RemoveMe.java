package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Scope("prototype")
public class RemoveMe extends Action implements ActionInterface {

    @Override
    public String getActionKey() {
        return "/removeme";
    }


    @Override
    public Answer setMeetingName(Answer answer) {
        meetingRepository.deleteExpiredMeetings();
        String passphrase = answer.getMessage();
        Long chat = answer.getMeeting().getChat();
        if (meetingRepository.existsByPassphrase(passphrase)) {
            if (meetingRepository.existsByChatAndPassphrase(chat, passphrase)) {
                answer.getMeeting().setMonth(meetingRepository.findMonthByPassphrase(passphrase));
                answer.getMeeting().setPassphrase(passphrase);
                answer.setState("getResult");
                answer.setQuestion("Вы точно хоите удалить свое участие в этой встрече?\nНапечатайте ответ для подтвеждения (<b>ДА</b>, <b>НЕТ</b>):");
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
    public Answer getResult(Answer answer) {
        String passphrase = answer.getMeeting().getPassphrase();
        Long chat = answer.getMeeting().getChat();
        LocalDate userLocalDate = answer.getMeeting().getUserLocalDate();
        if (answer.getMessage().equalsIgnoreCase("да")) {
            if (meetingRepository.checkPassphraseAndOwner(passphrase)) {
                answer.setState("notify");
                String notifiedStr = meetingRepository.listOfNotified(passphrase);
                String[] notifiedArr = notifiedStr.split(" ");
                for (int i = 0; i < notifiedArr.length; i++) {
                    answer.getMustBeNotified().add(Long.valueOf(notifiedArr[i]));
                }
                answer.getMustBeNotified().remove(chat);
                Long nextOwner = meetingRepository.whoWillBeNextOwner(passphrase);
                meetingRepository.setNextOwner(nextOwner, passphrase);
                meetingRepository.deleteByChatAndPassphrase(chat, passphrase);

                answer.setNotification("<b>"+answer.getMeeting().getName() + "</b> не захотел(-а) участвовать в вашей встрече <b>" + passphrase+ "</b>:\n" +
                        printMeeting(passphrase, userLocalDate)+
                        "Но можно пригласить кого-нибудь еще.");
                answer.setMessage("Вы удалили свое участие во встрече <b>" + passphrase + "</b>: \n" +
                        printMeeting(passphrase, userLocalDate));
            }else{
                meetingRepository.deleteByChatAndPassphrase(chat, passphrase);
                answer.setMessage("Вы удалили свою встречу <b>" + passphrase + "</b>. \n" +
                        "Но надо будет как-нибудь создать новую.");
            }
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню.");

            return answer;
        } else if (answer.getMessage().equalsIgnoreCase("нет")) {
            answer.setMessage("Удаление участия отменено.");
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
