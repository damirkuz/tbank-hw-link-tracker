package backend.academy.linktracker.scrapper.repository.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "subscriptions",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_subscriptions_chat_link",
                    columnNames = {"chat_id", "link_id"})
        })
@NoArgsConstructor
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatEntity chat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "link_id", nullable = false)
    private LinkEntity link;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "subscription_tags", joinColumns = @JoinColumn(name = "subscription_id"))
    @Column(name = "tag", nullable = false)
    private Set<String> tags = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "subscription_filters", joinColumns = @JoinColumn(name = "subscription_id"))
    @Column(name = "filter_value", nullable = false)
    private Set<String> filters = new HashSet<>();

    public SubscriptionEntity(ChatEntity chat, LinkEntity link, Set<String> tags, Set<String> filters) {
        this.chat = chat;
        this.link = link;
        if (tags != null) {
            this.tags = tags;
        }
        if (filters != null) {
            this.filters = filters;
        }
    }
}
