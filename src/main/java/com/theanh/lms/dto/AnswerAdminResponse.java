package com.theanh.lms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnswerAdminResponse extends AnswerDto {
    private String createdUser;
}
