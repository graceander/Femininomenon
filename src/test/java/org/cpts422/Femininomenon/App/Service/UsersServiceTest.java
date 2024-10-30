package org.cpts422.Femininomenon.App.Service;

import static org.junit.jupiter.api.Assertions.*;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
class UsersServiceTest {
    @InjectMocks
    private UsersService usersService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testRegisterUser() {
        String firstName = "Matt";
        String lastName = "Pham";
        String login = "mattp";
        String password = "securePassword";
        String email = "matt@example.com";
        UserModel userModel = new UserModel();
        userModel.setFirstName(firstName);
        userModel.setLastName(lastName);
        userModel.setLogin(login);
        userModel.setPassword(password);
        userModel.setEmail(email);
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);
        UserModel registeredUser = usersService.registerUser(firstName, lastName, login, password, email);
        assertEquals(userModel, registeredUser);
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testRegisterUserNullLogin() {
        // Arrange
        String firstName = "Matt";
        String lastName = "Pham";
        String login = null;
        String password = "securePassword";
        String email = "matt@example.com";
        UserModel registeredUser = usersService.registerUser(firstName, lastName, login, password, email);
        assertNull(registeredUser);
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void testRegisterUser_NullPassword() {
        // Arrange
        String firstName = "Matt";
        String lastName = "Pham";
        String login = "mattp";
        String password = null;
        String email = "matt@example.com";
        UserModel registeredUser = usersService.registerUser(firstName, lastName, login, password, email);
        assertNull(registeredUser);
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void testSaveUser() {
        UserModel userModel = new UserModel();
        userModel.setFirstName("Matt");
        userModel.setLastName("Pham");
        userModel.setLogin("mattp");
        userModel.setPassword("securePassword");
        userModel.setEmail("matt@example.com");
        when(userRepository.save(userModel)).thenReturn(userModel);
        UserModel savedUser = usersService.saveUser(userModel);
        assertEquals(userModel, savedUser);
        verify(userRepository).save(userModel);
    }

    @Test
    void testAuthenticateUser() {
        String login = "mattp";
        String password = "securePassword";
        UserModel userModel = new UserModel();
        userModel.setLogin(login);
        userModel.setPassword(password);
        when(userRepository.findByLoginAndPassword(login, password)).thenReturn(Optional.of(userModel));
        UserModel authenticatedUser = usersService.authenticateUser(login, password);
        assertEquals(userModel, authenticatedUser);
        verify(userRepository).findByLoginAndPassword(login, password);
    }
    @Test
    void testFindByLogin() {
        String login = "mattp";
        UserModel userModel = new UserModel();
        userModel.setLogin(login);
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(userModel));
        UserModel foundUser = usersService.findByLogin(login);
        assertEquals(userModel, foundUser);
        verify(userRepository).findByLogin(login);
    }


}