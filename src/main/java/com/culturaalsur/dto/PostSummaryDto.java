package com.culturaalsur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// PostSummaryDto.java
// Used in the list endpoint — only the fields needed for a card thumbnail.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryDto {
    private Long id;
    private String title;
    private String category;         // drives ngClass on the Angular card
    private String tag;
    private String createdAt;        // ISO-8601 string, formatted in PostService
    private String authorUsername;
    private String firstImageUrl;
    private int commentCount;
}
