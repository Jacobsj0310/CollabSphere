package com.CollabSphere.CollabSphere.Interface;

import com.CollabSphere.CollabSphere.DTO.FileStorageDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileStorageServiceInterface {

    /**
     * Upload file to S3 and save metadata.
     * requesterEmail used to find uploader; teamId is optional (null allowed).
     */
    FileStorageDTO.FileUploadResponseDTO uploadFile(Long teamId, MultipartFile file, String requesterEmail) throws Exception;

    /**
     * Get presigned download URL for a stored file (valid for given seconds).
     * Performs authorization check (uploader / team member / admin).
     */
    String getPresignedDownloadUrl(Long fileId, String requesterEmail, int validitySeconds) throws Exception;

    /**
     * Delete a file (soft delete metadata + delete from S3).
     */
    void deleteFile(Long fileId, String requesterEmail) throws Exception;

    /**
     * List files for a team (metadata only).
     */
    List<FileStorageDTO.FileMetadataOnlyDTO> listFilesForTeam(Long teamId);

    /**
     * Stream download (if you prefer server stream instead of presigned url).
     * Returns InputStream of the object from S3. Caller must close it.
     */
    InputStream downloadFileStream(Long fileId, String requesterEmail) throws Exception;

}
