package com.example.meetSchedulerBot.actions;

import com.example.meetSchedulerBot.service.Meeting;

public interface ActionInterface {
    void run(Meeting meeting);
}
