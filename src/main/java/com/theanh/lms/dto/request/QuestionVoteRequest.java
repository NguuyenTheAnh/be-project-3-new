package com.theanh.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionVoteRequest {
    @NotBlank
    private String voteType; // UP or DOWN
}
