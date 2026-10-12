package com.studentreadiness.repository;

import com.studentreadiness.entity.Certification;
import com.studentreadiness.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByUser(User user);
    List<Certification> findByUserId(Long userId);
    Optional<Certification> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
