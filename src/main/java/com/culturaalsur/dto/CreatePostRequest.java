package com.culturaalsur.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    @NotBlank
    private String title;

    private String body;

    // Optional — defaults to "general" in the service if left blank.
    private String category;

    // Optional — a post does not need to be tagged at creation time.
    private String tag;

    private List<PostMediaDto> media;
}
