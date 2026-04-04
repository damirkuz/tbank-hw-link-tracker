package backend.academy.linktracker.scrapper.repository.jpa.repository;

import backend.academy.linktracker.scrapper.repository.jpa.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatJpaRepository extends JpaRepository<ChatEntity, Long> {
    @Modifying
    @Query("delete from ChatEntity c where c.id = :id")
    int deleteRowById(@Param("id") Long id);
}
