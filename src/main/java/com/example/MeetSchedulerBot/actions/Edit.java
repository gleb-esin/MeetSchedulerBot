package com.example.MeetSchedulerBot.actions;

import com.example.MeetSchedulerBot.service.Answer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class Edit extends Action implements ActionInterface {

    @Override
    public String getActionKey() {
        return "/edit";
    }


    @Override
    public Answer setMeetingName(Answer answer) {
        String passphrase = answer.getMessage();
        answer.getMeeting().setMonth(meetingRepository.findMonthByPassphrase(passphrase));
        if (meetingRepository.existsByPassphrase(passphrase)) {
            if (meetingRepository.existsByChatAndPassphrase(answer.getMeeting().getChat(), passphrase)) {
                answer.getMeeting().setMonth(meetingRepository.findMonthByPassphrase(passphrase));
                answer.getMeeting().setPassphrase(passphrase);
                answer.setState("setDates");
                answer.setQuestion("Выберите один из вариантов:\n" +
                        "<b>Убрать неподходящие даты</b> или <b>Оставить подходящие даты</b>");
                InlineKeyboardMarkup inlineButtons = setInlineButtons("Убрать даты--setBusyDates--Оставить даты--setAvailableDates");
                answer.setInlineButtons(inlineButtons);
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
        LocalDate userLocalDate = answer.getMeeting().getUserLocalDate();
        List<Integer> dates = datesParser(answer.getMessage(), userLocalDate);
        Long chat = answer.getMeeting().getChat();
        String passphrase = answer.getMeeting().getPassphrase();

        if (dates.isEmpty()) {
            answer.setMessage("Не распознал числа, повторите, пожалуйста ввод.");
            answer.setQuestion("Введите даты в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться:");
            answer.setState("Error");
            return answer;
        } else {
            List<Integer> commonDates = commonDates(meetingRepository.concatenateDatesByPassphrase(passphrase));
            if (answer.getState().contains("setBusyDates")) {
                commonDates.retainAll(busyToAvailableConverter(dates,userLocalDate));
                if (busyToAvailableConverter(dates, userLocalDate).containsAll(commonDates)) {
                    answer.getMeeting().setDates(commonDates);
                } else {
                    answer.setMessage("Не все ваши числа подходят другим учасникам встречи.");
                    answer.setQuestion("Введите даты из календаря выше, в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться:");
                    answer.setState("Error");
                    return answer;
                }
            } else {
                if (commonDates.containsAll(dates)) {
                    answer.getMeeting().setDates(dates);
                } else {
                    answer.setMessage("Не все ваши числа подходят другим учасникам встречи.");
                    answer.setQuestion("Введите даты из календаря выше, в которые Вы <u><b>НЕ МОЖЕТЕ</b></u> встретиться:");
                    answer.setState("Error");
                    return answer;
                }
            }
        }
        boolean isUserOwner = meetingRepository.isUserOwner(chat, passphrase);
        if (isUserOwner) answer.getMeeting().setOwner(true);
        meetingRepository.deleteByChatAndPassphrase(chat, passphrase);
        meetingRepository.deleteExpiredMeetings();
        meetingRepository.save(answer.getMeeting());
        answer.setMessage("Вы отредактировали даты своего участия во встрече <b>" + answer.getMeeting().getPassphrase() + "</b>: \n" +
                printMeeting(passphrase, userLocalDate));
        answer.setQuestion("Чтобы продолжить, выбери что-нибудь из меню");

        answer.setState("notify");
        answer.setNotification("<b>" + answer.getMeeting().getName() + "</b> изменил(-а) даты во встрече <b>" + answer.getMeeting().getPassphrase() + "</b>:\n" +
                printMeeting(passphrase, userLocalDate));
        String notifiedStr = meetingRepository.listOfNotified(passphrase);
        String[] notifiedArr = notifiedStr.split(" ");
        for (int i = 0; i < notifiedArr.length; i++) {
            answer.getMustBeNotified().add(Long.valueOf(notifiedArr[i]));
        }
        answer.getMustBeNotified().remove(answer.getMeeting().getChat());
        return answer;
    }


    private InlineKeyboardMarkup setInlineButtons(String buttonMetaString) {
        String[] buttonsMetaArr = buttonMetaString.split("--");
        List<String> buttonsName = new ArrayList<>();
        List<String> buttonsCallbackData = new ArrayList<>();
        for (int i = 0; i < buttonsMetaArr.length; i++) {
            if (i % 2 == 0) {
                buttonsName.add(buttonsMetaArr[i]);
            } else {
                buttonsCallbackData.add(buttonsMetaArr[i]);
            }
        }
        List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
        for (int i = 0; i < buttonsMetaArr.length / 2; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonsName.get(i));
            button.setCallbackData(buttonsCallbackData.get(i));
            buttonsRow.add(button);
        }
        List<List<InlineKeyboardButton>> rowsOfButton = new ArrayList<>();
        rowsOfButton.add(buttonsRow);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rowsOfButton);
        return inlineKeyboardMarkup;
    }
}