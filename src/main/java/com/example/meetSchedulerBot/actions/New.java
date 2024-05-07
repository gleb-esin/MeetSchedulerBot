package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class New extends Action implements ActionInterface {

    @Autowired
    public New(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Meeting meeting) {
        Long chatId = meeting.getChat();
        meeting.setOwner(true);
        setPassphrase(chatId, meeting);
        if (myMeetingIsNotExist(meeting.getPassphrase())) {
            messageService.sendMessageTo(chatId, "Встреча " + meeting.getPassphrase().split("-")[0] + " успешно создана!");
            setMonth(chatId, meeting);
            setNewDates(meeting);
            messageService.sendMessageTo(chatId, "Создана встреча: ");
            saveMeeting(meeting);
            sendLink(chatId, meeting);
        } else {
            messageService.sendMessageTo(chatId, "Встреча с таким названием уже существует!");
        }
        messageService.sendMessageTo(chatId, "Чтобы продолжить, выбери что-нибудь из меню.");
    }

    private void setPassphrase(Long chatId, Meeting meeting) {
        messageService.sendMessageTo(chatId, "Введите название встречи: ");
        String passphrase = messageService.receiveMessageFrom(chatId);
        meeting.setPassphrase(passphrase + "-" + meeting.getChat());
    }

    private void setMonth(Long chatId, Meeting meeting) {
        messageService.sendMessageTo(chatId, "Напечатайте название месяца на русском языке (январь, февраль):");
        String month = messageService.receiveMessageFrom(chatId);
        final List<String> months = Arrays.asList("январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь");
        if (!months.contains(month.toLowerCase())) {
            while (!months.contains(month.toLowerCase())) {
                messageService.sendMessageTo(chatId, "Месяц не распознан. Напечатайте название месяца на руссокм языке (январь, февраль):");
                month = messageService.receiveMessageFrom(chatId);
            }
        }
        meeting.setMonth(month);
        LocalDate userLocalDate = getUserLocalDate(meeting.getMonth());
        String wholeMonth = calendarPrinter(wholeMonth(userLocalDate), userLocalDate);
        messageService.sendMessageTo(chatId, wholeMonth);
    }

    private void sendLink(Long chatId, Meeting meeting) {
        messageService.sendMessageTo(chatId, "Чтобы пригласить кого-нибудь просто прешли им это сообщение:");
        String description = "Присоединяйся к моей встрече <b> " + meeting.getPassphrase().split("-")[0] + "</b>";
        String link = linkCreator(meeting, description, "join");
        messageService.sendMessageTo(chatId, "Привет, это <b>" + meeting.getName() + "</b>!\n" + link + "\nчерез @MeetSchedulerbot!");
    }

    private boolean myMeetingIsNotExist(String passphrase) {
        return !meetingRepository.existsByPassphrase(passphrase);
    }
}
