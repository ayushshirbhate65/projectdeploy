package com.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.demo.dto.CreateUserDTO;
import com.demo.dto.ProductDTO;
import com.demo.dto.RoleDTO;
import com.demo.dto.UserResponseDTO;
import com.demo.model.Product;
import com.demo.model.User;
import com.demo.security.JwtUtil;
import com.demo.service.ProductService;
import com.demo.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    // FIX: Inject JwtUtil to generate a fresh token after role upgrade
    @Autowired
    private JwtUtil jwtUtil;


    /* ======================
       CREATE USER
       ====================== */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @RequestBody CreateUserDTO dto) {

        User savedUser = userService.createUser(dto);

        return new ResponseEntity<>(
                mapToUserDTO(savedUser),
                HttpStatus.CREATED
        );
    }

    /* ======================
       BECOME SELLER
       FIX: Returns a new JWT token containing the SELLER role,
       so the frontend doesn't need to re-login manually.
       ====================== */
    @PostMapping("/become-seller")
    public ResponseEntity<Map<String, Object>> becomeSeller(
            @RequestParam Integer userId) {

        userService.becomeSeller(userId);

        // Re-fetch updated user and issue a fresh token with the new SELLER role
        User user = userService.getUserById(userId);
        String newToken = jwtUtil.generateToken(user.getEmail());

        Map<String, Object> response = Map.of(
                "token", newToken,
                "type", "Bearer",
                "message", "User upgraded to SELLER",
                "user", Map.of(
                        "id", user.getUserId(),
                        "name", user.getUserName(),
                        "email", user.getEmail(),
                        "roles", user.getUserRoles()
                                .stream()
                                .map(ur -> ur.getRole().getRoleName())
                                .toList()
                )
        );

        return ResponseEntity.ok(response);
    }


    /* ======================
       GET USER BY ID
       ====================== */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(mapToUserDTO(user));
    }

    /* ======================
       GET ALL USERS
       ====================== */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        List<UserResponseDTO> users = userService.getAllUsers()
                .stream()
                .map(this::mapToUserDTO)
                .toList();
        return ResponseEntity.ok(users);
    }

    /* ======================
       UPDATE USER
       ====================== */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Integer id,
            @RequestBody User user) {

        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(mapToUserDTO(updatedUser));
    }

    /* ======================
       DELETE USER
       ====================== */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    /* ======================
       GET USER BY EMAIL
       ====================== */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(mapToUserDTO(user));
    }

    /* ======================
       GET USERS BY STATUS
       ====================== */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByStatus(@PathVariable String status) {
        List<UserResponseDTO> users = userService.getUsersByStatus(status)
                .stream()
                .map(this::mapToUserDTO)
                .toList();
        return ResponseEntity.ok(users);
    }


    /* ======================
       GET PRODUCTS BY SELLER (USER)
       ====================== */
    @GetMapping("/{userId}/products")
    public ResponseEntity<List<ProductDTO>> getProductsBySeller(
            @PathVariable Integer userId) {

        List<ProductDTO> products = productService.getProductsBySeller(userId)
                .stream()
                .map(this::mapToProductDTO)
                .toList();

        return ResponseEntity.ok(products);
    }


    /* ======================
       ENTITY → DTO MAPPER
       ====================== */
    private UserResponseDTO mapToUserDTO(User user) {

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setAddress(user.getAddress());
        dto.setStatus(user.getStatus());
        dto.setCredit(user.getCredit());
        dto.setGender(user.getGender());
        dto.setDob(user.getDob());

        List<RoleDTO> roles = user.getUserRoles()
                .stream()
                .map(ur -> {
                    RoleDTO r = new RoleDTO();
                    r.setRoleId(ur.getRole().getRoleId());
                    r.setRoleName(ur.getRole().getRoleName());
                    return r;
                })
                .toList();

        dto.setRoles(roles);
        return dto;
    }


    private ProductDTO mapToProductDTO(Product p) {

        ProductDTO dto = new ProductDTO();
        dto.setProductId(p.getProductId());
        dto.setProductName(p.getProductName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setQuantityAvailable(p.getQuantityAvailable());
        dto.setUnit(p.getUnit());
        dto.setStatus(p.getStatus());
        dto.setImageUrl(p.getImageUrl());
        dto.setCreatedAt(p.getCreatedAt());

        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getCategoryId());
            dto.setCategoryName(p.getCategory().getCategoryName());
        }

        if (p.getSeller() != null) {
            dto.setSellerId(p.getSeller().getUserId());
            dto.setSellerName(p.getSeller().getUserName());
        }

        return dto;
    }
}