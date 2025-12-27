package com.theanh.lms.repository;

import com.theanh.common.base.BaseRepository;
import com.theanh.lms.entity.UploadedFile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadedFileRepository extends BaseRepository<UploadedFile, Long> {

    Optional<UploadedFile> findByObjectKey(String objectKey);
}
