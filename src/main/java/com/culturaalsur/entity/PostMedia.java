package com.culturaalsur.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post_media")
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "media_type")
    @Builder.Default
    private String mediaType = "IMAGE";

    private int position;

    @Column(name = "size_hint", length = 10)
    @Builder.Default
    private String sizeHint = "large";

    @Column(name = "align", length = 10)
    @Builder.Default
    private String align = "none";
}
