package com.culturaalsur.service;

import com.culturaalsur.dto.CreatePostRequest;
import com.culturaalsur.dto.PostDetailDto;
import com.culturaalsur.dto.PostMediaDto;
import com.culturaalsur.dto.PostSummaryDto;
import com.culturaalsur.entity.AppUser;
import com.culturaalsur.entity.Post;
import com.culturaalsur.entity.PostMedia;
import com.culturaalsur.repository.AppUserRepository;
import com.culturaalsur.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private PostService postService;

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private AppUser author() {
        return AppUser.builder()
                .username("juanjls")
                .email("juan@example.com")
                .password("hashed")
                .build();
    }

    private Post musicPost(AppUser author) {
        return Post.builder()
                .title("Music Review")
                .body("Great album")
                .category("music")
                .tag("featured")
                .author(author)
                .build();
    }

    // ── getAllPosts ───────────────────────────────────────────────────────────────

    @Test
    void getAllPosts_noFilter_returnsAllPosts() {
        AppUser author = author();
        when(postRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(musicPost(author)));

        List<PostSummaryDto> result = postService.getAllPosts(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Music Review");
    }

    @Test
    void getAllPosts_emptyRepository_returnsEmptyList() {
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        assertThat(postService.getAllPosts(null, null)).isEmpty();
    }

    @Test
    void getAllPosts_filterByCategory_usesCategoryQuery() {
        AppUser author = author();
        when(postRepository.findByCategoryOrderByCreatedAtDesc("music"))
                .thenReturn(List.of(musicPost(author)));

        List<PostSummaryDto> result = postService.getAllPosts("music", null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCategory()).isEqualTo("music");
    }

    @Test
    void getAllPosts_filterByTag_usesTagQuery() {
        AppUser author = author();
        when(postRepository.findByTagOrderByCreatedAtDesc("featured"))
                .thenReturn(List.of(musicPost(author)));

        List<PostSummaryDto> result = postService.getAllPosts(null, "featured");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTag()).isEqualTo("featured");
    }

    @Test
    void getAllPosts_categoryTakesPrecedenceOverTag() {
        AppUser author = author();
        when(postRepository.findByCategoryOrderByCreatedAtDesc("music"))
                .thenReturn(List.of(musicPost(author)));
        // tag query should NOT be called

        List<PostSummaryDto> result = postService.getAllPosts("music", "featured");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCategory()).isEqualTo("music");
    }

    @Test
    void getAllPosts_nullAuthor_showsAnonymous() {
        Post noAuthor = Post.builder().title("Anon Post").category("general").build();
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(noAuthor));

        List<PostSummaryDto> result = postService.getAllPosts(null, null);

        assertThat(result.getFirst().getAuthorUsername()).isEqualTo("Anonymous");
    }

    @Test
    void getAllPosts_postWithImage_populatesFirstImageUrl() {
        AppUser author = author();
        Post post = musicPost(author);
        PostMedia img = PostMedia.builder()
                .post(post).url("https://example.com/cover.jpg")
                .mediaType("IMAGE").position(0).build();
        post.getMedia().add(img);
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(post));

        PostSummaryDto dto = postService.getAllPosts(null, null).getFirst();

        assertThat(dto.getFirstImageUrl()).isEqualTo("https://example.com/cover.jpg");
    }

    @Test
    void getAllPosts_postWithOnlyVideo_firstImageUrlIsNull() {
        AppUser author = author();
        Post post = musicPost(author);
        PostMedia video = PostMedia.builder()
                .post(post).url("https://example.com/clip.mp4")
                .mediaType("VIDEO").position(0).build();
        post.getMedia().add(video);
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(post));

        PostSummaryDto dto = postService.getAllPosts(null, null).getFirst();

        assertThat(dto.getFirstImageUrl()).isNull();
    }

    @Test
    void getAllPosts_commentCountIsCorrect() {
        AppUser author = author();
        Post post = musicPost(author);
        // Add two comments via entity relationships
        post.getComments().add(
                com.culturaalsur.entity.Comment.builder()
                        .content("Nice!").post(post).author(author).build());
        post.getComments().add(
                com.culturaalsur.entity.Comment.builder()
                        .content("Great!").post(post).author(author).build());
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(post));

        assertThat(postService.getAllPosts(null, null).getFirst().getCommentCount()).isEqualTo(2);
    }

    // ── getPost ───────────────────────────────────────────────────────────────────

    @Test
    void getPost_existingId_returnsFullDetail() {
        AppUser author = author();
        Post post = musicPost(author);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostDetailDto dto = postService.getPost(1L);

        assertThat(dto.getTitle()).isEqualTo("Music Review");
        assertThat(dto.getBody()).isEqualTo("Great album");
        assertThat(dto.getAuthorUsername()).isEqualTo("juanjls");
        assertThat(dto.getCategory()).isEqualTo("music");
    }

    @Test
    void getPost_nonExistingId_throwsNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void getPost_postWithMedia_includesMediaInResponse() {
        AppUser author = author();
        Post post = musicPost(author);
        PostMedia img = PostMedia.builder()
                .post(post).url("https://example.com/photo.jpg")
                .mediaType("IMAGE").position(0).sizeHint("large").build();
        post.getMedia().add(img);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostDetailDto dto = postService.getPost(1L);

        assertThat(dto.getMedia()).hasSize(1);
        assertThat(dto.getMedia().getFirst().getUrl()).isEqualTo("https://example.com/photo.jpg");
        assertThat(dto.getMedia().getFirst().getSizeHint()).isEqualTo("large");
    }

    @Test
    void getPost_nullAuthor_showsAnonymous() {
        Post post = Post.builder().title("No author post").build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThat(postService.getPost(1L).getAuthorUsername()).isEqualTo("Anonymous");
    }

    // ── createPost ────────────────────────────────────────────────────────────────

    @Test
    void createPost_validRequest_savesAndReturnsDetail() {
        AppUser author = author();
        when(userRepository.findByUsername("juanjls")).thenReturn(Optional.of(author));

        CreatePostRequest req =
                new CreatePostRequest("New Title", "Body content", "music", "tag", null);

        Post saved = Post.builder()
                .title("New Title").body("Body content")
                .category("music").tag("tag").author(author).build();
        when(postRepository.save(any(Post.class))).thenReturn(saved);

        PostDetailDto dto = postService.createPost(req, "juanjls");

        assertThat(dto.getTitle()).isEqualTo("New Title");
        assertThat(dto.getCategory()).isEqualTo("music");
        assertThat(dto.getTag()).isEqualTo("tag");
    }

    @Test
    void createPost_nullCategory_defaultsToGeneral() {
        AppUser author = author();
        when(userRepository.findByUsername("juanjls")).thenReturn(Optional.of(author));

        CreatePostRequest req = new CreatePostRequest("Title", "Body", null, null, null);

        Post saved = Post.builder()
                .title("Title").body("Body").category("general").author(author).build();
        when(postRepository.save(any(Post.class))).thenReturn(saved);

        assertThat(postService.createPost(req, "juanjls").getCategory()).isEqualTo("general");
    }

    @Test
    void createPost_unknownAuthor_throwsUnauthorized() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        CreatePostRequest req = new CreatePostRequest("Title", "Body", null, null, null);

        assertThatThrownBy(() -> postService.createPost(req, "ghost"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createPost_withMedia_includedInSavedPost() {
        AppUser author = author();
        when(userRepository.findByUsername("juanjls")).thenReturn(Optional.of(author));

        PostMediaDto mediaItem = PostMediaDto.builder()
                .url("https://example.com/img.jpg")
                .mediaType("IMAGE").position(0).sizeHint("medium").build();
        CreatePostRequest req =
                new CreatePostRequest("Title", "Body", "general", null, List.of(mediaItem));

        // The saved post returned by the mock includes the media we added
        Post saved = Post.builder()
                .title("Title").body("Body").category("general").author(author).build();
        PostMedia pm = PostMedia.builder()
                .post(saved).url("https://example.com/img.jpg")
                .mediaType("IMAGE").position(0).sizeHint("medium").build();
        saved.getMedia().add(pm);
        when(postRepository.save(any(Post.class))).thenReturn(saved);

        PostDetailDto dto = postService.createPost(req, "juanjls");

        assertThat(dto.getMedia()).hasSize(1);
        assertThat(dto.getMedia().getFirst().getSizeHint()).isEqualTo("medium");
    }

    @Test
    void createPost_withMediaCaptionAndAlign_includedInSavedPost() {
        AppUser author = author();
        when(userRepository.findByUsername("juanjls")).thenReturn(Optional.of(author));

        PostMediaDto mediaItem = PostMediaDto.builder()
                .url("https://example.com/img.jpg")
                .mediaType("IMAGE").position(0).sizeHint("small")
                .align("center").caption("A nice photo").build();
        CreatePostRequest req =
                new CreatePostRequest("Title", "Body", "general", null, List.of(mediaItem));

        Post saved = Post.builder()
                .title("Title").body("Body").category("general").author(author).build();
        PostMedia pm = PostMedia.builder()
                .post(saved).url("https://example.com/img.jpg")
                .mediaType("IMAGE").position(0).sizeHint("small")
                .align("center").caption("A nice photo").build();
        saved.getMedia().add(pm);
        when(postRepository.save(any(Post.class))).thenReturn(saved);

        PostDetailDto dto = postService.createPost(req, "juanjls");

        assertThat(dto.getMedia()).hasSize(1);
        assertThat(dto.getMedia().getFirst().getAlign()).isEqualTo("center");
        assertThat(dto.getMedia().getFirst().getCaption()).isEqualTo("A nice photo");
        assertThat(dto.getMedia().getFirst().getSizeHint()).isEqualTo("small");
    }

    // ── updatePost ────────────────────────────────────────────────────────────────

    @Test
    void updatePost_existingPost_updatesFieldsAndReturnsDetail() {
        AppUser author = author();
        Post existing = Post.builder()
                .title("Old Title").body("Old body").category("general")
                .tag("old").author(author).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(existing));

        CreatePostRequest req =
                new CreatePostRequest("New Title", "New body", "music", "new-tag", null);

        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostDetailDto dto = postService.updatePost(1L, req);

        assertThat(dto.getTitle()).isEqualTo("New Title");
        assertThat(dto.getBody()).isEqualTo("New body");
        assertThat(dto.getCategory()).isEqualTo("music");
        assertThat(dto.getTag()).isEqualTo("new-tag");
    }

    @Test
    void updatePost_nonExistingId_throwsNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        CreatePostRequest req = new CreatePostRequest("Title", "Body", null, null, null);

        assertThatThrownBy(() -> postService.updatePost(99L, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void updatePost_nullCategory_defaultsToGeneral() {
        AppUser author = author();
        Post existing = Post.builder()
                .title("Title").body("Body").category("music").author(author).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(existing));

        CreatePostRequest req = new CreatePostRequest("Title", "Body", null, null, null);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(postService.updatePost(1L, req).getCategory()).isEqualTo("general");
    }

    @Test
    void updatePost_replacesMedia() {
        AppUser author = author();
        Post existing = Post.builder()
                .title("Title").body("Body").category("general").author(author).build();
        PostMedia oldMedia = PostMedia.builder()
                .post(existing).url("https://example.com/old.jpg")
                .mediaType("IMAGE").position(0).sizeHint("large").build();
        existing.getMedia().add(oldMedia);
        when(postRepository.findById(1L)).thenReturn(Optional.of(existing));

        PostMediaDto newMediaDto = PostMediaDto.builder()
                .url("https://example.com/new.jpg")
                .mediaType("IMAGE").position(0).sizeHint("small")
                .align("left").caption("Updated caption").build();
        CreatePostRequest req =
                new CreatePostRequest("Title", "Body", "general", null, List.of(newMediaDto));

        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostDetailDto dto = postService.updatePost(1L, req);

        assertThat(dto.getMedia()).hasSize(1);
        assertThat(dto.getMedia().getFirst().getUrl()).isEqualTo("https://example.com/new.jpg");
        assertThat(dto.getMedia().getFirst().getSizeHint()).isEqualTo("small");
        assertThat(dto.getMedia().getFirst().getAlign()).isEqualTo("left");
        assertThat(dto.getMedia().getFirst().getCaption()).isEqualTo("Updated caption");
    }

    @Test
    void updatePost_withNullMedia_clearsExistingMedia() {
        AppUser author = author();
        Post existing = Post.builder()
                .title("Title").body("Body").category("general").author(author).build();
        existing.getMedia().add(PostMedia.builder()
                .post(existing).url("https://example.com/img.jpg")
                .mediaType("IMAGE").position(0).build());
        when(postRepository.findById(1L)).thenReturn(Optional.of(existing));

        CreatePostRequest req = new CreatePostRequest("Title", "Body", "general", null, null);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostDetailDto dto = postService.updatePost(1L, req);

        assertThat(dto.getMedia()).isEmpty();
    }

    @Test
    void updatePost_preservesSizeHintDefault() {
        AppUser author = author();
        Post existing = Post.builder()
                .title("Title").body("Body").category("general").author(author).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(existing));

        PostMediaDto mediaDto = PostMediaDto.builder()
                .url("https://example.com/img.jpg")
                .mediaType("IMAGE").position(0).sizeHint(null).build();
        CreatePostRequest req =
                new CreatePostRequest("Title", "Body", "general", null, List.of(mediaDto));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostDetailDto dto = postService.updatePost(1L, req);

        assertThat(dto.getMedia().getFirst().getSizeHint()).isEqualTo("large");
    }
}
