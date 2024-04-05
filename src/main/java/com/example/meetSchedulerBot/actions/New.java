package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

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
    public void run(Message message) {
        Meeting meeting = new Meeting();
        Long chatId = message.getChatId();
        setCredentials(message, meeting);
        setPassphrase(chatId, meeting);
        setMonth(chatId, meeting);
        setNewDates(meeting);
        messageService.sendMessageTo(chatId, "Создана встреча: ");
        saveMeeting(meeting);
        sendLink(chatId, meeting);
        messageService.sendMessageTo(chatId, "Чтобы продолжить, выбери что-нибудь из меню.");
    }

    @Override
    protected void setCredentials(Message message, Meeting meeting) {
        meeting.setChat(message.getChatId());
        meeting.setName(message.getChat().getFirstName());
        meeting.setOwner(true);
    }

    private void setPassphrase(Long chatId, Meeting meeting) {
        messageService.sendMessageTo(chatId, "Введите название встречи: ");
        String passphrase = messageService.receiveMessageFrom(chatId);
        if (meetingRepository.existsByPassphrase(passphrase)) {
            while (meetingRepository.existsByPassphrase(passphrase)) {
                messageService.sendMessageTo(chatId, "Это название уже занято, попробуйте ввести другое название");
                passphrase = messageService.receiveMessageFrom(chatId);
            }
        } else {
            messageService.sendMessageTo(chatId, "Встреча " + passphrase + " успешно создана!");
        }
        meeting.setPassphrase(passphrase);
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
        String description = "Присоединяйся к моей встрече";
        String link = linkCreator(meeting, description);
        messageService.sendMessageTo(chatId, "Привет, это <b>" + meeting.getName() + "</b>! " + link+ " через @MeetSchedulerbot!");
    }
}
