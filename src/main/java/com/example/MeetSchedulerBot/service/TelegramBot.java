package com.example.MeetSchedulerBot.service;

import com.example.MeetSchedulerBot.actions.ActionInterface;
import com.example.MeetSchedulerBot.actions.newMeeting;
import com.example.MeetSchedulerBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private Map<Long, Map<String, ActionInterface>> bindingBy = new ConcurrentHashMap<>();
    @Autowired
    private List<ActionInterface> actionBeans = new ArrayList<>(Arrays.asList(new newMeeting()));
    private final List<String> actions = Arrays.asList("/new");
    private String state;
    private final BotConfig config;
    @Autowired
    Answer answer;
    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    Meeting meeting;


    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> menu = new ArrayList<>();
        menu.add(new BotCommand("/new", "Создание новой встечи"));
        menu.add(new BotCommand("/join", "Присоединисться к уже существующей встрече"));
        menu.add(new BotCommand("/edit", "Редактировать уже существующую встречу"));
        menu.add(new BotCommand("/delete", "Удалить уже существующую встречу"));

        try {
            execute(new SetMyCommands(menu, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list" + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String usersMessage = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String question = "";
//            Meeting meeting = new Meeting();
//            Answer answer = new Answer();

            if (update.getMessage().getText().equals("/start")) {
                question = startAction(update);
                send(chatId, question);

                //if user's usersMessage contains some menu command
            } else if (actions.contains(usersMessage)) {
                //set chatId and name
                meeting.setId(chatId);
                meeting.setName(update.getMessage().getChat().getFirstName());
                setState("setMeetingName");
                send(chatId, "Введите название встречи:");
                setState("setMeetingName");
                //save user state and action
                setBindingBy(chatId, state, getAction(usersMessage));
                //if user already send some command
            } else if (bindingBy.containsKey(chatId)) {
                //update
                if (bindingBy.get(chatId).containsKey("setMeetingName")) {
                    answer.setMeeting(meeting);
                    answer.setMessage(usersMessage);
                    answer = bindingBy.get(chatId).get("setMeetingName").setMeetingName(answer);
                    if (answer.getMessage().equals("Это название уже занято, попробуйте ввести другое название")) {
                        send(chatId, answer.getMessage());
                        setState("setMeetingName");
                    } else {
                        send(chatId, answer.getMessage());
                        if (bindingBy.get(chatId).get("setMeetingName") instanceof newMeeting) {
                            question = "Введите название месяца";
                            send(chatId, question);
                            setState("setMonth");
                            setBindingBy(chatId, state, bindingBy.get(chatId).get("setMeetingName"));
                        } else {
                            question = "Введите даты в которые Вы НЕ МОЖЕТЕ встретиться:";
                            send(chatId, question);
                            setState("setDates");
                            setBindingBy(chatId, state, bindingBy.get(chatId).get("setMeetingName"));
                        }
                    }
                } else if (bindingBy.get(chatId).containsKey("setMonth")) {
                    answer.setMessage(usersMessage);
                    answer.setMeeting(meeting);
                    answer = bindingBy.get(chatId).get("setMonth").setMonth(answer);
                    send(chatId, answer.getMessage());
                    question = "Введите даты в которые Вы НЕ МОЖЕТЕ встретиться:";
                    send(chatId, question);
                    setState("setDates");
                    setBindingBy(chatId, state, bindingBy.get(chatId).get("setMonth"));
                } else if (bindingBy.get(chatId).containsKey("setDates")) {
                    answer.setMessage(usersMessage);
                    answer = bindingBy.get(chatId).get("setDates").setDates(answer);
                    answer = bindingBy.get(chatId).get("setDates").getResult(answer);
                    send(chatId, answer.getMessage());
                    send(chatId, "Чтобы продолжить, выбери что-нибудь из меню");
                }
            }
        }
    }

    public String startAction(Update update) {
        Message msg = update.getMessage();
        String chatId = msg.getChatId().toString();
        String text = "Помогу выбрать даты для встречи. Выбери что-нибудь из меню";
        return text;
    }

    private void setBindingBy(Long chatID, String state, ActionInterface action) {
        Map<String, ActionInterface> actionState = new HashMap<>();
        actionState.put(state, action);
        this.bindingBy.put(chatID, actionState);
    }


    public void send(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        message.setParseMode(ParseMode.HTML);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {

        return config.getToken();
    }

    private ActionInterface getAction(String key) {
        for (var a : actionBeans) {
            if (a.getActionKey().equals(key)) {
                return a;
            }
        }
        return null;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {

        this.state = state;
    }
}