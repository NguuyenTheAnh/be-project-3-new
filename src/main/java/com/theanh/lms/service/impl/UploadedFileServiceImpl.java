package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.constants.MessageCode;
import com.theanh.lms.dto.UploadedFileDto;
import com.theanh.lms.entity.UploadedFile;
import com.theanh.lms.model.StoredFile;
import com.theanh.lms.repository.UploadedFileRepository;
import com.theanh.lms.service.FileStorageService;
import com.theanh.lms.service.UploadedFileService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        UploadedFileDto dto = modelMapper.map(uploadedFile, getDtoClass());
        dto.setAccessUrl(fileStorageService.buildPublicUrl(objectKey));
        return dto;
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
