package com.byteforge.setorabackend.business.model;

import com.byteforge.setorabackend.auth.User;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@Entity
@Table(
        name = "businesses",
        check =
                @CheckConstraint(
                        name = "chk_business_coordinates",
                        constraint = "latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180"))
@Getter
@Setter
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, columnDefinition = "text")
    private String name;

    // used for the public page
    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Embedded
    private Location location;

    @Column(columnDefinition = "text")
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Convert(converter = Jsr310JpaConverters.ZoneIdConverter.class)
    @Column(nullable = false)
    private ZoneId timezone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "photo_urls", columnDefinition = "text[]")
    private String[] photoUrls;

    @Column(name = "thumbnail_url", columnDefinition = "text")
    private String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "business", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<ServiceItem> serviceItems = new ArrayList<>();
}
