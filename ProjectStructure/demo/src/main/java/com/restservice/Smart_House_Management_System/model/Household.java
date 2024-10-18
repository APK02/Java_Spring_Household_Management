package com.restservice.authentication.model;
import com.restservice.authentication.dto.HouseholdDTO;
import com.restservice.authentication.repositories.HouseholdRepositoriesGroup;
import com.restservice.shoppingListAndInventory.chores.ChoresHistoryList;
import com.restservice.shoppingListAndInventory.chores.ChoresList;
import com.restservice.shoppingListAndInventory.inventory.InventoryException;
import com.restservice.shoppingListAndInventory.inventory.InventoryList;
import com.restservice.shoppingListAndInventory.inventory.Quantity;
import com.restservice.shoppingListAndInventory.inventory.QuantityType;
import com.restservice.shoppingListAndInventory.notifications.NotificationsList;
import com.restservice.shoppingListAndInventory.shopping.ShoppingException;
import com.restservice.shoppingListAndInventory.shopping.ShoppingItem;
import com.restservice.shoppingListAndInventory.shopping.ShoppingLists;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "household")
public class Household {

    @Id
    @Column(name = "household_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "description")
    private String description;

    @OneToMany
    @JoinTable(name = "household_users",
            joinColumns = @JoinColumn(name = "household_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<User> users = new ArrayList<>();

    public void addUser(User user) {
        if (user == null) {
            return;
        }
        if (users == null) {
            users = new ArrayList<>();
        }
        users.add(user);
    }


    @OneToOne(mappedBy = "household", cascade = CascadeType.ALL)
    public InventoryList inventoryList;

    @OneToOne(mappedBy = "household", cascade = CascadeType.ALL)
    public ShoppingLists shoppingLists;

    @OneToOne(mappedBy = "household", cascade = CascadeType.ALL)
    public ChoresList choresList;

    @OneToOne(mappedBy = "household", cascade = CascadeType.ALL)
    public ChoresHistoryList choresHistoryList;

    @OneToOne(mappedBy = "household", cascade = CascadeType.ALL)
    public NotificationsList notificationsList;

    public Household(HouseholdDTO householdDTO) {
        this.name = householdDTO.getName();
        this.address = householdDTO.getAddress() + ", " + householdDTO.getCity();
        this.country = householdDTO.getCountry();
        this.city = householdDTO.getCity();
        this.description = householdDTO.getDescription();
    }

    public boolean removeUser(Long userID) //returns true if remove was successful, false otherwise
    {
        if (userID == null) {
            return false;
        }
        if (users == null) {
            users = new ArrayList<>();
        }
        for (int i = 0; i < users.size(); i++)
            if (users.get(i).getId().equals(userID)) {
                users.remove(i);
                return true;
            }
        return false;
    }
    public static Household loadHousehold(HouseholdRepositoriesGroup repositories){
        Iterator<Household> iter = repositories.householdRepository.findAll().iterator();
        if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
    }
    public void markItemAsBought(String indexString, String idString, HouseholdRepositoriesGroup repositories) throws ShoppingException, InventoryException {
        ShoppingItem shoppingItem = shoppingLists.removeItem(indexString, idString, repositories.shoppingRepository);
        for (int i = 0; i < inventoryList.getItemList().size(); i++)
            if (inventoryList.getItemList().get(i).getItem().getId() == shoppingItem.getItem().getId()) {
                Quantity quantity = new Quantity(inventoryList.getItemList().get(i).getQuantity().getValue() + shoppingItem.getQuantity().getValue(), QuantityType.Amount);
                inventoryList.changeQuantity(i, quantity, repositories.inventoryRepository);
                return;
            }
        inventoryList.addItem(shoppingItem.getItem().getId(), shoppingItem.getQuantity(), repositories.inventoryRepository);
    }
}
