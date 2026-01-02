package com.theanh.lms.service.impl;

import com.theanh.common.base.BaseServiceImpl;
import com.theanh.common.exception.BusinessException;
import com.theanh.lms.dto.UserDto;
import com.theanh.lms.dto.request.ProfileUpdateRequest;
import com.theanh.lms.dto.request.UserCreateRequest;
import com.theanh.lms.dto.request.UserUpdateRequest;
import com.theanh.lms.entity.Role;
import com.theanh.lms.entity.User;
import com.theanh.lms.entity.UserRole;
import com.theanh.lms.enums.UserStatus;
import com.theanh.lms.enums.UploadPurpose;
import com.theanh.lms.repository.RoleRepository;
import com.theanh.lms.repository.UserRepository;
import com.theanh.lms.repository.UserRoleRepository;
import com.theanh.lms.service.RoleService;
import com.theanh.lms.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.theanh.lms.service.UploadedFileService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends BaseServiceImpl<User, UserDto, Long> implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UploadedFileService uploadedFileService;

    public UserServiceImpl(UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           RoleRepository roleRepository,
                           RoleService roleService,
                           ModelMapper mapper,
                           PasswordEncoder passwordEncoder,
                           UploadedFileService uploadedFileService) {
        super(userRepository, mapper);
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.uploadedFileService = uploadedFileService;
    }

    @Override
    public Optional<User> findActiveByEmail(String email) {
        return userRepository.findByEmailAndIsDeletedFalse(email);
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .ifPresent(u -> {
                    throw new BusinessException("data.exists");
                });
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .avatarFileId(request.getAvatarFileId())
                .status(UserStatus.ACTIVE)
                .build();
        user.setIsActive(Boolean.TRUE);
        user.setIsDeleted(Boolean.FALSE);
        User saved = userRepository.save(user);
        if (request.getAvatarFileId() != null) {
            uploadedFileService.markAttached(request.getAvatarFileId(), null, null, UploadPurpose.DOCUMENT);
        }
        Set<String> roles = assignRoles(saved.getId(), request.getRoles());
        return toDto(saved, roles);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAvatarFileId() != null) {
            user.setAvatarFileId(request.getAvatarFileId());
            uploadedFileService.markAttached(request.getAvatarFileId(), null, null, UploadPurpose.DOCUMENT);
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getIsDeleted() != null) {
            user.setIsDeleted(request.getIsDeleted());
        }
        User saved = userRepository.save(user);
        Set<String> roles = assignRoles(saved.getId(), request.getRoles());
        return toDto(saved, roles);
    }

    @Override
    @Transactional
    public UserDto updateProfile(Long id, ProfileUpdateRequest request) {
        User user = userRepository.findById(id)
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .orElseThrow(() -> new BusinessException("data.not_found"));
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAvatarFileId() != null) {
            user.setAvatarFileId(request.getAvatarFileId());
            uploadedFileService.markAttached(request.getAvatarFileId(), null, null, UploadPurpose.DOCUMENT);
        }
        User saved = userRepository.save(user);
        Set<String> roles = findRoles(saved.getId());
        return toDto(saved, roles);
    }

    @Override
    public Set<String> findRoles(Long userId) {
        List<UserRole> mappings = userRoleRepository.findByUserId(userId);
        if (mappings.isEmpty()) {
            return Set.of();
        }
        Set<Long> roleIds = mappings.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        return roleRepository.findAllById(roleIds).stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public Page<UserDto> search(String keyword, Pageable pageable) {
        Specification<User> spec = notDeletedOrNullSpec();
        if (StringUtils.hasText(keyword)) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("fullName")), "%" + keyword.toLowerCase() + "%")
            ));
        }
        Page<User> page = userRepository.findAll(spec, pageable);
        List<UserDto> content = page.getContent().stream()
                .map(user -> toDto(user, findRoles(user.getId())))
                .toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public UserDto findById(Long id) {
        UserDto dto = super.findById(id);
        dto.setRoles(findRoles(id));
        return dto;
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected Class<UserDto> getDtoClass() {
        return UserDto.class;
    }

    private Set<String> assignRoles(Long userId, Set<String> roleNames) {
        userRoleRepository.deleteByUserId(userId);
        if (roleNames == null || roleNames.isEmpty()) {
            return findRoles(userId);
        }
        Set<String> assigned = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleService.ensureRole(roleName, roleName);
            UserRole mapping = UserRole.builder()
                    .userId(userId)
                    .roleId(role.getId())
                    .build();
            userRoleRepository.save(mapping);
            assigned.add(role.getName());
        }
        return assigned;
    }

    private UserDto toDto(User user, Set<String> roles) {
        UserDto dto = mapper.map(user, UserDto.class);
        dto.setRoles(roles);
        return dto;
    }

    private Specification<User> notDeletedOrNullSpec() {
        return (root, query, cb) -> cb.or(
                cb.isFalse(root.get("isDeleted")),
                cb.isNull(root.get("isDeleted"))
        );
    }
}
