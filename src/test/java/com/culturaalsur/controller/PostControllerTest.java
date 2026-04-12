package com.culturaalsur.controller;

import com.culturaalsur.dto.*;
import com.culturaalsur.service.CommentService;
import com.culturaalsur.service.PostService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CommentService commentService;

    private MockMvc mockMvc;

    // ── Sample DTOs ──────────────────────────────────────────────────────────────

    private final PostSummaryDto summaryDto = PostSummaryDto.builder()
            .id(1L).title("Music Review").category("music").tag("featured")
            .createdAt("2024-01-15T10:00:00").authorUsername("juanjls")
            .commentCount(0).build();

    private final PostDetailDto detailDto = PostDetailDto.builder()
            .id(1L).title("Music Review").body("Great album").category("music")
            .tag("featured").createdAt("2024-01-15T10:00:00").authorUsername("juanjls")
            .media(List.of()).comments(List.of()).build();

    private final CommentDto commentDto = CommentDto.builder()
            .id(5L).content("Nice post!").authorUsername("juanjls")
            .createdAt("2024-01-16T08:00:00").build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ── GET /api/posts ────────────────────────────────────────────────────────────

    @Test
    void getAllPosts_noFilter_returns200WithList() throws Exception {
        when(postService.getAllPosts(isNull(), isNull())).thenReturn(List.of(summaryDto));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Music Review"))
                .andExpect(jsonPath("$[0].category").value("music"));
    }

    @Test
    void getAllPosts_filterByCategory_returns200() throws Exception {
        when(postService.getAllPosts(eq("music"), isNull())).thenReturn(List.of(summaryDto));

        mockMvc.perform(get("/api/posts").param("category", "music"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("music"));
    }

    @Test
    void getAllPosts_filterByTag_returns200() throws Exception {
        when(postService.getAllPosts(isNull(), eq("featured"))).thenReturn(List.of(summaryDto));

        mockMvc.perform(get("/api/posts").param("tag", "featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tag").value("featured"));
    }

    @Test
    void getAllPosts_empty_returns200WithEmptyArray() throws Exception {
        when(postService.getAllPosts(isNull(), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/posts/{id} ───────────────────────────────────────────────────────

    @Test
    void getPost_existingId_returns200WithDetail() throws Exception {
        when(postService.getPost(1L)).thenReturn(detailDto);

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Music Review"))
                .andExpect(jsonPath("$.body").value("Great album"));
    }

    @Test
    void getPost_nonExistingId_returns404() throws Exception {
        when(postService.getPost(99L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Post not found"));

        mockMvc.perform(get("/api/posts/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/posts/{id}/comments ─────────────────────────────────────────────

    @Test
    void getComments_existingPost_returns200WithList() throws Exception {
        when(commentService.getCommentsByPost(1L)).thenReturn(List.of(commentDto));

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Nice post!"))
                .andExpect(jsonPath("$[0].authorUsername").value("juanjls"));
    }

    @Test
    void getComments_nonExistingPost_returns404() throws Exception {
        when(commentService.getCommentsByPost(99L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Post not found"));

        mockMvc.perform(get("/api/posts/99/comments"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/posts/{id}/comments ────────────────────────────────────────────

    @Test
    void addComment_notAuthenticated_returns4xx() throws Exception {
        String body = objectMapper.writeValueAsString(new CreateCommentRequest("Hello"));

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "juanjls")
    void addComment_authenticated_returns201() throws Exception {
        when(commentService.addComment(eq(1L), any(CreateCommentRequest.class), eq("juanjls")))
                .thenReturn(commentDto);

        String body = objectMapper.writeValueAsString(new CreateCommentRequest("Nice post!"));

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Nice post!"));
    }

    @Test
    @WithMockUser(username = "juanjls")
    void addComment_blankContent_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(new CreateCommentRequest(""));

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── POST /api/posts ───────────────────────────────────────────────────────────

    @Test
    void createPost_notAuthenticated_returns4xx() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreatePostRequest("Title", "Body", "music", null, null));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "juanjls", roles = "USER")
    void createPost_notAdmin_returns403() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreatePostRequest("Title", "Body", "music", null, null));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createPost_asAdmin_returns201() throws Exception {
        when(postService.createPost(any(CreatePostRequest.class), eq("admin")))
                .thenReturn(detailDto);

        String body = objectMapper.writeValueAsString(
                new CreatePostRequest("Music Review", "Great album", "music", "featured", null));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Music Review"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createPost_blankTitle_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreatePostRequest("", "Body", "music", null, null));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
