package com.culturaalsur.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostMediaTest {

    @Autowired
    private EntityManager em;

    private Post post;

    @BeforeEach
    void setUp() {
        AppUser author = AppUser.builder()
                .username("juanjls")
                .email("juan@example.com")
                .password("secret")
                .build();
        em.persist(author);

        post = Post.builder()
                .title("Post with media")
                .author(author)
                .build();
        em.persist(post);
        em.flush();
    }

    @Test
    void savesAndRetrievesMedia() {
        PostMedia media = PostMedia.builder()
                .post(post)
                .url("https://example.com/image.jpg")
                .position(0)
                .build();

        em.persist(media);
        em.flush();
        em.clear();

        PostMedia found = em.find(PostMedia.class, media.getId());
        assertThat(found.getUrl()).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    void defaultMediaTypeIsImage() {
        PostMedia media = PostMedia.builder()
                .post(post)
                .url("https://example.com/image.jpg")
                .position(0)
                .build();

        em.persist(media);
        em.flush();
        em.clear();

        PostMedia found = em.find(PostMedia.class, media.getId());
        assertThat(found.getMediaType()).isEqualTo("IMAGE");
    }

    @Test
    void customMediaTypeIsPersisted() {
        PostMedia media = PostMedia.builder()
                .post(post)
                .url("https://example.com/video.mp4")
                .mediaType("VIDEO")
                .position(1)
                .build();

        em.persist(media);
        em.flush();
        em.clear();

        PostMedia found = em.find(PostMedia.class, media.getId());
        assertThat(found.getMediaType()).isEqualTo("VIDEO");
    }

    @Test
    void postRelationshipIsPersisted() {
        PostMedia media = PostMedia.builder()
                .post(post)
                .url("https://example.com/image.jpg")
                .position(0)
                .build();

        em.persist(media);
        em.flush();
        em.clear();

        PostMedia found = em.find(PostMedia.class, media.getId());
        assertThat(found.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    void positionIsPersisted() {
        PostMedia media = PostMedia.builder()
                .post(post)
                .url("https://example.com/image.jpg")
                .position(3)
                .build();

        em.persist(media);
        em.flush();
        em.clear();

        PostMedia found = em.find(PostMedia.class, media.getId());
        assertThat(found.getPosition()).isEqualTo(3);
    }

    @Test
    void captionIsPersisted() {
        PostMedia media = PostMedia.builder()
                .post(post)
                .url("https://example.com/image.jpg")
                .position(0)
                .caption("A beautiful sunset")
                .build();

        em.persist(media);
        em.flush();
        em.clear();

        PostMedia found = em.find(PostMedia.class, media.getId());
        assertThat(found.getCaption()).isEqualTo("A beautiful sunset");
    }

    @Test
    void captionDefaultsToNull() {
        PostMedia media = PostMedia.builder()
                .post(post)
                .url("https://example.com/image.jpg")
                .position(0)
                .build();

        em.persist(media);
        em.flush();
        em.clear();

        PostMedia found = em.find(PostMedia.class, media.getId());
        assertThat(found.getCaption()).isNull();
    }

    @Test
    void alignIsPersisted() {
        PostMedia media = PostMedia.builder()
                .post(post)
                .url("https://example.com/image.jpg")
                .position(0)
                .align("center")
                .build();

        em.persist(media);
        em.flush();
        em.clear();

        PostMedia found = em.find(PostMedia.class, media.getId());
        assertThat(found.getAlign()).isEqualTo("center");
    }

    @Test
    void alignDefaultsToNone() {
        PostMedia media = PostMedia.builder()
                .post(post)
                .url("https://example.com/image.jpg")
                .position(0)
                .build();

        em.persist(media);
        em.flush();
        em.clear();

        PostMedia found = em.find(PostMedia.class, media.getId());
        assertThat(found.getAlign()).isEqualTo("none");
    }

    @Test
    void sizeHintIsPersisted() {
        PostMedia media = PostMedia.builder()
                .post(post)
                .url("https://example.com/image.jpg")
                .position(0)
                .sizeHint("small")
                .build();

        em.persist(media);
        em.flush();
        em.clear();

        PostMedia found = em.find(PostMedia.class, media.getId());
        assertThat(found.getSizeHint()).isEqualTo("small");
    }
}
