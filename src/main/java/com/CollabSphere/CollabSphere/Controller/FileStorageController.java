package com.CollabSphere.CollabSphere.Controller;

import com.CollabSphere.CollabSphere.DTO.FileStorageDTO;
import com.CollabSphere.CollabSphere.Service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileStorageController {

    private final FileStorageService fileStorageService;

    /**
     * Upload file (multipart). teamId optional (attach to team).
     * POST /api/files?teamId=123
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileStorageDTO.FileUploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file,
                                                                           @RequestParam(value = "teamId", required = false) Long teamId,
                                                                           Authentication authentication) throws Exception {
        String email = authentication.getName();
        FileStorageDTO.FileUploadResponseDTO resp = fileStorageService.uploadFile(teamId, file, email);
        return ResponseEntity.created(URI.create("/api/files/" + resp.getId())).body(resp);
    }

    /**
     * Return presigned download URL for the file (so client downloads directly from S3)
     * GET /api/files/{id}/download?validFor=300
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<?> getPresignedUrl(@PathVariable("id") Long fileId,
                                             @RequestParam(value = "validFor", defaultValue = "300") int validFor,
                                             Authentication authentication) throws Exception {
        String email = authentication.getName();
        String url = fileStorageService.getPresignedDownloadUrl(fileId, email, validFor);
        return ResponseEntity.ok().body(java.util.Map.of("url", url));
    }

    /**
     * Stream download via server (optional)
     * GET /api/files/{id}/stream
     */
    @GetMapping("/{id}/stream")
    public ResponseEntity<?> streamDownload(@PathVariable("id") Long fileId,
                                            Authentication authentication) throws Exception {
        String email = authentication.getName();
        InputStream in = fileStorageService.downloadFileStream(fileId, email);
        // You can set content-disposition and content-type by reading metadata if needed
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file\"")
                .body(new org.springframework.core.io.InputStreamResource(in));
    }

    /**
     * List files for a team (metadata only)
     * GET /api/files/team/{teamId}
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<FileStorageDTO.FileMetadataOnlyDTO>> listTeamFiles(@PathVariable Long teamId) {
        List<FileStorageDTO.FileMetadataOnlyDTO> list = fileStorageService.listFilesForTeam(teamId);
        return ResponseEntity.ok(list);
    }

    /**
     * Delete file (soft delete + try to delete object from S3)
     * DELETE /api/files/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable("id") Long id, Authentication authentication) throws Exception {
        String email = authentication.getName();
        fileStorageService.deleteFile(id, email);
        return ResponseEntity.noContent().build();
    }
}