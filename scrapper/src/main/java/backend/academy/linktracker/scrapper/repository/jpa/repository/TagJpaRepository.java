package backend.academy.linktracker.scrapper.repository.jpa.repository;

import backend.academy.linktracker.scrapper.repository.jpa.entity.TagEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagJpaRepository extends JpaRepository<TagEntity, Long> {

    Optional<TagEntity> findByChat_ChatIdAndName(long chatId, String name);

    List<TagEntity> findAllByChat_ChatIdOrderById(long chatId);

    @Query("""
        select l.tag
        from SubscriptionTagLinkEntity l
        where l.subscription.id = :subscriptionId
        order by l.tag.id
        """)
    List<TagEntity> findAllBySubscriptionId(@Param("subscriptionId") long subscriptionId);

    boolean existsByChat_ChatIdAndName(long chatId, String name);
}
