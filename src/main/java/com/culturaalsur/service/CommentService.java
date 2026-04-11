package com.culturaalsur.service;

import com.culturaalsur.dto.CommentDto;
import com.culturaalsur.dto.CreateCommentRequest;
import com.culturaalsur.entity.AppUser;
import com.culturaalsur.entity.Comment;
import com.culturaalsur.entity.Post;
import com.culturaalsur.repository.AppUserRepository;
import com.culturaalsur.repository.CommentRepository;
import com.culturaalsur.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// CommentService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AppUserRepository userRepository;

    public CommentDto addComment(Long postId, CreateCommentRequest req, String authorUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found with id: " + postId));

        AppUser author = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found: " + authorUsername));

        Comment comment = Comment.builder()
                .content(req.getContent())
                .post(post)
                .author(author)
                .build();

        log.info("User '{}' commenting on post {}", authorUsername, postId);

        return toDto(commentRepository.save(comment));
    }

    public List<CommentDto> getCommentsByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Post not found with id: " + postId);
        }
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .authorUsername(comment.getAuthor() != null
                        ? comment.getAuthor().getUsername() : "Anonymous")
                .build();
    }
}
