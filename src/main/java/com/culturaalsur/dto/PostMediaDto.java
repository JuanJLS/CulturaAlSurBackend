package com.culturaalsur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostMediaDto {
    private String url;
    private String mediaType;   // "IMAGE" or "VIDEO"
    private int position;
    private String sizeHint;
    private String align;
    private String caption;
}
