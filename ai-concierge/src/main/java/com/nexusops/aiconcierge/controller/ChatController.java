package com.nexusops.aiconcierge.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
            .defaultSystem("You are the automated NexusSpace AI Concierge. You manage facility resources. If asked to book a room, strictly use the 'createReservationTool' with a properly formatted ISO-8601 date, and ask clarifying questions if details are missing.")
            .defaultFunctions("createReservationTool")
            .build();
    }

    @PostMapping
    public String chat(@RequestBody String message) {
        return chatClient.prompt()
            .user(message)
            .call()
            .content();
    }
}
