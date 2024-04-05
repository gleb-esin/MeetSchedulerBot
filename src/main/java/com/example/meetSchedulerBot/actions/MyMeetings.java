package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component("mymeetings")
public class MyMeetings extends Action implements ActionInterface {
    public MyMeetings(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Message message) {
        if (ifMeetingIsFound(message.getChatId())) {
            StringBuilder meetings = new StringBuilder();
            meetings.append("Найдены следующие встречи:\n\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL", new Locale("ru"));
            List<Meeting> meetingList = meetingRepository.findByChatOrderByExpiredAsc(message.getChatId());
            int n = 1;
            for (Meeting m : meetingList) {
                meetings.append(n).append(". ");
                meetings.append("<b>").append(linkCreator(m, m.getPassphrase())).append("</b>").append(", ");
                meetings.append(getUserLocalDate(m.getMonth()).format(formatter)).append(" ");
                meetings.append(getUserLocalDate(m.getMonth()).getYear());
                meetings.append("\n");
                n++;
            }
            messageService.sendMessageTo(message.getChatId(), meetings.toString());
        }
        messageService.sendMessageTo(message.getChatId(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }

    protected boolean ifMeetingIsFound(Long chat) {
        boolean userIsParticipant = meetingRepository.existsByChat(chat);
        if (!userIsParticipant) {
            messageService.sendMessageTo(chat, "Вы не состоите ни в одной встрече");
            return false;
        }
        return true;
    }
}
//
//    @Override
//    public String getActionKey() {
//        return "/mymeetings";
//    }
//
//    @Override
//    public Answer getResult(Answer answer) {
//        Long chat = answer.getMeeting().getChat();
//        if (meetingRepository.existsByChat(chat)){
//            meetingRepository.deleteExpiredMeetings();
//            meetingRepository.deletePastDate();
//            StringBuilder meetings = new StringBuilder();
//            meetings.append("Найдены следующие встречи:\n\n");
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL", new Locale("ru"));
//            List<Meeting> meetingList =  meetingRepository.findByChatOrderByExpiredAsc(chat);
//            int n = 1;
//            for(Meeting m: meetingList){
//                meetings.append(n + ". ");
//                meetings.append("<b>"+ m.getPassphrase()+ "</b>" + ", ");
//                meetings.append(m.getUserLocalDate().format(formatter) + " ");
//                meetings.append(m.getUserLocalDate().getYear());
//                meetings.append("\n");
//                n++;
//            }
//            answer.setMessage(meetings.toString());
//            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню.");
//            answer.setState("finish");
//            return answer;
//        } else {
//            answer.setMessage("Вы ни в одной встрече не состоите.");
//            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню.");
//            answer.setState("finish");
//            return answer;
//        }
//    }
//}