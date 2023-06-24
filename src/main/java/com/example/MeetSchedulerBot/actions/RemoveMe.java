package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
        String passphrase = answer.getMessage();
        if (meetingRepository.existsByPassphrase(passphrase)) {
            if (meetingRepository.existsByChatAndPassphrase(answer.getMeeting().getChat(), passphrase)) {
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
        if (answer.getMessage().equalsIgnoreCase("да")) {
            if (meetingRepository.checkPassphraseAndOwner(answer.getMeeting().getPassphrase())) {
                answer.setState("notify");
                List<String> notifiedStr = meetingRepository.listOfNotified(answer.getMeeting().getPassphrase());
                for(int i = 0; i < notifiedStr.size(); i++){
                    answer.getMustBeNotified().add(Long.valueOf(notifiedStr.get(i)));
                }
                answer.getMustBeNotified().remove(answer.getMeeting().getChat());
                Long nextOwner = meetingRepository.whoWillBeNextOwner(answer.getMeeting().getPassphrase());
                meetingRepository.setNextOwner(nextOwner, answer.getMeeting().getPassphrase());
                meetingRepository.deleteByChatAndPassphrase(answer.getMeeting().getChat(), answer.getMeeting().getPassphrase());

                answer.setNotification("<b>"+answer.getMeeting().getName() + "</b> не захотел(-а) участвовать в вашей встрече <b>" + answer.getMeeting().getPassphrase()+ "</b>:\n" +
                        printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate())+
                        "Но можно пригласить кого-нибудь еще.");
                answer.setMessage("Вы удалили свое участие во встрече <b>" + answer.getMeeting().getPassphrase() + "</b>: \n" +
                        printMeeting(answer.getMeeting().getPassphrase(), answer.getMeeting().getUserLocalDate()));
            }else{
                answer.setMessage("Вы удалили свою встречу <b>" + answer.getMeeting().getPassphrase() + "</b>. \n" +
                        "Но надо будет как-нибудь создать новую.");
            }
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню");

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
