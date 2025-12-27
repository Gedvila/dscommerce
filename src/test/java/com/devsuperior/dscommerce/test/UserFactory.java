package com.devsuperior.dscommerce.test;

import com.devsuperior.dscommerce.entities.Role;
import com.devsuperior.dscommerce.entities.User;

import java.time.LocalDate;

public class UserFactory {

    public static User createClientUser(){
        User user = new User(1L,"João","joão@gmail.com","99999888", LocalDate.of(2003,9,3),"$2a$12$6UHtEvtcw1GAHNhKOJqvR.7F269fX6I7jS7iifdjPkuP0dSE0kQ2m");
        user.addRole(new Role(1L,"ROLE_CLIENT"));
        return user;
    }

    public static User createAdminUser(){
        User user = new User(1L,"Pedro","pedro@gmail.com","99999888", LocalDate.of(2003,9,3),"$2a$12$6UHtEvtcw1GAHNhKOJqvR.7F269fX6I7jS7iifdjPkuP0dSE0kQ2m");
        user.addRole(new Role(2L,"ROLE_ADMIN"));
        return user;
    }

    public static User createCustomUser(Long id, String name, String email){
        User user = new User(id,name,email,"99999888", LocalDate.of(2003,9,3),"$2a$12$6UHtEvtcw1GAHNhKOJqvR.7F269fX6I7jS7iifdjPkuP0dSE0kQ2m");
        user.addRole(new Role(1L,"ROLE_CLIENT"));
        return user;
    }
}
