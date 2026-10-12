package com.smartfind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** One scored pair: a lost report and a found report. Table is match_results because MATCH is reserved in MySQL. */
@Entity
@Table(name = "match_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lost_item_id", "found_item_id"}))
@Getter @Setter @NoArgsConstructor
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private LostItem lostItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "found_item_id", nullable = false)
    private FoundItem foundItem;

    @Column(nullable = false)
    private double score;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_level", nullable = false, length = 20)
    private MatchLevel matchLevel;

    @Column(length = 1000)
    private String reasons;

    @Column(name = "name_score")        private double nameScore;
    @Column(name = "category_score")    private double categoryScore;
    @Column(name = "color_score")       private double colorScore;
    @Column(name = "location_score")    private double locationScore;
    @Column(name = "date_score")        private double dateScore;
    @Column(name = "description_score") private double descriptionScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
