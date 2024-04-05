package com.example.meetSchedulerBot.service;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Getter
@Setter
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
    private LocalDate expired;


    //need to test
    public void setDates(List<Integer> availableDaysList, LocalDate meetingDate) {
        this.dates = availableDaysList;
        this.expired = LocalDate.of(meetingDate.getYear(), getMonth(), availableDaysList.get(availableDaysList.size() - 1));
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setMonth(String month) {
        Map<String, Integer> monthMap = Map.ofEntries(Map.entry("январь", 1), Map.entry("февраль", 2), Map.entry("март", 3), Map.entry("апрель", 4), Map.entry("май", 5), Map.entry("июнь", 6), Map.entry("июль", 7), Map.entry("август", 8), Map.entry("сентябрь", 9), Map.entry("октябрь", 10), Map.entry("ноябрь", 11), Map.entry("декабрь", 12));
        this.month = monthMap.get(month.toLowerCase());
    }

    @Override
    public String toString() {
        return "Meeting{" + "meetingID=" + id + ", chat=" + chat + ", name='" + name + '\'' + ", passphrase='" + passphrase + '\'' + ", month=" + month + ", owner=" + owner + ", dates=" + dates + ", expired=" + expired + "}";
    }
}
