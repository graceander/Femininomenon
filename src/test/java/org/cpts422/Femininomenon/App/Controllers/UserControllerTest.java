package org.cpts422.Femininomenon.App.Controllers;


import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class UserControllerTest {
    @Mock
    private UsersService usersService;
    @Mock
    private Model model;
    @InjectMocks
    private UserController userController;
    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
    }

    //mock mvc extension to spring framework

    // this test function will test if it can get to the register page
    @Test
    public void testRegisterPage() {
        String viewName = userController.getRegisterPage(model);
        verify(model, times(1)).addAttribute(eq("registerRequest"), any(UserModel.class));
        assertEquals("register", viewName);
    }


    // this test will check if it can register a new user.
    @Test
    public void testRegisterCorrect() {
        UserModel mockUser = new UserModel();
        mockUser.setFirstName("Matthew");
        mockUser.setLastName("Pham");
        mockUser.setLogin("hello123");
        mockUser.setPassword("password");
        mockUser.setEmail("matthew@gmail.com");
        when(usersService.registerUser("Matthew", "Pham", "hello123", "password", "matthew@gmail.com")).thenReturn(mockUser);
        String result = userController.register(mockUser);
        assertEquals("redirect:/", result);
    }


    // this will test the error handling of the register page
    @Test
    public void testRegisterFalse() {
        UserModel mockUser = new UserModel();
        mockUser.setFirstName("Matthew");
        mockUser.setLastName("Pham");
        mockUser.setLogin("hello");
        mockUser.setPassword("password");
        mockUser.setEmail("matthew@gmail.com");
        when(usersService.registerUser("Matthew", "Pham", "hello123", "password", "matthew@gmail.com")).thenReturn(null);
        String result = userController.register(mockUser);
        assertEquals("error", result);
    }
    // this will test the login page
    @Test
    public void testIndexPage() {
        String viewName = userController.index(model);
        verify(model, times(1)).addAttribute(eq("loginRequest"), any(UserModel.class));
        assertEquals("index", viewName);
    }

    
    // this will test the login if it's successful
    @Test
    public void testLoginSuccessful() {
        UserModel mockUser = new UserModel();
        mockUser.setLogin("Matt");
        mockUser.setPassword("password");
        when(usersService.authenticateUser("Matt", "password")).thenReturn(mockUser);
        String result = userController.login(mockUser);
        assertEquals("redirect:/home?login=Matt", result);
    }
    // this will test if the login failed like username or password wrong
    @Test
    public void testLoginFailed() {
        UserModel mockUser = new UserModel();
        mockUser.setLogin("Matt");
        mockUser.setPassword("wrongpassword");
        when(usersService.authenticateUser("Matt", "password")).thenReturn(null);
        String result = userController.login(mockUser);
        assertEquals("error", result);
    }


}

