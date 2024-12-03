package org.cpts422.Femininomenon.App.UnitTests.Controllers;


import org.cpts422.Femininomenon.App.Controllers.UserController;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {


    // comment from last meeting
    //mock mvc extension to spring framework
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;


    // Test for the register page
    @Test
    public void testRegisterPage() throws Exception {
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    // Test for successful user registration
    @Test
    public void testRegisterCorrect() throws Exception {
        UserModel mockUser = new UserModel();
        mockUser.setFirstName("Matthew");
        mockUser.setLastName("Pham");
        mockUser.setLogin("hello123");
        mockUser.setPassword("password");
        mockUser.setEmail("matthew@gmail.com");

        when(usersService.registerUser("Matthew", "Pham", "hello123", "password", "matthew@gmail.com")).thenReturn(mockUser);

        mockMvc.perform(post("/register").param("firstName", "Matthew").param("lastName", "Pham").param("login", "hello123").param("password", "password").param("email", "matthew@gmail.com")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }

    // Test for failed user registration
    @Test
    public void testRegisterFalse() throws Exception {
        when(usersService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/register")
                .param("firstName", "Matthew")
                .param("lastName", "Pham")
                .param("login", "hello")
                .param("password", "password")
                .param("email", "matthew@gmail.com")).andExpect(status().isOk());

    }

    // Test for the login page
    @Test
    public void testIndexPage() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    // Test for successful login
    @Test
    public void testLoginSuccessful() throws Exception {
        UserModel mockUser = new UserModel();
        mockUser.setLogin("Matt");
        mockUser.setPassword("password");
        when(usersService.authenticateUser("Matt", "password")).thenReturn(mockUser);
        mockMvc.perform(post("/login").param("login", "Matt")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home?login=Matt"));
    }

    // Test for failed login
    @Test
    public void testLoginFailed() throws Exception {
        when(usersService.authenticateUser("Matt", "wrongpassword")).thenReturn(null);
        mockMvc.perform(post("/login").param("login", "Matt")
                .param("password", "wrongpassword")).andExpect(status().isOk()).andExpect(view().name("error"));
    }
}
