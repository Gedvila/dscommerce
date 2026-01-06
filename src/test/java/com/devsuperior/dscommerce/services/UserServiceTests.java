package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.UserDTO;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.projections.UserDetailsProjection;
import com.devsuperior.dscommerce.repositories.UserRepository;
import com.devsuperior.dscommerce.test.UserDetailsFactory;
import com.devsuperior.dscommerce.test.UserFactory;
import com.devsuperior.dscommerce.util.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserUtil customUserUtil;

    private User user;
    private String existingEmail;
    private String nonExistingEmail;
    private List<UserDetailsProjection> userDetails;

    @BeforeEach
    void setUp() throws Exception{

        existingEmail = "joao@gmail.com";
        nonExistingEmail = "a@gmail.com";

        user = UserFactory.createCustomUser(1L,"joao",existingEmail);
        userDetails = UserDetailsFactory.createCustomAdminUser(existingEmail);


        Mockito.when(userRepository.searchUserAndRoleByEmail(existingEmail)).thenReturn(userDetails);
        Mockito.when(userRepository.searchUserAndRoleByEmail(nonExistingEmail)).thenReturn(new ArrayList<>());

        Mockito.when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());

    }

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists(){

        UserDetails result = userService.loadUserByUsername(existingEmail);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getUsername(),existingEmail);
    }

    @Test
    public void loadUserByUsernameShouldThrowUserNotFoundExceptionWhenUserDoesNotExist(){

        Assertions.assertThrows(UsernameNotFoundException.class,() ->{
            userService.loadUserByUsername(nonExistingEmail);
        });
    }

    @Test
    public void authenticatedShouldReturnUserWhenUserExist(){

        Mockito.when(customUserUtil.getLoggedUsername()).thenReturn(existingEmail);

        User result = userService.authenticated();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getUsername(),existingEmail);
    }

    @Test
    public void authenticatedShouldThrowUserNotFoundExceptionWhenUserDoesNotExist(){

        Mockito.doThrow(ClassCastException.class).when(customUserUtil).getLoggedUsername();

        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            userService.authenticated();
        });
    }

    @Test
    public void getMeShouldReturnUserDTOWhenUserAuthenticated(){

        UserService spyUserService = Mockito.spy(userService);
        Mockito.doReturn(user).when(spyUserService).authenticated();

        UserDTO result = spyUserService.getMe();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingEmail,result.getEmail());
    }

    @Test
    public void getMeShouldThrowUsernameNotFoundExceptionWhenUserNotAuthenticated(){
        UserService spyUserService = Mockito.spy(userService);
        Mockito.doThrow(UsernameNotFoundException.class).when(spyUserService).authenticated();

        Assertions.assertThrows(UsernameNotFoundException.class,()->{
            UserDTO result = spyUserService.getMe();
        });
    }
}