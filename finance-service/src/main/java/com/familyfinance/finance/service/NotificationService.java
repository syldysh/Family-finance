package com.familyfinance.finance.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyFamilyUpdate(Long familyId) {
        messagingTemplate.convertAndSend(
                "/topic/family/" + familyId,
                Map.of("event", "transaction_added", "familyId", familyId)
        );
    }
}
