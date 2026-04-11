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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final AppUserRepository userRepository;

    /**
     * Returns all posts, optionally filtered by category or tag.
     * Only one filter is applied at a time; category takes precedence over tag.
     */
    @Transactional(readOnly = true)
    public List<PostSummaryDto> getAllPosts(String category, String tag) {
        List<Post> posts;
        if (StringUtils.hasText(category)) {
            posts = postRepository.findByCategoryOrderByCreatedAtDesc(category);
        } else if (StringUtils.hasText(tag)) {
            posts = postRepository.findByTagOrderByCreatedAtDesc(tag);
        } else {
            posts = postRepository.findAllByOrderByCreatedAtDesc();
        }
        return posts.stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public PostDetailDto getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Post not found with id: " + id));
        return toDetail(post);
    }

    @Transactional
    public PostDetailDto createPost(CreatePostRequest req, String authorUsername) {
        AppUser author = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "User not found: " + authorUsername));

        Post post = Post.builder()
                .title(req.getTitle())
                .body(req.getBody())
                .category(req.getCategory() != null ? req.getCategory() : "general")
                .tag(req.getTag())
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
                .category(post.getCategory())
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
                .category(post.getCategory())
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
