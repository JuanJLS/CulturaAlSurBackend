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
class PostTest {

    @Autowired
    private EntityManager em;

    private AppUser author;

    @BeforeEach
    void setUp() {
        author = AppUser.builder()
                .username("juanjls")
                .email("juan@example.com")
                .password("secret")
                .build();
        em.persist(author);
        em.flush();
    }

    @Test
    void savesAndRetrievesPost() {
        Post post = Post.builder()
                .title("My first post")
                .body("Hello world")
                .author(author)
                .build();

        em.persist(post);
        em.flush();
        em.clear();

        Post found = em.find(Post.class, post.getId());
        assertThat(found.getTitle()).isEqualTo("My first post");
        assertThat(found.getBody()).isEqualTo("Hello world");
    }

    @Test
    void defaultCategoryIsGeneral() {
        Post post = Post.builder()
                .title("Post without category")
                .author(author)
                .build();

        em.persist(post);
        em.flush();
        em.clear();

        Post found = em.find(Post.class, post.getId());
        assertThat(found.getCategory()).isEqualTo("general");
    }

    @Test
    void createdAtIsSetAutomatically() {
        Post post = Post.builder()
                .title("Timestamped post")
                .author(author)
                .build();

        em.persist(post);
        em.flush();
        em.clear();

        Post found = em.find(Post.class, post.getId());
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void authorRelationshipIsPersisted() {
        Post post = Post.builder()
                .title("Post with author")
                .author(author)
                .build();

        em.persist(post);
        em.flush();
        em.clear();

        Post found = em.find(Post.class, post.getId());
        assertThat(found.getAuthor().getUsername()).isEqualTo("juanjls");
    }

    @Test
    void mediaListIsInitiallyEmpty() {
        Post post = Post.builder()
                .title("Post with no media")
                .author(author)
                .build();

        em.persist(post);
        em.flush();
        em.clear();

        Post found = em.find(Post.class, post.getId());
        assertThat(found.getMedia()).isEmpty();
    }

    @Test
    void commentsListIsInitiallyEmpty() {
        Post post = Post.builder()
                .title("Post with no comments")
                .author(author)
                .build();

        em.persist(post);
        em.flush();
        em.clear();

        Post found = em.find(Post.class, post.getId());
        assertThat(found.getComments()).isEmpty();
    }
}
