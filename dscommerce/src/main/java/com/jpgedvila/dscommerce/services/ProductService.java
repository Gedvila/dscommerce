package com.jpgedvila.dscommerce.services;

import com.jpgedvila.dscommerce.dto.ProductDTO;
import com.jpgedvila.dscommerce.entities.Product;
import com.jpgedvila.dscommerce.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = repository.findById(id).get();
        return new ProductDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(Pageable pageable) {
        Page<Product> result = repository.findAll(pageable);
        return result.map(x -> new ProductDTO(x));
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
       Product entity = new Product();

       entity.setName(dto.getName());
       entity.setDescription(dto.getDescription());
       entity.setPrice(dto.getPrice());
       entity.setImgUrl(dto.getImgUrl());

       entity = repository.save(entity);

       return new ProductDTO(entity);
    }
}
