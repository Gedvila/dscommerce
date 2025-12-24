package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.dto.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscommerce.test.ProductFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;
    private String productName;
    private Product product;
    private PageImpl page;
    private ProductDTO productDTO;
    private Long totalProducts;

    @BeforeEach
    void setUp() throws Exception{
        existingId = 1L;
        nonExistingId = 200L;
        dependentId = 3L;
        totalProducts = 25L;
        productName = "PlayStation 5";

        product = ProductFactory.createProduct(productName);
        productDTO = new ProductDTO(product);


        page = new PageImpl<>(List.of(product));

        Mockito.when(productRepository.existsById(existingId)).thenReturn(true);
        Mockito.when(productRepository.existsById(nonExistingId)).thenReturn(false);
        Mockito.when(productRepository.existsById(dependentId)).thenReturn(true);

        Mockito.when(productRepository.getReferenceById(existingId)).thenReturn(product);
        Mockito.when(productRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);
        Mockito.when(productRepository.getReferenceById(dependentId)).thenThrow(DataIntegrityViolationException.class);

        Mockito.when(productRepository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Mockito.when(productRepository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(product);

        Mockito.doNothing().when(productRepository).deleteById(existingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);
    }


    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists(){

        ProductDTO productDTO = productService.findById(existingId);

        Assertions.assertNotNull(productDTO);
        Assertions.assertEquals(productDTO.getId(),existingId);
        Assertions.assertEquals(productDTO.getName(),product.getName());
    }

    @Test
    public void findByIdShouldThrowsResourceNotFoundExceptionWhenIdNonExists(){

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.findById(nonExistingId);
        });
    }

    @Test
    public void findAllShouldReturnPagedProductMinDTO(){
        Pageable pageable = PageRequest.of(0,10);

        Page<ProductMinDTO> result = productService.findAll(pageable);

        Assertions.assertFalse(result.isEmpty());
        Mockito.verify(productRepository).findAll(pageable);
    }

    @Test
    public void insertShouldReturnProductDto(){

        ProductDTO result = productService.insert(productDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(),product.getId());
    }

    @Test
    public void updateShouldReturnProductDtoWhenIdExists(){

        ProductDTO result = productService.update(existingId,productDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(product.getId(),result.getId());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenNonExistId(){

        Assertions.assertThrows(ResourceNotFoundException.class, ()->{
            productService.update(nonExistingId,productDTO);
        });
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists(){
       Assertions.assertDoesNotThrow(()->{
           productService.delete(existingId);
       });
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdNonExists(){
        Assertions.assertThrows(ResourceNotFoundException.class,() ->{
            productService.delete(nonExistingId);
        });
    }

    @Test
    public void deleteShouldThrowDataIntegrityViolationExceptionWhenDependentId(){
        Assertions.assertThrows(DatabaseException.class,()->{
            productService.delete(dependentId);
        });
    }

}
