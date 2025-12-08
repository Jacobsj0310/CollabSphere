package com.CollabSphere.CollabSphere.Repository;

import com.CollabSphere.CollabSphere.Entity.FileStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {

    List<FileStorage> findByTeamIdAndDeletedFalseOrderByCreatedAtDesc(Long teamId);

    List<FileStorage> findByUploaderIdAndDeletedFalseOrderByCreatedAtDesc(Long uploaderId);
}