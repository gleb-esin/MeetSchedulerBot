package com.example.meetSchedulerBot.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides asynchronous methods to add and retrieve incoming messages from a messages monitor.
 */
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageMonitor {
    ConcurrentHashMap<Long, CompletableFuture<String>> messagesMonitor = new ConcurrentHashMap<>();

    /**
     * Completes the future with given text from given chatId request.
     *
     * @param  chatId    the ID of the chat
     * @param  text      the text of the incoming message
     */

    public void completeRequestedMessage(Long chatId, String text) {
        CompletableFuture<String> future;
        if (messagesMonitor.containsKey(chatId)) {
            future = messagesMonitor.get(chatId);
            future.complete(text);
            messagesMonitor.putIfAbsent(chatId, future);
        }
    }

    /**
     * Removes previous not requested message from the monitor.
     * Then creates a request with a new CompletableFuture<String> and adds it to the messagesMonitor map.
     * When the future will be completed, it returns the string with which the CompletableFuture was completed.
     *
     * @param  chatId  the client ID
     * @return         the string with which the CompletableFuture was completed.
     */

    public String requestIncomingMessage(Long chatId) {
        messagesMonitor.remove(chatId);
        CompletableFuture<String> future = new CompletableFuture<>();
        messagesMonitor.putIfAbsent(chatId, future);
        return future.join();
    }
}
