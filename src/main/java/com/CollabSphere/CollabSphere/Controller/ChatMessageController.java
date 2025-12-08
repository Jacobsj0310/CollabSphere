package com.CollabSphere.CollabSphere.Controller;

import com.CollabSphere.CollabSphere.DTO.ChatMessageDTO;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import com.CollabSphere.CollabSphere.Service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;


    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDTO> createMessage(@Valid @RequestBody ChatMessageDTO dto,
                                                        Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        // ensure server controls senderId (ignore client-supplied senderId)
        dto.setSenderId(userId);

        ChatMessageDTO created = chatMessageService.createMessage(dto);
        return ResponseEntity
                .created(URI.create("/api/chat/messages/" + created.getId()))
                .body(created);
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<ChatMessageDTO> getMessage(@PathVariable("id") Long id) {
        ChatMessageDTO dto = chatMessageService.getMessage(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/teams/{teamId}/messages")
    public ResponseEntity<Page<ChatMessageDTO>> getMessagesForTeam(
            @PathVariable("teamId") Long teamId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<ChatMessageDTO> pageResult = chatMessageService.getMessagesForTeam(teamId, pageable);
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/channel/{channel}/messages")
    public ResponseEntity<Page<ChatMessageDTO>> getMessagesForChannel(
            @PathVariable("channel") String channel,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<ChatMessageDTO> pageResult = chatMessageService.getMessagesForChannel(channel, pageable);
        return ResponseEntity.ok(pageResult);
    }


    @PutMapping("/messages/{id}")
    public ResponseEntity<ChatMessageDTO> editMessage(@PathVariable("id") Long messageId,
                                                      @Valid @RequestBody ChatMessageDTO dto,
                                                      Authentication authentication) {
        Long editorUserId = getUserIdFromAuth(authentication);
        ChatMessageDTO updated = chatMessageService.editMessage(messageId, editorUserId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable("id") Long messageId,
                                              Authentication authentication) {
        Long requestorUserId = getUserIdFromAuth(authentication);
        chatMessageService.deleteMessage(messageId, requestorUserId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated request");
        }

        String principal = authentication.getName(); // your CustomUserDetailsService should set username = email
        User user = userRepository.findByEmail(principal)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + principal));
        return user.getId();
    }
}