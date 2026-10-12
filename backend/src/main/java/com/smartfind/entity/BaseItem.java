package com.smartfind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Fields shared by lost and found reports. The date column is renamed in each subclass. */
@MappedSuperclass
@Getter @Setter
public abstract class BaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false, length = 60)
    private String color;

    @Column(nullable = false, length = 200)
    private String location;

    /** Stored as date_lost or date_found (see @AttributeOverride in the subclasses). */
    @Column(name = "item_date", nullable = false)
    private LocalDate itemDate;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "identifying_details", length = 1000)
    private String identifyingDetails;

    @Column(name = "image_path", length = 500)
    private String imageUrl;

    @Column(name = "contact_info", nullable = false, length = 150)
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public abstract ItemType getType();
}
