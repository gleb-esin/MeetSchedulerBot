package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import com.example.MeetSchedulerBot.service.Meeting;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@Scope("prototype")
public class MyMeetings extends Action implements ActionInterface {

    @Override
    public String getActionKey() {
        return "/mymeetings";
    }

    @Override
    public Answer getResult(Answer answer) {
        Long chat = answer.getMeeting().getChat();
        if (meetingRepository.existsByChat(chat)){
            meetingRepository.deleteExpiredMeetings();
            StringBuilder meetings = new StringBuilder();
            meetings.append("Найдены следующие встречи:\n\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL", new Locale("ru"));
            List<Meeting> meetingList =  meetingRepository.findByChatOrderByExpiredAsc(chat);
            int n = 1;
            for(Meeting m: meetingList){
                meetings.append(n + ". ");
                meetings.append("<b>"+ m.getPassphrase()+ "</b>" + ", ");
                meetings.append(m.getUserLocalDate().format(formatter) + " ");
                meetings.append(m.getUserLocalDate().getYear());
                meetings.append("\n");
                n++;
            }
            answer.setMessage(meetings.toString());
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню.");
            answer.setState("finish");
            return answer;
        } else {
            answer.setMessage("Вы ни в одной встрече не состоите.");
            answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню.");
            answer.setState("finish");
            return answer;
        }
    }
}