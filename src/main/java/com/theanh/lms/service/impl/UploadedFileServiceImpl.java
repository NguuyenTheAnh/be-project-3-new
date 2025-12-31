package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.constants.MessageCode;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.entity.UploadedFile;
import com.theanh.lms.enums.UploadPurpose;
import com.theanh.lms.enums.UploadedFileStatus;
import com.theanh.lms.model.StoredFile;
import com.theanh.lms.repository.UploadedFileRepository;
import com.theanh.lms.service.FileStorageService;
import com.theanh.lms.service.UploadedFileService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
public class UploadedFileServiceImpl extends BaseServiceImpl<UploadedFile, UploadedFileDto, Long> implements UploadedFileService {

    private final UploadedFileRepository uploadedFileRepository;
    private final FileStorageService fileStorageService;

    public UploadedFileServiceImpl(UploadedFileRepository uploadedFileRepository,
                                   ModelMapper modelMapper,
                                   FileStorageService fileStorageService) {
        super(uploadedFileRepository, modelMapper);
        this.uploadedFileRepository = uploadedFileRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public UploadedFileDto store(MultipartFile file, boolean isPublic) {
        StoredFile storedFile = fileStorageService.store(file, isPublic);
        UploadedFile entity = UploadedFile.builder()
                .storageProvider(storedFile.getStorageProvider())
                .bucket(storedFile.getBucket())
                .objectKey(storedFile.getObjectKey())
                .originalName(storedFile.getOriginalName())
                .contentType(storedFile.getContentType())
                .sizeBytes(storedFile.getSizeBytes())
                .checksumSha256(storedFile.getChecksumSha256())
                .isPublic(storedFile.getIsPublic())
                .status(UploadedFileStatus.READY.name())
                .purpose(UploadPurpose.GENERIC.name())
                .build();
        entity.setIsActive(Boolean.TRUE);
        entity.setIsDeleted(Boolean.FALSE);
        UploadedFile saved = uploadedFileRepository.save(entity);
        UploadedFileDto dto = modelMapper.map(saved, getDtoClass());
        dto.setAccessUrl(fileStorageService.buildPublicUrl(dto.getObjectKey()));
        return dto;
    }

    @Override
    public UploadedFileDto getByObjectKey(String objectKey) {
        UploadedFile uploadedFile = uploadedFileRepository.findByObjectKey(objectKey)
                .orElseThrow(() -> new BusinessException(MessageCode.FILE_NOT_FOUND));
        if (Boolean.TRUE.equals(uploadedFile.getIsDeleted())) {
            throw new BusinessException(MessageCode.FILE_NOT_FOUND);
        }
        if (UploadedFileStatus.PENDING.name().equals(uploadedFile.getStatus())) {
            throw new BusinessException(MessageCode.FILE_NOT_READY);
        }
        UploadedFileDto dto = modelMapper.map(uploadedFile, getDtoClass());
        dto.setAccessUrl(fileStorageService.buildPublicUrl(objectKey));
        return dto;
    }

    @Override
    public UploadedFileDto createPending(UploadPurpose purpose, String bucket, String objectKey, String filename,
                                         String contentType, Long size, Boolean isPublic, Long courseId, Long lessonId) {
        UploadedFile entity = UploadedFile.builder()
                .storageProvider("MINIO")
                .bucket(bucket)
                .objectKey(objectKey)
                .originalName(filename)
                .contentType(contentType)
                .sizeBytes(size)
                .isPublic(Boolean.TRUE.equals(isPublic))
                .status(UploadedFileStatus.PENDING.name())
                .purpose(purpose.name())
                .courseId(courseId)
                .lessonId(lessonId)
                .build();
        entity.setIsActive(Boolean.TRUE);
        entity.setIsDeleted(Boolean.FALSE);
        UploadedFile saved = uploadedFileRepository.save(entity);
        return modelMapper.map(saved, getDtoClass());
    }

    @Override
    public UploadedFileDto markReady(Long fileId) {
        UploadedFile entity = findActive(fileId);
        if (!Objects.equals(entity.getStatus(), UploadedFileStatus.PENDING.name())) {
            throw new BusinessException(MessageCode.FILE_INVALID_STATUS);
        }
        StoredFile meta = fileStorageService.getMetadata(entity.getObjectKey());
        if (meta == null || meta.getSizeBytes() == null || meta.getSizeBytes() <= 0) {
            throw new BusinessException(MessageCode.FILE_NOT_FOUND);
        }
        entity.setSizeBytes(meta.getSizeBytes());
        if (meta.getContentType() != null) {
            entity.setContentType(meta.getContentType());
        }
        entity.setChecksumSha256(meta.getChecksumSha256());
        entity.setStatus(UploadedFileStatus.READY.name());
        UploadedFile saved = uploadedFileRepository.save(entity);
        UploadedFileDto dto = modelMapper.map(saved, getDtoClass());
        dto.setAccessUrl(fileStorageService.buildPublicUrl(dto.getObjectKey()));
        return dto;
    }

    @Override
    public UploadedFileDto abort(Long fileId) {
        UploadedFile entity = findActive(fileId);
        if (UploadedFileStatus.ATTACHED.name().equals(entity.getStatus())) {
            throw new BusinessException(MessageCode.FILE_DELETE_GUARD);
        }
        try {
            fileStorageService.delete(entity.getObjectKey());
        } catch (Exception ignored) {
            // swallow delete errors for idempotency
        }
        entity.setStatus(UploadedFileStatus.DELETED.name());
        entity.setIsDeleted(Boolean.TRUE);
        UploadedFile saved = uploadedFileRepository.save(entity);
        return modelMapper.map(saved, getDtoClass());
    }

    @Override
    public UploadedFileDto markAttached(Long fileId) {
        UploadedFile entity = ensureReady(fileId);
        entity.setStatus(UploadedFileStatus.ATTACHED.name());
        UploadedFile saved = uploadedFileRepository.save(entity);
        return modelMapper.map(saved, getDtoClass());
    }

    @Override
    public UploadedFileDto markAttached(Long fileId, Long courseId, Long lessonId, UploadPurpose purpose) {
        UploadedFile entity = ensureReady(fileId);
        if (UploadedFileStatus.ATTACHED.name().equals(entity.getStatus())) {
            // If already attached elsewhere, block
            if (!Objects.equals(entity.getCourseId(), courseId) || !Objects.equals(entity.getLessonId(), lessonId)) {
                throw new BusinessException(MessageCode.FILE_ALREADY_ATTACHED);
            }
            return modelMapper.map(entity, getDtoClass());
        }
        if (courseId != null) {
            entity.setCourseId(courseId);
        }
        if (lessonId != null) {
            entity.setLessonId(lessonId);
        }
        if (purpose != null) {
            entity.setPurpose(purpose.name());
        }
        entity.setStatus(UploadedFileStatus.ATTACHED.name());
        UploadedFile saved = uploadedFileRepository.save(entity);
        return modelMapper.map(saved, getDtoClass());
    }

    @Override
    public UploadedFile ensureReady(Long fileId) {
        UploadedFile entity = findActive(fileId);
        if (UploadedFileStatus.DELETED.name().equals(entity.getStatus())) {
            throw new BusinessException(MessageCode.FILE_NOT_FOUND);
        }
        if (!UploadedFileStatus.READY.name().equals(entity.getStatus())
                && !UploadedFileStatus.ATTACHED.name().equals(entity.getStatus())) {
            throw new BusinessException(MessageCode.FILE_NOT_READY);
        }
        return entity;
    }

    private UploadedFile findActive(Long id) {
        return uploadedFileRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(MessageCode.FILE_NOT_FOUND));
    }

    @Override
    protected Class<UploadedFile> getEntityClass() {
        return UploadedFile.class;
    }

    @Override
    protected Class<UploadedFileDto> getDtoClass() {
        return UploadedFileDto.class;
    }
}
