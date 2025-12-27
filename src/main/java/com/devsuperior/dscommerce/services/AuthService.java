package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.services.exceptions.ForbiddenException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    public AuthService(UserService userService){
        this.userService = userService;
    }

    public void validateSelfOrAdmin(Long userId){
        User me = userService.authenticated();
        if (me.hasRole("ROLE_ADMIN")){
            return;
        }
        if (!me.getId().equals(userId)){
            throw new ForbiddenException("Acesso Negado");
        }
    }
}
