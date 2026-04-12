package com.culturaalsur.service;

import com.culturaalsur.dto.CommentDto;
import com.culturaalsur.dto.CreateCommentRequest;
import com.culturaalsur.entity.AppUser;
import com.culturaalsur.entity.Comment;
import com.culturaalsur.entity.Post;
import com.culturaalsur.repository.AppUserRepository;
import com.culturaalsur.repository.CommentRepository;
import com.culturaalsur.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private AppUser author() {
        return AppUser.builder()
                .username("juanjls")
                .email("juan@example.com")
                .password("hashed")
                .build();
    }

    private Post post() {
        return Post.builder().title("A post").author(author()).build();
    }

    // ── addComment ────────────────────────────────────────────────────────────────

    @Test
    void addComment_validRequest_returnsDto() {
        AppUser author = author();
        Post post = post();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("juanjls")).thenReturn(Optional.of(author));

        Comment saved = Comment.builder()
                .id(10L)
                .content("Great post!")
                .post(post)
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentDto dto = commentService.addComment(1L, new CreateCommentRequest("Great post!"), "juanjls");

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getContent()).isEqualTo("Great post!");
        assertThat(dto.getAuthorUsername()).isEqualTo("juanjls");
        assertThat(dto.getCreatedAt()).isNotNull();
    }

    @Test
    void addComment_postNotFound_throwsNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                commentService.addComment(99L, new CreateCommentRequest("hi"), "juanjls"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void addComment_userNotFound_throwsUnauthorized() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post()));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                commentService.addComment(1L, new CreateCommentRequest("hi"), "ghost"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void addComment_nullAuthorInSaved_showsAnonymous() {
        Post post = post();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("juanjls")).thenReturn(Optional.of(author()));

        Comment saved = Comment.builder()
                .id(1L).content("Hi").post(post).author(null)
                .createdAt(LocalDateTime.now()).build();
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentDto dto = commentService.addComment(1L, new CreateCommentRequest("Hi"), "juanjls");

        assertThat(dto.getAuthorUsername()).isEqualTo("Anonymous");
    }

    // ── getCommentsByPost ─────────────────────────────────────────────────────────

    @Test
    void getCommentsByPost_existingPost_returnsCommentList() {
        AppUser author = author();
        Post post = post();
        when(postRepository.existsById(1L)).thenReturn(true);

        Comment c1 = Comment.builder()
                .id(1L).content("First!").post(post).author(author)
                .createdAt(LocalDateTime.now()).build();
        Comment c2 = Comment.builder()
                .id(2L).content("Second!").post(post).author(author)
                .createdAt(LocalDateTime.now().plusSeconds(5)).build();
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(c1, c2));

        List<CommentDto> result = commentService.getCommentsByPost(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("First!");
        assertThat(result.get(1).getContent()).isEqualTo("Second!");
    }

    @Test
    void getCommentsByPost_emptyComments_returnsEmptyList() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        assertThat(commentService.getCommentsByPost(1L)).isEmpty();
    }

    @Test
    void getCommentsByPost_nonExistingPost_throwsNotFound() {
        when(postRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> commentService.getCommentsByPost(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Post not found");
    }
}
