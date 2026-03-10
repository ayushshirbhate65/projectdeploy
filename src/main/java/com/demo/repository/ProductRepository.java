package com.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByStatus(String status);

    List<Product> findByCategory_CategoryId(Integer categoryId);

    List<Product> findByCategory_CategoryIdAndStatus(Integer categoryId, String status);

    List<Product> findBySeller_UserId(Integer userId);

    List<Product> findBySeller_State_StateIdAndStatus(Integer stateId, String status);

}