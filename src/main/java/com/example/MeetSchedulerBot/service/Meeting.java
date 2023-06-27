package com.example.MeetSchedulerBot.service;


import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import java.util.List;
import java.util.Map;

@Data
@Getter
@Entity
@Component
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private Long chat;
    @Column
    private String name;
    @Column
    private String passphrase;
    @Column
    private int month;
    @Column
    private boolean owner = false;
    @Column
    private List<Integer> dates;
    @Column
    LocalDate expired;

    //need to test
    public void setDates(List<Integer> availableDaysList) {
        this.dates = availableDaysList;
        this.expired = LocalDate.of(getUserLocalDate().getYear(),
                getMonth(),
                availableDaysList.get(availableDaysList.size() - 1));
    }


    public void setStringToMonth(String month) {
        Map<String, Integer> monthMap = Map.ofEntries(
                Map.entry("январь", 1),
                Map.entry("февраль", 2),
                Map.entry("март", 3),
                Map.entry("апрель", 4),
                Map.entry("май", 5),
                Map.entry("июнь", 6),
                Map.entry("июль", 7),
                Map.entry("август", 8),
                Map.entry("сентябрь", 9),
                Map.entry("октябрь", 10),
                Map.entry("ноябрь", 11),
                Map.entry("декабрь", 12));
        this.month = monthMap.get(month.toLowerCase());
    }

    public LocalDate getUserLocalDate() {
        LocalDate userLocalDate;
        if (getMonth() == LocalDate.now().getMonthValue()) {
            userLocalDate = LocalDate.now();
        } else if (getMonth() > LocalDate.now().getMonthValue()) {
            userLocalDate = LocalDate.of(LocalDate.now().getYear(), getMonth(), 1);
        } else {
            userLocalDate = LocalDate.of(LocalDate.now().plusYears(1).getYear(), getMonth(), 1);
        }
        return userLocalDate;
    }

//    public void setId(Long chatId) {
//        this.chatId = chatId;
//    }
}
