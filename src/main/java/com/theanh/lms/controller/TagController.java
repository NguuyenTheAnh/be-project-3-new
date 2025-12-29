package com.theanh.lms.controller;

import com.theanh.common.dto.ResponseDto;
import com.theanh.common.util.ResponseConfig;
import com.theanh.lms.dto.TagDto;
import com.theanh.lms.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<ResponseDto<List<TagDto>>> list() {
        return ResponseConfig.success(tagService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<TagDto>> get(@PathVariable Long id) {
        return ResponseConfig.success(tagService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<TagDto>> create(@Valid @RequestBody TagDto dto) {
        return ResponseConfig.success(tagService.saveObject(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<TagDto>> update(@PathVariable Long id, @Valid @RequestBody TagDto dto) {
        dto.setId(id);
        return ResponseConfig.success(tagService.saveObject(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        tagService.deleteById(id);
        return ResponseConfig.success("deleted");
    }
}
