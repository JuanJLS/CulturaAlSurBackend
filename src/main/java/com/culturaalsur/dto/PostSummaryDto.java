package com.culturaalsur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryDto {
    private Long id;
    private String title;
    private String style;
    private LocalDateTime createdAt;
    private String authorUsername;
    private String firstImageUrl;   // thumbnail
    private int commentCount;
}
