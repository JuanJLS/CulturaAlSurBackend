package com.culturaalsur.service;

import com.culturaalsur.dto.*;
import com.culturaalsur.entity.AppUser;
import com.culturaalsur.entity.Comment;
import com.culturaalsur.entity.Post;
import com.culturaalsur.entity.PostMedia;
import com.culturaalsur.repository.AppUserRepository;
import com.culturaalsur.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final AppUserRepository userRepository;

    public List<PostSummaryDto> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public PostDetailDto getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return toDetail(post);
    }

    public PostDetailDto createPost(CreatePostRequest req, String authorUsername) {
        AppUser author = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Post post = Post.builder()
                .title(req.getTitle())
                .body(req.getBody())
                // Fall back to "general" so the entity's @Builder.Default also kicks in.
                // Either way, the frontend always receives a non-null string.
                .category(req.getCategory() != null ? req.getCategory() : "general")
                .tag(req.getTag())   // tag is allowed to be null
                .author(author)
                .build();

        if (req.getMedia() != null) {
            req.getMedia().forEach(m -> {
                PostMedia media = PostMedia.builder()
                        .post(post)
                        .url(m.getUrl())
                        .mediaType(m.getMediaType())
                        .position(m.getPosition())
                        .build();
                post.getMedia().add(media);
            });
        }

        log.info("Creating post '{}' in category '{}' by {}",
                post.getTitle(), post.getCategory(), authorUsername);

        return toDetail(postRepository.save(post));
    }

    // ---- Private mapping helpers ----

    private PostSummaryDto toSummary(Post post) {
        String firstImage = post.getMedia().stream()
                .filter(m -> "IMAGE".equals(m.getMediaType()))
                .findFirst()
                .map(PostMedia::getUrl)
                .orElse(null);

        return PostSummaryDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .category(post.getCategory())   // replaces 'style'
                .tag(post.getTag())
                .createdAt(post.getCreatedAt())
                .authorUsername(post.getAuthor() != null
                        ? post.getAuthor().getUsername() : "Anonymous")
                .firstImageUrl(firstImage)
                .commentCount(post.getComments().size())
                .build();
    }

    private PostDetailDto toDetail(Post post) {
        return PostDetailDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .category(post.getCategory())   // replaces 'style'
                .tag(post.getTag())
                .createdAt(post.getCreatedAt())
                .authorUsername(post.getAuthor() != null
                        ? post.getAuthor().getUsername() : "Anonymous")
                .media(post.getMedia().stream().map(this::toMediaDto).toList())
                .comments(post.getComments().stream().map(this::toCommentDto).toList())
                .build();
    }

    private PostMediaDto toMediaDto(PostMedia m) {
        return PostMediaDto.builder()
                .url(m.getUrl())
                .mediaType(m.getMediaType())
                .position(m.getPosition())
                .build();
    }

    private CommentDto toCommentDto(Comment c) {
        return CommentDto.builder()
                .id(c.getId())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .authorUsername(c.getAuthor() != null
                        ? c.getAuthor().getUsername() : "Anonymous")
                .build();
    }
}