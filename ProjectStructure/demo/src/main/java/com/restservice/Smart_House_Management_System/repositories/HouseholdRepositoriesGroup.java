package com.restservice.authentication.repositories;

import com.restservice.shoppingListAndInventory.chores.ChoreRepository;
import com.restservice.shoppingListAndInventory.inventory.InventoryRepository;
import com.restservice.shoppingListAndInventory.notifications.NotificationRepository;
import com.restservice.shoppingListAndInventory.shopping.ShoppingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HouseholdRepositoriesGroup {
    @Autowired
    public HouseholdRepository householdRepository;
    @Autowired
    public InventoryRepository inventoryRepository;
    @Autowired
    public ShoppingRepository shoppingRepository;
    @Autowired
    public ChoreRepository choreRepository;
    @Autowired
    public NotificationRepository notificationRepository;
}