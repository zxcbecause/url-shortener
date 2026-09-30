package io.github.zxcbecause.shortener.link;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByCode(String code);

    boolean existsByCode(String code);

    /**
     * Atomic increment in the database, so concurrent redirects never lose a click
     * (a read-modify-write in Java would).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Link l set l.clicks = l.clicks + 1, l.lastAccessedAt = :now where l.id = :id")
    int registerClick(@Param("id") Long id, @Param("now") Instant now);
}
