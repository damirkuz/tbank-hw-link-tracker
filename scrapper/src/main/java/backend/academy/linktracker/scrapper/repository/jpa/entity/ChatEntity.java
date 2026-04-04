package backend.academy.linktracker.scrapper.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chats")
@NoArgsConstructor
public class ChatEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    public ChatEntity(Long chatId) {
        this.id = chatId;
    }
}
