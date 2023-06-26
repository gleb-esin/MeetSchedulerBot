package com.example.MeetSchedulerBot.service;

import com.example.MeetSchedulerBot.actions.*;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private Map<Long, Answer> bindingBy = new ConcurrentHashMap<>();
    @Autowired
    private List<ActionInterface> actionBeans = new ArrayList<>(Arrays.asList(new New(), new Join(), new Find(), new Edit(), new RemoveMe(), new DeleteMeeting(), new Feedback()));
    private final List<String> actions = Arrays.asList("/new", "/join", "/find", "/edit", "/removeme", "/deletemeeting", "/feedback");
    private String state;
    private final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> menu = new ArrayList<>();
        menu.add(new BotCommand("/new", "Создание новой встечи"));
        menu.add(new BotCommand("/join", "Присоединисться"));
        menu.add(new BotCommand("/find", "Найти встречу"));
        menu.add(new BotCommand("/edit", "Редактировать даты"));
        menu.add(new BotCommand("/removeme", "Удалить свое участие"));
        menu.add(new BotCommand("/feedback", "Оставить отзыв, предложение или замечение"));

        try {
            execute(new SetMyCommands(menu, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list" + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String usersMessage = update.getMessage().getText();
            if (update.getMessage().getText().equals("/start")) {
                send(chatId, "Данный  бот помогает выбрать общие даты при планировании встречи. Участникам предлагается ввести даты, когда, по их мнению, встреча не возможна. Оставшиеся даты считаются приемлемым для всех. Если дат осталось слишком много, по желанию участников, можно отредактировать свои даты, сузив число конечных дат. \n" +
                        "\n" +
                        "Бот позволяет создавать новые встречи, присоединяться к уже существующим встречам, редактировать даты в уже существующей встрече, удалять свое участие и те встречи, владельцем которых является пользователь.\n" +
                        "\n" +
                        "После создания встречи бот сгенерирует сообщение для пересылки, где будет указано имя создателя встречи, название встречи и ссылка на бота. Тем, кого Вы хотите пригласить, останется лишь перейти по внутренней ссылке в чат с ботом и ввести название Вашей встречи.\n" +
                        "\n" +
                        "Если кто-то присоединится к встрече,  у встречи изменятся даты или кто-то не захочет в ней участвовать, бот пришлет оповещение остальным участникам встречи.\n" +
                        "\n" +
                        "Встречайтесь чаще!");

                //if user's usersMessage contains some menu command
            } else if (actions.contains(usersMessage)) {
                //set chatId and name
                Meeting meeting = new Meeting();
                Answer answer = new Answer();
                meeting.setChat(chatId);
                meeting.setName(update.getMessage().getChat().getFirstName());
                answer.setMeeting(meeting);
                answer.setAction(getAction(usersMessage));

                if (usersMessage.equals("/feedback")) {
                    send(chatId, "Напишите Ваши вопросы, предложения, замечания:");
                    answer.setState("getResult");

                } else {
                    send(chatId, "Введите название встречи:");
                    answer.setState("setMeetingName");
                }
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
                        if (answer.getInlineButtons() == null) {
                            send(chatId, answer.getQuestion());
                        } else {
                            sendInlineMarkup(chatId, answer.getQuestion(), answer.getInlineButtons());
                        }
                        if ((bindingBy.get(chatId).getAction() instanceof Find)) {
                            bindingBy.remove(chatId);
                        }
                    }
                } else if (bindingBy.get(chatId).getState().equals("setMonth")) {
                    var answer = bindingBy.get(chatId);
                    answer.setMessage(usersMessage);
                    answer = answer.getAction().setMonth(answer);
                    if (answer.getState().equals("Error")) {
                        send(chatId, answer.getMessage());
                        answer.setState("setMonth");
                    } else {
                        send(chatId, answer.getMessage());
                        send(chatId, answer.getQuestion());
                        setBindingBy(chatId, answer);
                    }
                } else if (bindingBy.get(chatId).getState().contains("getResult")) {
                    var answer = bindingBy.get(chatId);
                    answer.setMessage(usersMessage);
                    answer = answer.getAction().getResult(answer);
                    send(chatId, answer.getMessage());
                    if (answer.getState().equals("notify")) {
                        for (Long l : answer.getMustBeNotified()) {
                            send(l, answer.getNotification());
                        }
                        send(chatId, answer.getQuestion());
                        bindingBy.remove(chatId);
                    } else if (answer.getState().equals("finish")) {
                        send(chatId, answer.getQuestion());
                        bindingBy.remove(chatId);
                    }
                }
            } else {
                send(chatId, "Команда не распознана. Выбери что-нибудь из меню");
            }
        } else if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals("setBusyDates")) {
                var chatId = update.getCallbackQuery().getMessage().getChatId();
                var answer = bindingBy.get(chatId);
                answer.setQuestion("Введите новые даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться в формате 1 3 7-15:\n" +
                        "(Если таких дат нет, введите 0)");
                answer.setState("getResult setBusyDates");
                send(chatId, answer.getQuestion());
                setBindingBy(chatId, answer);
            } else if (update.getCallbackQuery().getData().equals("setAvailableDates")) {
                var chatId = update.getCallbackQuery().getMessage().getChatId();
                var answer = bindingBy.get(chatId);
                answer.setQuestion("Введите даты, которые Вы считаете наиболее <u><b>ПОДХОДЯЩИМИ</b></u> в формате 1 3 7-15:\n" +
                        "(Чем больше дат Вы отметите, тем с большей вероятностью состоятся встреча)");
                answer.setState("getResult setAvailableDates");
                send(chatId, answer.getQuestion());
                setBindingBy(chatId, answer);
            }
        } else {
            send(update.getMessage().getChatId(), "Понимаю только буквы. Повторите ввод, пожалуйста.");
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

    public void sendInlineMarkup(Long chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        message.setParseMode(ParseMode.HTML);
        message.setReplyMarkup(inlineKeyboardMarkup);
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