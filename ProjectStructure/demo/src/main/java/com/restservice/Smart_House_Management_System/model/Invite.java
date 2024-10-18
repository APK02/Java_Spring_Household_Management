package com.restservice.authentication.model;

import com.restservice.shoppingListAndInventory.inventory.InventoryList;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "invites")
public class Invite {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "household_id")
    Long householdId;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;
}