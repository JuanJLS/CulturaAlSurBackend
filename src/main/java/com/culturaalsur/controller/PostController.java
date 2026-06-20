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

    // ── Public read endpoints ──────────────────────────────────────────────────

    /**
     * GET /api/posts
     * GET /api/posts?category=music
     * GET /api/posts?tag=featured
     *
     * Returns a paginated-ready list of post summaries. Angular can filter by
     * passing ?category= or ?tag= as query parameters; only one is applied at a
     * time (category takes precedence).
     */
    @GetMapping
    public ResponseEntity<List<PostSummaryDto>> getAllPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag) {
        return ResponseEntity.ok(postService.getAllPosts(category, tag));
    }

    /** GET /api/posts/{id} — full post with body, media and comments. */
    @GetMapping("/{id}")
    public ResponseEntity<PostDetailDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    /**
     * GET /api/posts/{id}/comments — paginated-ready comment list for a post.
     * Public so Angular can display comments without requiring login.
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentsByPost(id));
    }

    // ── Authenticated endpoints ────────────────────────────────────────────────

    /** POST /api/posts/{id}/comments — add a comment; requires a valid JWT. */
    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long id,
            @RequestBody @Valid CreateCommentRequest req,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(id, req, principal.getName()));
    }

    // ── Admin-only endpoints ───────────────────────────────────────────────────

    /** POST /api/posts — create a post; requires ROLE_ADMIN. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostDetailDto> createPost(
            @RequestBody @Valid CreatePostRequest req,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(req, principal.getName()));
    }

    /** PUT /api/posts/{id} — edit a post; requires ROLE_ADMIN. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostDetailDto> updatePost(
            @PathVariable Long id,
            @RequestBody @Valid CreatePostRequest req) {
        return ResponseEntity.ok(postService.updatePost(id, req));
    }
}
