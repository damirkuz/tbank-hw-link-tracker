package backend.academy.linktracker.scrapper.repository.jpa.repository;

import backend.academy.linktracker.scrapper.repository.jpa.entity.SubscriptionTagLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionTagLinkJpaRepository extends JpaRepository<SubscriptionTagLinkEntity, Long> {

    boolean existsBySubscription_IdAndTag_Id(long subscriptionId, long tagId);

    long deleteBySubscription_IdAndTag_Id(long subscriptionId, long tagId);
}
