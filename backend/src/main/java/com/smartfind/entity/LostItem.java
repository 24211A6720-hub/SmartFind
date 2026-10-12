package com.smartfind.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lost_items")
@AttributeOverride(name = "itemDate", column = @Column(name = "date_lost", nullable = false))
public class LostItem extends BaseItem {
    @Override
    public ItemType getType() {
        return ItemType.LOST;
    }
}
