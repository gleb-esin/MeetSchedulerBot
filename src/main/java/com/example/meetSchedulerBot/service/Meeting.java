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

    public void setDates(List<Integer> availableDaysList) {
        this.dates = availableDaysList;

    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setMonth(String month) {
        Map<String, Integer> monthMap = Map.ofEntries(Map.entry("январь", 1), Map.entry("февраль", 2), Map.entry("март", 3), Map.entry("апрель", 4), Map.entry("май", 5), Map.entry("июнь", 6), Map.entry("июль", 7), Map.entry("август", 8), Map.entry("сентябрь", 9), Map.entry("октябрь", 10), Map.entry("ноябрь", 11), Map.entry("декабрь", 12));
        this.month = monthMap.get(month.toLowerCase());
    }

    public void setExpired(LocalDate meetingDate, List<Integer> availableDaysList) {
        Integer lastAvailableDay = 1;
        if (!availableDaysList.isEmpty()) {
            lastAvailableDay = availableDaysList.get(availableDaysList.size() - 1);
        }
        this.expired = LocalDate.of(meetingDate.getYear(), meetingDate.getMonthValue(), lastAvailableDay);

    }

    @Override
    public String toString() {
        return "Meeting{" + "meetingID=" + id + ", chat=" + chat + ", name='" + name + '\'' + ", passphrase='" + passphrase + '\'' + ", month=" + month + ", owner=" + owner + ", dates=" + dates + ", expired=" + expired + "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meeting meeting = (Meeting) o;
        return month == meeting.month &&
                chat.equals(meeting.chat) &&
                owner == meeting.owner &&
               name.equals(meeting.name) &&
                passphrase.equals(meeting.passphrase) &&
                dates.equals(meeting.dates) &&
                expired.equals(meeting.expired);
    }
}
