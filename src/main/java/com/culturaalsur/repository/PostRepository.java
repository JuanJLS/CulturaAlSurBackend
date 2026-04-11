package com.culturaalsur.repository;

import com.culturaalsur.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByCategoryOrderByCreatedAtDesc(String category);
    List<Post> findByTagOrderByCreatedAtDesc(String tag);
}
