package com.devsuperior.dscommerce.controllers;

import com.devsuperior.dscommerce.TokenUtil;
import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.test.ProductFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String productName;
    private  String userUsername, password, admUsername, token;

    private Product product;
    private ProductDTO productDTO;

    private String jsonBody;

    private Long existingProductId, nonExistingProductId, dependentProductId;

    @BeforeEach
    void setUp() throws Exception{
        productName = "Macbook";

        userUsername = "maria@gmail.com";
        admUsername = "alex@gmail.com";
        password = "123456";

        product = ProductFactory.createProduct();
        productDTO = new ProductDTO(product);

        jsonBody = objectMapper.writeValueAsString(productDTO);

        existingProductId = 2L;
        nonExistingProductId = 10000L;
        dependentProductId = 3L;
    }

    @Test
    public void findAllShouldReturnPageWhenNameParamIsNotEmpty() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get("/products?name={productName}", productName)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(3L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("Macbook Pro"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].price").value(1250.0));
    }

    @Test
    public void findAllShouldReturnPageWhenNameParamIsEmpty() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("The Lord of the Rings"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].price").value(90.5));
    }

    @Test
    public void insertShouldInsertProductWhenAdmIsLogged() throws Exception{

        String expectedName = productDTO.getName();
        String expectedDescription = productDTO.getDescription();

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                .header("Authorization","Bearer " + token)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(expectedName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(expectedDescription));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdmIsLoggedAndNameIsInvalid() throws Exception{

        product.setName("A");
        productDTO = new ProductDTO(product);
        jsonBody = objectMapper.writeValueAsString(productDTO);

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .header("Authorization","Bearer " + token)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Dados inválidos"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].fieldName").value("name"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value("Nome precisar ter de 3 a 80 caracteres"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdmIsLoggedAndDescriptionIsInvalid() throws Exception{

        product.setDescription("Não");
        productDTO = new ProductDTO(product);
        jsonBody = objectMapper.writeValueAsString(productDTO);

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .header("Authorization","Bearer " + token)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Dados inválidos"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].fieldName").value("description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value("Descrição precisa ter no mínimo 10 caracteres"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdmIsLoggedAndPriceNegative() throws Exception{

        product.setPrice(-200.0);
        productDTO = new ProductDTO(product);
        jsonBody = objectMapper.writeValueAsString(productDTO);

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .header("Authorization","Bearer " + token)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Dados inválidos"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].fieldName").value("price"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value("O preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdmIsLoggedAndPriceIsZero() throws Exception{

        product.setPrice(0.0);
        productDTO = new ProductDTO(product);
        jsonBody = objectMapper.writeValueAsString(productDTO);

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .header("Authorization","Bearer " + token)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Dados inválidos"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].fieldName").value("price"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value("O preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdmIsLoggedAndNoCategories() throws Exception{

        product.getCategories().clear();
        productDTO = new ProductDTO(product);
        jsonBody = objectMapper.writeValueAsString(productDTO);

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .header("Authorization","Bearer " + token)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Dados inválidos"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].fieldName").value("categories"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message").value("Deve ter pelo menos uma categoria"));
    }

    @Test
    public void insertShouldReturnForbiddenWhenUserIsLogged() throws Exception{


        token = tokenUtil.obtainAccessToken(mockMvc,userUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .header("Authorization","Bearer " + token)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void insertShouldReturnUnauthorizedWhenIsNotLogged() throws Exception{


        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void deleteShouldDeleteProductWhenAdminIsLogged() throws Exception{

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", existingProductId)
                .header("Authorization","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void deleteShouldReturnNotFoundWhenAdminIsLoggedAndIdNonExists() throws Exception{

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", nonExistingProductId)
                        .header("Authorization","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteShouldBadRequestWhenAdminIsLoggedAndDependentId() throws Exception{

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", dependentProductId)
                        .header("Authorization","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void deleteShouldReturnForbiddenWhenUserIsLogged() throws Exception{

        token = tokenUtil.obtainAccessToken(mockMvc,userUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", existingProductId)
                        .header("Authorization","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
        public void deleteShouldReturnUnauthorizedWhenNotLogged() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", existingProductId)
                        .header("Authorization","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}