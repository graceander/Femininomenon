package org.cpts422.Femininomenon.App.IntegrationTests;

import org.cpts422.Femininomenon.App.Controllers.UserController;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserControlerAndServicePairwiseTest {
    @Mock
    private UsersService usersService;
    @InjectMocks
    private UserController userController;
    private UserModel userModel;
    @BeforeEach
    public void setUp()
    {
        userModel = new UserModel();
    }



    @Test
    public void testRegisterUser_ValidInputs() {
        userModel.setFirstName("Matthew");
        userModel.setLastName("Pham");
        userModel.setEmail("MattP@gmail.com");
        userModel.setPassword("hello12345");
        userModel.setLogin("matthewpham");
        when(usersService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(userModel);
        String result = userController.register(userModel);
        assertEquals("redirect:/", result);
        verify(usersService).registerUser("Matthew", "Pham", "hello12345", "a", "MattP@gmail.com");
    }

    @Test
    public void testRegisterUser_InvalidFirstName() {
        userModel.setFirstName("");
        userModel.setLastName("Pham");
        userModel.setEmail("MattP@gmail.com");
        userModel.setPassword("Password123");
        userModel.setLogin("matthewpham");
        when(usersService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(null);
        String result = userController.register(userModel);
        assertEquals("error", result);
    }

    @Test
    public void testRegisterUser_InvalidEmail() {
        userModel.setFirstName("Matthew");
        userModel.setLastName("Pham");
        userModel.setEmail("@@@@@@.com");
        userModel.setPassword("Password123");
        userModel.setLogin("matthewpham");
        when(usersService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(null);
        String result = userController.register(userModel);
        assertEquals("error", result);
    }
    @Test
    public void testRegisterUser_InvalidPassword() {

        userModel.setFirstName("Matthew");
        userModel.setLastName("Pham");
        userModel.setEmail("MattP@gmail.com");
        userModel.setPassword("");
        userModel.setLogin("matthewpham");
        when(usersService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(null);
        String result = userController.register(userModel);
        assertEquals("error", result);
    }


    @Test
    public void testRegisterUser_InvalidLogin() {
        userModel.setFirstName("Matthew");
        userModel.setLastName("Pham");
        userModel.setEmail("MattP@gmail.com");
        userModel.setPassword("Password123");
        userModel.setLogin("");
        when(usersService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(null);
        String result = userController.register(userModel);
        assertEquals("error", result);
    }






}
