package com.memora.modules.gamification.repository;

import com.memora.modules.gamification.entity.XpTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link XpTransaction}.
 */
@Repository
public interface XpTransactionRepository extends JpaRepository<XpTransaction, Long> {

    List<XpTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<XpTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);
}
