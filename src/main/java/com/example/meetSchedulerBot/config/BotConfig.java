package com.example.meetSchedulerBot.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {
    @Value("${webhookpath}")
    String webHookPath;
    @Value("${bot.name}")
    String userName;
    @Value("${bot.token}")
    String botToken;
}
