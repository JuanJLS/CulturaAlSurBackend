package com.culturaalsur.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class CommentTest {

    @Autowired
    private EntityManager em;

    private Post post;
    private AppUser author;

    @BeforeEach
    void setUp() {
        author = AppUser.builder()
                .username("juanjls")
                .email("juan@example.com")
                .password("secret")
                .build();
        em.persist(author);

        post = Post.builder()
                .title("Post to comment on")
                .author(author)
                .build();
        em.persist(post);
        em.flush();
    }

    @Test
    void savesAndRetrievesComment() {
        Comment comment = Comment.builder()
                .content("Great post!")
                .post(post)
                .author(author)
                .build();

        em.persist(comment);
        em.flush();
        em.clear();

        Comment found = em.find(Comment.class, comment.getId());
        assertThat(found.getContent()).isEqualTo("Great post!");
    }

    @Test
    void createdAtIsSetAutomatically() {
        Comment comment = Comment.builder()
                .content("Timestamped comment")
                .post(post)
                .author(author)
                .build();

        em.persist(comment);
        em.flush();
        em.clear();

        Comment found = em.find(Comment.class, comment.getId());
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void postRelationshipIsPersisted() {
        Comment comment = Comment.builder()
                .content("Some comment")
                .post(post)
                .author(author)
                .build();

        em.persist(comment);
        em.flush();
        em.clear();

        Comment found = em.find(Comment.class, comment.getId());
        assertThat(found.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    void authorRelationshipIsPersisted() {
        Comment comment = Comment.builder()
                .content("Some comment")
                .post(post)
                .author(author)
                .build();

        em.persist(comment);
        em.flush();
        em.clear();

        Comment found = em.find(Comment.class, comment.getId());
        assertThat(found.getAuthor().getUsername()).isEqualTo("juanjls");
    }

    @Test
    void commentCanHaveNullAuthor() {
        Comment comment = Comment.builder()
                .content("Anonymous comment")
                .post(post)
                .build();

        em.persist(comment);
        em.flush();
        em.clear();

        Comment found = em.find(Comment.class, comment.getId());
        assertThat(found.getAuthor()).isNull();
    }
}
