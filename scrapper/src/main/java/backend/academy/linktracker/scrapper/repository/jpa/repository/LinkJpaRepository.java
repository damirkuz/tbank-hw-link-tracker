package backend.academy.linktracker.scrapper.repository.jpa.repository;

import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.jpa.entity.LinkEntity;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LinkJpaRepository extends JpaRepository<LinkEntity, Long> {

    Optional<LinkEntity> findByUriAndTrackedResource(URI uri, TrackedResource trackedResource);

    @Query("""
        select l
        from LinkEntity l
        where (l.nextCheckAt is null or l.nextCheckAt <= :before)
          and l.id > :lastSeenId
        order by l.id asc
        """)
    List<LinkEntity> findLinksForUpdate(
            @Param("before") OffsetDateTime before, @Param("lastSeenId") long lastSeenId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update LinkEntity l
        set l.lastUpdate = :lastUpdate,
            l.nextCheckAt = :nextCheckAt
        where l.id = :linkId
        """)
    int updateCheckState(
            @Param("linkId") long linkId,
            @Param("lastUpdate") Instant lastUpdate,
            @Param("nextCheckAt") OffsetDateTime nextCheckAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update LinkEntity l
        set l.nextCheckAt = :nextCheckAt
        where l.id = :linkId
        """)
    int updateNextCheckAt(@Param("linkId") long linkId, @Param("nextCheckAt") OffsetDateTime nextCheckAt);
}
