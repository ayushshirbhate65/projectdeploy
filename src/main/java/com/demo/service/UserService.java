package com.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.demo.dto.CreateUserDTO;
import com.demo.model.Division;
import com.demo.model.Role;
import com.demo.model.State;
import com.demo.model.User;
import com.demo.model.UserRole;
import com.demo.repository.DivisionRepository;
import com.demo.repository.RoleRepository;
import com.demo.repository.StateRepository;
import com.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    /* ======================
       ASSIGN ROLES
       ====================== */
    @Transactional
    public void assignRoles(Integer userId, List<Integer> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getUserRoles().clear();
        userRepository.flush();

        for (Integer roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            user.getUserRoles().add(userRole);
        }
        userRepository.save(user);
    }

    /* ======================
       CREATE USER
       ====================== */
    @Transactional
    public User createUser(CreateUserDTO dto) {

        User user = new User();

        user.setUserName(dto.getUserName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setMobile(dto.getMobile());
        user.setAddress(dto.getAddress());
        user.setStatus(dto.getStatus());
        user.setCredit(dto.getCredit());
        user.setGender(dto.getGender());
        user.setDob(dto.getDob());
        user.setCreatedAt(LocalDateTime.now());

        // DEFAULT ROLE = ROLE_BUYER
        Role buyerRole = roleRepository.findByRoleName("ROLE_BUYER")
                .orElseThrow(() -> new RuntimeException("ROLE_BUYER not found"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(buyerRole);

        user.getUserRoles().add(userRole);

        // STATE
        State state = stateRepository.findById(dto.getStateId())
                .orElseThrow(() -> new RuntimeException("State not found"));

        // DIVISION
        Division division = divisionRepository.findById(dto.getDivisionId())
                .orElseThrow(() -> new RuntimeException("Division not found"));

        // VALIDATION
        if (!division.getState().getStateId().equals(state.getStateId())) {
            throw new RuntimeException("Division does not belong to selected state");
        }

        user.setState(state);
        user.setDivision(division);

        return userRepository.save(user);
    }

    /* ======================
       GET USER BY ID
       ====================== */
    public User getUserById(Integer userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + userId));
    }

    /* ======================
       GET ALL USERS
       ====================== */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /* ======================
       UPDATE USER
       ====================== */
    public User updateUser(Integer userId, User user) {

        User existingUser = getUserById(userId);

        existingUser.setUserName(user.getUserName());
        existingUser.setMobile(user.getMobile());
        existingUser.setAddress(user.getAddress());
        existingUser.setStatus(user.getStatus());
        existingUser.setCredit(user.getCredit());

        return userRepository.save(existingUser);
    }
    

    /* ======================
       DELETE USER
       ====================== */
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }

    /* ======================
       GET USER BY EMAIL
       ====================== */
    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found with email: " + email));
    }

    /* ======================
       GET USERS BY STATUS
       ====================== */
    public List<User> getUsersByStatus(String status) {
        return userRepository.findByStatus(status);
    }

    @Transactional
    public void becomeSeller(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role sellerRole = roleRepository.findByRoleName("ROLE_SELLER")
                .orElseThrow(() -> new RuntimeException("ROLE_SELLER not found"));

        boolean alreadySeller = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getRoleName().equals("ROLE_SELLER"));

        if (!alreadySeller) {

            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(sellerRole);

            user.getUserRoles().add(userRole);

            userRepository.save(user);
        }
    }
}