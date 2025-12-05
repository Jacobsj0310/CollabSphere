package com.CollabSphere.CollabSphere.DTO;

import lombok.Data;

import java.util.List;

@Data
public class TeamDTO {
    private Long id;                // team ID (optional for create)
    private String name;            // team name
    private String description;     // team description
    private Long adminId;           // user ID of the admin/creator
    private List<Long> memberIds;   // list of user IDs in the team

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }
}
