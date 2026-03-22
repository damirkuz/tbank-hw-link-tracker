package backend.academy.linktracker.scrapper.repository.jpa.repository;

import backend.academy.linktracker.scrapper.repository.jpa.entity.SubscriptionFilterLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionFilterLinkJpaRepository extends JpaRepository<SubscriptionFilterLinkEntity, Long> {

    boolean existsBySubscription_IdAndFilter_Id(long subscriptionId, long filterId);

    long deleteBySubscription_IdAndFilter_Id(long subscriptionId, long filterId);
}
