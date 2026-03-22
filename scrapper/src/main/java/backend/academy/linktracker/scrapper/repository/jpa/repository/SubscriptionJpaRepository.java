package backend.academy.linktracker.scrapper.repository.jpa.repository;

import backend.academy.linktracker.scrapper.repository.jpa.entity.SubscriptionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, Long> {

    Optional<SubscriptionEntity> findByChat_ChatIdAndLink_Id(Long chatId, Long linkId);

    @EntityGraph(attributePaths = {"chat", "link", "tags", "filters"})
    List<SubscriptionEntity> findAllByChat_ChatId(Long chatId);

    @EntityGraph(attributePaths = {"chat"})
    List<SubscriptionEntity> findAllByLink_Id(Long linkId);

    long deleteByChat_ChatIdAndLink_Id(Long chatId, Long linkId);
}
