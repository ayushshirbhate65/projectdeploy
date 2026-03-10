package com.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.dto.CreateProductDTO;
import com.demo.model.Category;
import com.demo.model.Product;
import com.demo.model.User;
import com.demo.repository.CategoryRespository;
import com.demo.repository.ProductRepository;
import com.demo.repository.UserRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRespository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileSystemService fileSystemService;

    /* ======================
       GET
       ====================== */

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> getProductsByStatus(String status) {
        return productRepository.findByStatus(status.toUpperCase());
    }

    public List<Product> getProductsByCategory(Integer categoryId) {
        return productRepository.findByCategory_CategoryId(categoryId);
    }

    public List<Product> getProductsByCategoryAndStatus(Integer categoryId, String status) {
        return productRepository.findByCategory_CategoryIdAndStatus(
                categoryId, status.toUpperCase());
    }

    /* ======================
       CREATE
       ====================== */

    public Product addProduct(CreateProductDTO dto) {

        fileSystemService.ensureUserProductFolderExists(dto.getSellerId());

        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantityAvailable(dto.getQuantityAvailable());
        product.setUnit(dto.getUnit());
        product.setQuality(dto.getQuality());
        product.setOriginPlace(dto.getOriginPlace());
        product.setImageUrl(dto.getImageUrl());

        product.setStatus("PENDING");
        product.setCreatedAt(LocalDateTime.now());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        User seller = userRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        product.setCategory(category);
        product.setSeller(seller);

        return productRepository.save(product);
    }

    /* ======================
       ADMIN STATUS UPDATE
       ====================== */

    public Product updateProductStatus(Integer productId, String status) {

        Product product = getProductById(productId);
        product.setStatus(status.toUpperCase());

        return productRepository.save(product);
    }

    /* ======================
       DELETE PRODUCT (SOFT DELETE)
       ====================== */

    public Product deleteProduct(Integer productId, Integer sellerId) {

        Product product = getProductById(productId);

        // Ensure seller owns the product
        if (!product.getSeller().getUserId().equals(sellerId)) {
            throw new RuntimeException("You are not allowed to delete this product");
        }

        product.setStatus("DELETED");

        return productRepository.save(product);
    }

    /* ======================
       GET PRODUCTS BY SELLER
       ====================== */

    public List<Product> getProductsBySeller(Integer userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        return productRepository.findBySeller_UserId(userId);
    }

    /* ======================
       UPDATE PRODUCT
       ====================== */

    public Product updateProductById(Integer productId, CreateProductDTO dto) {

        Product product = getProductById(productId);

        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantityAvailable(dto.getQuantityAvailable());
        product.setUnit(dto.getUnit());
        product.setQuality(dto.getQuality());
        product.setOriginPlace(dto.getOriginPlace());
        product.setImageUrl(dto.getImageUrl());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (dto.getSellerId() != null) {
            User seller = userRepository.findById(dto.getSellerId())
                    .orElseThrow(() -> new RuntimeException("Seller not found"));
            product.setSeller(seller);
        }

        return productRepository.save(product);
    }

    /* ======================
       REGION PRODUCTS
       ====================== */

    public List<Product> getProductsForUserRegion(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Integer stateId = user.getState().getStateId();

        return productRepository
                .findBySeller_State_StateIdAndStatus(stateId, "APPROVED");
    }
}