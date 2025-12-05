package com.CollabSphere.CollabSphere.DTO;

import lombok.Data;

import java.util.List;

@Data
public class TeamDTO {
    @Data
    public static class TeamRequest {
        private String name;                 // Team name
        private String description;          // Team description
        private List<String> memberEmails;   // List of member emails to add

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

        public List<String> getMemberEmails() {
            return memberEmails;
        }

        public void setMemberEmails(List<String> memberEmails) {
            this.memberEmails = memberEmails;
        }
    }

    //  2. RESPONSE DTO
    @Data
    public static class TeamResponse {
        private Long id;                      // Team ID
        private String name;                  // Team name
        private String description;           // Team description
        private String adminEmail;            // Admin email (team owner)
        private List<String> memberEmails; // List of all team member

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

        public String getAdminEmail() {
            return adminEmail;
        }

        public void setAdminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
        }

        public List<String> getMemberEmails() {
            return memberEmails;
        }

        public void setMemberEmails(List<String> memberEmails) {
            this.memberEmails = memberEmails;
        }
    }
}
