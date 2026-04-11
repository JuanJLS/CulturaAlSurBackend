package com.culturaalsur.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class AppUserTest {

    @Autowired
    private EntityManager em;

    @Test
    void savesAndRetrievesUser() {
        AppUser user = AppUser.builder()
                .username("juanjls")
                .email("juan@example.com")
                .password("secret")
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        AppUser found = em.find(AppUser.class, user.getId());
        assertThat(found.getUsername()).isEqualTo("juanjls");
        assertThat(found.getEmail()).isEqualTo("juan@example.com");
    }

    @Test
    void defaultRoleIsRoleUser() {
        AppUser user = AppUser.builder()
                .username("juanjls")
                .email("juan@example.com")
                .password("secret")
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        AppUser found = em.find(AppUser.class, user.getId());
        assertThat(found.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void customRoleIsPersisted() {
        AppUser admin = AppUser.builder()
                .username("admin")
                .email("admin@example.com")
                .password("secret")
                .role("ROLE_ADMIN")
                .build();

        em.persist(admin);
        em.flush();
        em.clear();

        AppUser found = em.find(AppUser.class, admin.getId());
        assertThat(found.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void duplicateUsernameFails() {
        em.persist(AppUser.builder()
                .username("juanjls")
                .email("first@example.com")
                .password("secret")
                .build());
        em.flush();

        assertThatThrownBy(() -> {
            em.persist(AppUser.builder()
                    .username("juanjls")
                    .email("second@example.com")
                    .password("secret")
                    .build());
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void duplicateEmailFails() {
        em.persist(AppUser.builder()
                .username("user1")
                .email("same@example.com")
                .password("secret")
                .build());
        em.flush();

        assertThatThrownBy(() -> {
            em.persist(AppUser.builder()
                    .username("user2")
                    .email("same@example.com")
                    .password("secret")
                    .build());
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }
}
