package com.jpgedvila.dscommerce.repositories;

import com.jpgedvila.dscommerce.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
