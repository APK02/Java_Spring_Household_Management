package com.restservice.authentication.service;

import com.restservice.authentication.dto.UserDTO;
import com.restservice.authentication.model.Household;
import com.restservice.authentication.model.Invite;
import com.restservice.authentication.model.Role;
import com.restservice.authentication.model.User;
import com.restservice.authentication.repositories.HouseholdRepositoriesGroup;
import com.restservice.authentication.repositories.HouseholdRepository;
import com.restservice.authentication.repositories.InviteRepository;
import com.restservice.authentication.repositories.UserRepository;
import com.restservice.shoppingListAndInventory.chores.ChoresHistoryList;
import com.restservice.shoppingListAndInventory.chores.ChoresList;
import com.restservice.shoppingListAndInventory.inventory.InventoryList;
import com.restservice.shoppingListAndInventory.notifications.NotificationsList;
import com.restservice.shoppingListAndInventory.shopping.ShoppingLists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HouseholdService {

    @Autowired
    HouseholdRepository householdRepository;
    @Autowired
    public HouseholdRepositoriesGroup householdRepositoriesGroup;

    @Autowired
    GeocodingService geocodingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InviteRepository inviteRepository;

    public Household findById(Long id) {
        return householdRepository.findById(id)
                .orElse(null);
    }

    public Household addHousehold(Household household, User user) throws Exception {
        GeocodingService.GeocodingResult result = geocodingService.geocode(household.getAddress());
        household.setLatitude(result.getLat());
        household.setLongitude(result.getLon());
        household.addUser(user);
        householdRepository.save(household);
        return this.initializeInventory(household);
    }
    public Household initializeInventory(Household household) throws Exception{

        household.inventoryList=new InventoryList();
        household.inventoryList.setHousehold(household);
        householdRepositoriesGroup.inventoryRepository.inventoryListRepository.save(household.inventoryList);

        household.shoppingLists=new ShoppingLists();
        household.shoppingLists.setHousehold(household);
        householdRepositoriesGroup.shoppingRepository.shoppingListsRepository.save(household.shoppingLists);
        household.shoppingLists.addList("List 1", householdRepositoriesGroup.shoppingRepository);

        household.choresList=new ChoresList();
        household.choresList.setHousehold(household);
        householdRepositoriesGroup.choreRepository.choreListRepository.save(household.choresList);

        household.choresHistoryList=new ChoresHistoryList();
        household.choresHistoryList.setHousehold(household);
        householdRepositoriesGroup.choreRepository.choreHistoryListRepository.save(household.choresHistoryList);

        household.notificationsList=new NotificationsList();
        household.notificationsList.setHousehold(household);
        householdRepositoriesGroup.notificationRepository.notificationListRepository.save(household.notificationsList);
        return household;
    }
    public Household inviteUser(Household household, UserDTO userDTO) {
        User user = new User(userDTO);
        household.addUser(user);
        return householdRepository.save(household);
    }

    public List<Household> findAll() {
        return householdRepository.findAll();
    }

    public Household findByUser(User user) {
        Long userId = user.getId();
        for (Household household : householdRepository.findAll()) {
            for (User iterUser : household.getUsers()) {
                if (iterUser.getId().equals(userId)) {
                    return household;
                }
            }
        }
        return null;
    }

    public List<UserDTO> memberList(User user) {
        Household household = this.findByUser(user);
        if (household == null) {
            return null;
        }
        System.out.println(household.getUsers());
        return household.getUsers().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    public void removeHousehold(Long userID) throws Exception{
        Optional<Household> householdOptional=householdRepository.findByUserId(userID);
        if(householdOptional.isEmpty())
            throw new Exception("User id does not exits");
        Household household= householdOptional.get();
        householdRepository.delete(household);
    }

    public void kickUser(User admin, Long kickedUser)throws Exception{
        if(admin.getRole()!= Role.ADMIN)
            throw new Exception("Only admins can kick users");
        Household household = this.findByUser(admin);
        if(household==null)
            throw new Exception("Household id doesn't exist");
        if(!household.removeUser(kickedUser))
            throw new Exception("User isn't part of admin's household");
        householdRepository.save(household);
    }

    public void sendInvite(User admin, String invitedUsername) throws Exception {
        if(admin.getRole()!= Role.ADMIN)
            throw new Exception("Only admins can invite users");
        Household household = householdRepository.findByUserId(admin.getId())
                .orElse(null);
        if(household==null)
            throw new Exception("Household id doesn't exist");
        User invitedUser=userRepository.findByUsername(invitedUsername);
        if(invitedUser==null)
            throw new Exception("Invited user doesn't exist");
        for(User iter:household.getUsers())
            if(iter.getId().equals(invitedUser.getId()))
                throw new Exception("User is already part of this household");
        inviteRepository.save(invitedUser.addInvite(household.getId()));
    }

    public void acceptInvite(User user, Long inviteID)throws Exception{
        Invite invite=inviteRepository.findById(inviteID)
                .orElse(null);
        if(invite==null)
            throw new Exception("Invite doesn't exist");
        Household household=this.findById(invite.getHouseholdId());
        if(household==null)
            throw new Exception("Household id doesn't exist");
        household.addUser(user);
        user.getInviteList().remove(invite);
        householdRepository.save(household);
        inviteRepository.delete(invite);
    }
}
