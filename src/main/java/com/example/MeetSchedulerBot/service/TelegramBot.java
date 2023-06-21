package com.example.MeetSchedulerBot.service;

import com.example.MeetSchedulerBot.actions.ActionInterface;
import com.example.MeetSchedulerBot.actions.Find;
import com.example.MeetSchedulerBot.actions.Join;
import com.example.MeetSchedulerBot.actions.NewMeeting;
import com.example.MeetSchedulerBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private Map<Long, Answer> bindingBy = new ConcurrentHashMap<>();
    @Autowired
    private List<ActionInterface> actionBeans = new ArrayList<>(Arrays.asList(new NewMeeting(), new Join(), new Find()));
    private final List<String> actions = Arrays.asList("/new", "/join", "/find");
    private String state;
    private final BotConfig config;

    @Autowired
    MeetingRepository meetingRepository;


    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> menu = new ArrayList<>();
        menu.add(new BotCommand("/new", "Создание новой встечи"));
        menu.add(new BotCommand("/join", "Присоединисться к уже существующей встрече"));
        menu.add(new BotCommand("/find", "Найти встречу"));
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


            if (update.getMessage().getText().equals("/start")) {
                send(chatId, "Помогу выбрать даты для встречи. Выбери что-нибудь из меню");

                //if user's usersMessage contains some menu command
            } else if (actions.contains(usersMessage)) {
                //set chatId and name
                Meeting meeting = new Meeting();
                Answer answer = new Answer();

                meeting.setChatId(chatId);
                meeting.setName(update.getMessage().getChat().getFirstName());
                answer.setMeeting(meeting);
                answer.setState("setMeetingName");
                answer.setAction(getAction(usersMessage));
                send(chatId, "Введите название встречи:");
                //save user state and action
                setBindingBy(chatId, answer);
                //if user already send some command
            } else if (bindingBy.containsKey(chatId)) {
                //update
                if (bindingBy.get(chatId).getState().equals("setMeetingName")) {
                    var answer = bindingBy.get(chatId);
                    answer.setMessage(usersMessage);
                    answer = answer.getAction().setMeetingName(answer);
                    if (answer.getState().equals("Error")) {
                        send(chatId, answer.getMessage());
                        answer.setState("setMeetingName");
                    } else {
                        send(chatId, answer.getMessage());
                        if (bindingBy.get(chatId).getAction() instanceof NewMeeting) {
                            send(chatId, "Введите название месяца");
                            answer.setState("setMonth");
                            setBindingBy(chatId, answer);
                        } else if (!(bindingBy.get(chatId).getAction() instanceof Find)) {
                            send(chatId, "Введите даты в которые Вы НЕ МОЖЕТЕ встретиться:");
                            answer.setState("setDates");
                            setBindingBy(chatId, answer);
                        }else if ((bindingBy.get(chatId).getAction() instanceof Find)){
                            bindingBy.remove(chatId);
                            send(chatId, "Чтобы продолжить, выбери что-нибудь из меню");
                        }
                    }
                } else if (bindingBy.get(chatId).getState().equals("setMonth")) {
                    var answer = bindingBy.get(chatId);
                    answer.setMessage(usersMessage);
                    answer = answer.getAction().setMonth(answer);
                    send(chatId, answer.getMessage());
                    send(chatId, "Введите даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться:");
                    answer.setState("setDates");
                    setBindingBy(chatId, answer);
                } else if (bindingBy.get(chatId).getState().equals("setDates")) {
                    var answer = bindingBy.get(chatId);
                    answer.setMessage(usersMessage);
                    answer = answer.getAction().setDates(answer);
                    answer = answer.getAction().getResult(answer);
                    bindingBy.remove(chatId);
                    send(chatId, answer.getMessage());
                    send(chatId, "Чтобы продолжить, выбери что-нибудь из меню");
                }
            }
        }
    }

    private void setBindingBy(Long chatID, Answer answer) {
        this.bindingBy.put(chatID, answer);
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
}