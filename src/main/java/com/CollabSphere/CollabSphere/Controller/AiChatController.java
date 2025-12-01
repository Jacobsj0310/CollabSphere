package com.CollabSphere.CollabSphere.Controller;

import com.CollabSphere.CollabSphere.Entity.AiMessage;
import com.CollabSphere.CollabSphere.Service.AiMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/messages")


public class AiChatController {
    @Autowired
    private final AiMessageService aiMessageService;

    public AiChatController(AiMessageService aiMessageService) {
        this.aiMessageService = aiMessageService;
    }

    // Create/save new AI message
    @PostMapping
    public ResponseEntity<AiMessage> createMessage(@RequestBody AiMessage message) {
        AiMessage saved = aiMessageService.saveMessage(message);
        return ResponseEntity.ok(saved);
    }

    // Update message
    @PutMapping("/{id}")
    public ResponseEntity<AiMessage> updateMessage(@PathVariable Long id, @RequestBody AiMessage update) {
        AiMessage updated = aiMessageService.updateMessage(id, update);
        return ResponseEntity.ok(updated);
    }

    // Delete message
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        aiMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    // Get message by id
    @GetMapping("/{id}")
    public ResponseEntity<AiMessage> getMessageById(@PathVariable Long id) {
        return aiMessageService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get messages in a chat room
    @GetMapping("/chatrooms/{chatRoomId}")
    public ResponseEntity<List<AiMessage>> getMessagesByChatRoom(@PathVariable Long chatRoomId) {
        List<AiMessage> messages = aiMessageService.getMessagesByChatRoom(chatRoomId);
        return ResponseEntity.ok(messages);
    }

    // Get messages by sender
    @GetMapping("/senders/{senderId}")
    public ResponseEntity<List<AiMessage>> getMessagesBySender(@PathVariable Long senderId) {
        List<AiMessage> messages = aiMessageService.getMessagesBySender(senderId);
        return ResponseEntity.ok(messages);
    }


}
