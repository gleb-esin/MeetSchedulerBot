package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.MeetingRepository;
import com.example.meetSchedulerBot.service.MessageService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MeetingID extends Action implements ActionInterface {
    Find find;
    Join join;

    @Autowired
    public MeetingID(MessageService messageService, MeetingRepository meetingRepository, Find find, Join join) {
        super(messageService, meetingRepository);
        this.find = find;
        this.join = join;
    }

    @Override
    public void run(Message message) {
        Long chatId = message.getChatId();
        if (isUserParticipant(chatId)) {
            find.run(message);
        } else {
            join.run(message);
        }
    }

    private boolean isUserParticipant(Long chatId) {
        return meetingRepository.existsByChat(chatId);
    }
}
