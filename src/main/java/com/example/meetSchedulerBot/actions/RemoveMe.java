package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;
import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;

@Component("removeme")
public class RemoveMe extends Action implements ActionInterface, ListableInterface {
    public RemoveMe(MessageService messageService, MeetingRepository meetingRepository) {
        super(messageService, meetingRepository);
    }

    @Override
    public void run(Meeting meeting) {
        String meetings;
        if (meetingRepository.existsByChat(meeting.getChat())) {
            meetings = getMeetingsList(meeting, "removeme");
            messageService.sendMessageTo(meeting.getChat(), meetings);
        } else {
            messageService.sendMessageTo(meeting.getChat(), "Вы пока не cостоите ни в одной встрече!");
            messageService.sendMessageTo(meeting.getChat(), "Чтобы продолжить, выбери что-нибудь из меню.");
        }
    }

    private void removeMe(Meeting meeting) {
        String[] buttonStr = {"Да-yes", "Нет-no"};
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkupBuilder(buttonStr);
        messageService.sendInlineKeyboardTo(meeting.getChat(), "Вы точно хоите удалить свое участие в этой встрече?", inlineKeyboardMarkup);
        String answer = messageService.receiveMessageFrom(meeting.getChat());
        if (answer.equals("yes")) {
            Long nextOwner = meetingRepository.whoWillBeNextOwner(meeting.getPassphrase());
            meetingRepository.setNextOwner(nextOwner, meeting.getPassphrase());
            meetingRepository.deleteByChatAndPassphrase(meeting.getChat(), meeting.getPassphrase());
            messageService.sendMessageTo(meeting.getChat(), "Вы больше не участвуете во встрече " + meeting.getPassphrase().split("-")[0]);
        }
    }

    @Override
    public void handleMeeting(Meeting meeting) {
        setDateCredentials(meeting);
        meetingRepository.deleteExpiredMeetings();
        meetingRepository.deletePastDate();
        messageService.sendMessageTo(meeting.getChat(), "Найдена встреча:");
        LocalDate meetingDate = getUserLocalDate(meeting.getMonth());
        messageService.sendMessageTo(meeting.getChat(), meetingToStr(meeting.getPassphrase(), meetingDate));
        removeMe(meeting);
        notifyParticipants(meeting, getUserLocalDate(meeting.getMonth()), "<b>" + meeting.getName() + "</b> не будет участвовать во встече:");
        messageService.sendMessageTo(meeting.getChat(), "Чтобы продолжить, выбери что-нибудь из меню.");
    }
}