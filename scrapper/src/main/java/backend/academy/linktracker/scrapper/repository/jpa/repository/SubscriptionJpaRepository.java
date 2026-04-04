package backend.academy.linktracker.scrapper.repository.jpa.repository;

import backend.academy.linktracker.scrapper.repository.jpa.entity.SubscriptionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, Long> {

    List<SubscriptionEntity> findAllByChat_Id(long chatId);

    List<SubscriptionEntity> findAllByLink_Id(long linkId);

    Optional<SubscriptionEntity> findByChat_IdAndLink_Id(Long chatId, Long linkId);

    boolean existsByChat_IdAndLink_Id(long chatId, long linkId);
}
