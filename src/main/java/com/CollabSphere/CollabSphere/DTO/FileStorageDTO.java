package com.CollabSphere.CollabSphere.DTO;

import lombok.Data;

import java.time.Instant;

public class FileStorageDTO {

    @Data
    public static class FileUploadResponseDTO {
        private Long id;
        private String fileName;
        private String contentType;
        private Long size;
        private Long teamId;
        private String uploaderEmail;
        private Instant createdAt;
        private String presignedDownloadUrl; // optional: included when requested
    }

    @Data
    public static class FileMetadataOnlyDTO {
        private Long id;
        private String fileName;
        private String contentType;
        private Long size;
        private Long teamId;
        private String uploaderEmail;
        private Instant createdAt;
    }
}
