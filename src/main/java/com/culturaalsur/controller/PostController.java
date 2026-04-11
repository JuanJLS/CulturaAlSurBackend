package com.culturaalsur.controller;

import com.culturaalsur.dto.*;
import com.culturaalsur.service.CommentService;
import com.culturaalsur.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    // Public: anyone can read posts
    @GetMapping
    public ResponseEntity<List<PostSummaryDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    // Users: authenticated users can comment
    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long id,
            @RequestBody @Valid CreateCommentRequest req,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(id, req, principal.getName()));
    }

    // Admin only: create post
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostDetailDto> createPost(
            @RequestBody @Valid CreatePostRequest req,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(req, principal.getName()));
    }
}