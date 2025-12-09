package com.CollabSphere.CollabSphere.Interface;


import com.CollabSphere.CollabSphere.Entity.FileStorage;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Duration;
import java.util.List;

public interface AwsS3ServiceInterface {

    /**
     * Upload MultipartFile to S3 and save metadata to DB.
     *
     * @param file      incoming multipart file
     * @param teamId    optional team id to attach file to (nullable)
     * @param uploaderId id of uploading user
     * @return saved FileStorage entity
     */
    FileStorage uploadFile(MultipartFile file, Long teamId, Long uploaderId);

    /**
     * Generate a presigned GET URL for a stored file.
     *
     * @param fileId file id
     * @param expiry duration of presigned URL (if null -> default 15 minutes)
     * @return presigned URL (java.net.URL)
     */
    URL generatePresignedDownloadUrl(Long fileId, Duration expiry);

    /**
     * Soft-delete file metadata and delete S3 object (best-effort).
     *
     * @param fileId         file id
     * @param requesterEmail email of the requestor (for permission checks)
     */
    void deleteFile(Long fileId, String requesterEmail);

    /**
     * Fetch FileStorage metadata by id.
     */
    FileStorage getFileMetadata(Long fileId);

    /**
     * List files for a team (not deleted), newest first.
     */
    List<FileStorage> listFilesForTeam(Long teamId);

    /**
     * List files uploaded by a user (not deleted), newest first.
     */
    List<FileStorage> listFilesUploadedByUser(Long uploaderId);
}
