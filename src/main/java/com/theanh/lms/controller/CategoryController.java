package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.CategoryDto;
import com.theanh.lms.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ResponseDto<List<CategoryDto>>> list() {
        return ResponseConfig.success(categoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<CategoryDto>> get(@PathVariable Long id) {
        return ResponseConfig.success(categoryService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<CategoryDto>> create(@Valid @RequestBody CategoryDto dto) {
        return ResponseConfig.success(categoryService.saveObject(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<CategoryDto>> update(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        CategoryDto existing = categoryService.findById(id);
        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
        }
        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
            existing.setSlug(dto.getSlug());
        }
        if (dto.getParentId() != null) {
            existing.setParentId(dto.getParentId());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getPosition() != null) {
            existing.setPosition(dto.getPosition());
        }
        existing.setId(id);
        return ResponseConfig.success(categoryService.saveObject(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return ResponseConfig.success("deleted");
    }
}
