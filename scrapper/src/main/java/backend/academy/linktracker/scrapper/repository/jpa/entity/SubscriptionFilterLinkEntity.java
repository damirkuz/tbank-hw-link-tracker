package backend.academy.linktracker.scrapper.repository.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "subscription_filter_links",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subscription_id", "filter_id"}))
@Getter
@Setter
public class SubscriptionFilterLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionEntity subscription;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id", nullable = false)
    private FilterEntity filter;
}
