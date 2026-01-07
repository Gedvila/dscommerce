package com.devsuperior.dscommerce.controllers;

import com.devsuperior.dscommerce.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenUtil tokenUtil;

    private Long existingOrderId, nonExistingOrderId;

    private String userUsername,admUsername,password,token;

    @BeforeEach
    void setUp() throws Exception{

        existingOrderId = 1L;
        nonExistingOrderId = 10000L;

        userUsername = "maria@gmail.com";
        admUsername = "alex@gmail.com";
        password = "123456";
    }

    @Test
    public void getByIdShouldReturnOrderWhenAdminIsLoggedAndIdExists() throws  Exception{

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}",existingOrderId)
                .header("Authorization","Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getByIdShouldReturnOrderWhenUserIsLoggedAndIdExistsAndOrderIsForClient() throws  Exception{

        token = tokenUtil.obtainAccessToken(mockMvc,userUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}",existingOrderId)
                        .header("Authorization","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getByIdShouldReturnForbiddenWhenUserIsLoggedAndOrderIsNotHis() throws  Exception{

        token = tokenUtil.obtainAccessToken(mockMvc,userUsername,password);
        existingOrderId = 2L;

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}",existingOrderId)
                        .header("Authorization","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getByIdShouldReturnNotFoundWhenAdminIsLoggedAndIdNonExists() throws  Exception{

        token = tokenUtil.obtainAccessToken(mockMvc,admUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}",nonExistingOrderId)
                        .header("Authorization","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void getByIdShouldReturnNotFoundWhenUserIsLoggedAndIdNonExists() throws  Exception{

        token = tokenUtil.obtainAccessToken(mockMvc,userUsername,password);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}",nonExistingOrderId)
                        .header("Authorization","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void getByIdShouldReturnUnauthorizeWhenNotLogged() throws  Exception{

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}",nonExistingOrderId)
                        .header("Authorization","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}
