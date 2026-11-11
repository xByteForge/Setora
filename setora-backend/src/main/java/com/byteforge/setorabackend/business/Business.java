package com.byteforge.setorabackend.business;

import com.byteforge.setorabackend.auth.User;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(
        name = "businesses",
        check = @CheckConstraint(constraint = "latitude between -90 and 90 and longitude between -180 and 180")
)
@Getter
@Setter
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, columnDefinition = "text")
    private String name;

    //used for the public page
    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String address;

    //map position, can be missing until the map is integrated
    private Double latitude;

    private Double longitude;

    @Column(columnDefinition = "text")
    private String phone;

    @Column(name = "photo_url", columnDefinition = "text")
    private String photoUrl;

    @Column(columnDefinition = "text")
    private ZoneId timezone;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
