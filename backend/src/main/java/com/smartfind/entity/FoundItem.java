package com.smartfind.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "found_items")
@AttributeOverride(name = "itemDate", column = @Column(name = "date_found", nullable = false))
public class FoundItem extends BaseItem {
    @Override
    public ItemType getType() {
        return ItemType.FOUND;
    }
}
