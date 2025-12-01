package com.CollabSphere.CollabSphere.Controller;

import com.CollabSphere.CollabSphere.DTO.ChatRoomRequestDTO;
import com.CollabSphere.CollabSphere.DTO.ParticipantRequestDTO;
import com.CollabSphere.CollabSphere.Entity.ChatRoom;
import com.CollabSphere.CollabSphere.Service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatRoomController {

    @Autowired
    private final ChatRoomService chatRoomService;


    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;

    }

    @PostMapping
    public ResponseEntity<ChatRoom> create(@RequestBody ChatRoomRequestDTO req) {
        ChatRoom created = chatRoomService.createChatRoom(req);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChatRoom> update(@PathVariable Long id, @RequestBody ChatRoomRequestDTO req) {
        ChatRoom updated = chatRoomService.updateChatRoom(id, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chatRoomService.deleteChatRoom(id);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ChatRoom> get(@PathVariable Long id) {
//        return ChatRoomService.getByChatRoomId(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
@GetMapping("/{id}")
public ResponseEntity<ChatRoom> get(@PathVariable Long id) {
    ChatRoom room = chatRoomService.getByChatRoomId(id);
    if (room == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(room);
}

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<ChatRoom>> listByWorkspace(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(chatRoomService.getByWorkspaceId(workspaceId));
    }

    @PostMapping("/{id}/participants")
    public ResponseEntity<Void> addParticipant(@PathVariable Long id, @RequestBody ParticipantRequestDTO req) {
        chatRoomService.addParticipant(id, req.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/participants/{userId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable Long id, @PathVariable Long userId) {
        chatRoomService.removeParticipant(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archive(@PathVariable Long id) {
        chatRoomService.archiveChat(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pin")
    public ResponseEntity<Void> pin(@PathVariable Long id) {
        chatRoomService.pinChat(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/transfer/{newOwnerId}")
    public ResponseEntity<Void> transfer(@PathVariable Long id, @PathVariable Long newOwnerId) {
        chatRoomService.transferOwnership(id, newOwnerId);
        return ResponseEntity.ok().build();
    }
}
