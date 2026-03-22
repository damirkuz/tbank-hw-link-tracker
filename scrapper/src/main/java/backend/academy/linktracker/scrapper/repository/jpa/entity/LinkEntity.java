package backend.academy.linktracker.scrapper.repository.jpa.entity;

import backend.academy.linktracker.scrapper.model.TrackedResource;
import backend.academy.linktracker.scrapper.repository.jpa.converter.UriAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "links")
@NoArgsConstructor
public class LinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = UriAttributeConverter.class)
    @Column(name = "uri", nullable = false, length = 2048)
    private URI uri;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracked_resource", nullable = false, length = 100)
    private TrackedResource trackedResource;

    @Column(name = "last_update")
    private Instant lastUpdate;

    @Column(name = "next_check_at")
    private OffsetDateTime nextCheckAt;

    public LinkEntity(URI uri, TrackedResource trackedResource, Instant lastUpdate) {
        this.uri = uri;
        this.trackedResource = trackedResource;
        this.lastUpdate = lastUpdate;
    }
}
