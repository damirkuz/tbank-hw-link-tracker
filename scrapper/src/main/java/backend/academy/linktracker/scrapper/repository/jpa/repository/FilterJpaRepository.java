package backend.academy.linktracker.scrapper.repository.jpa.repository;

import backend.academy.linktracker.scrapper.repository.jpa.entity.FilterEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FilterJpaRepository extends JpaRepository<FilterEntity, Long> {

    Optional<FilterEntity> findByChat_IdAndValue(long chatId, String value);

    List<FilterEntity> findAllByChat_IdOrderById(long chatId);

    @Query("""
        select l.filter
        from SubscriptionFilterLinkEntity l
        where l.subscription.id = :subscriptionId
        order by l.filter.id
        """)
    List<FilterEntity> findAllBySubscriptionId(@Param("subscriptionId") long subscriptionId);

    boolean existsByChat_IdAndValue(long chatId, String value);
}
