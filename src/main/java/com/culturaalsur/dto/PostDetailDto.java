package com.culturaalsur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// PostDetailDto.java
// Used in the single-post endpoint — every field the detail page needs.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailDto {
    private Long id;
    private String title;
    private String body;
    private String category;
    private String tag;
    private LocalDateTime createdAt;
    private String authorUsername;
    private List<PostMediaDto> media;
    private List<CommentDto> comments;
}
