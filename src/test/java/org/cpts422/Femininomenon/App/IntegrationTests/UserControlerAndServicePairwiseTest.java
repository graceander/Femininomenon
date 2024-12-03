package org.cpts422.Femininomenon.App.IntegrationTests;

import org.cpts422.Femininomenon.App.Controllers.UserController;
import org.cpts422.Femininomenon.App.Models.UserModel;
import org.cpts422.Femininomenon.App.Service.UsersService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControlerAndServicePairwiseTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;

    @Test
    public void testRegisterUserValidInputs() throws Exception {
        UserModel userModel = new UserModel();
        userModel.setFirstName("Matthew");
        userModel.setLastName("Pham");
        userModel.setEmail("MattP@gmail.com");
        userModel.setPassword("hello12345");
        userModel.setLogin("matthewpham");

        when(usersService.registerUser(eq("Matthew"), eq("Pham"), eq("matthewpham"), eq("hello12345"), eq("MattP@gmail.com"))).thenReturn(userModel);

        mockMvc.perform(post("/register").param("firstName", "Matthew").param("lastName", "Pham").param("login", "matthewpham").param("password", "hello12345")
                        .param("email", "MattP@gmail.com")).andExpect(status().isFound()).andExpect(redirectedUrl("/"));

    }

    @Test
    public void testRegisterUserInvalidFirstName() throws Exception {
        when(usersService.registerUser(any(), any(), any(), any(), any())).thenReturn(null);

        mockMvc.perform(post("/register").param("firstName", "").param("lastName", "Pham").param("login", "matthewpham")
                        .param("password", "Password123").param("email", "MattP@gmail.com")).andExpect(status().isOk()).andExpect(view().name("error"));
    }

    @Test
    public void testRegisterUserInvalidEmail() throws Exception {
        when(usersService.registerUser(any(), any(), any(), any(), any())).thenReturn(null);
        mockMvc.perform(post("/register").param("firstName", "Matthew").param("lastName", "Pham").param("login", "matthewpham")
                        .param("password", "Password123").param("email", "@@@@@@.com")).andExpect(status().isOk()).andExpect(view().name("error"));
    }

    @Test
    public void testRegisterUserInvalidPassword() throws Exception {
        when(usersService.registerUser(any(), any(), any(), any(), any())).thenReturn(null);

        mockMvc.perform(post("/register").param("firstName", "Matthew").param("lastName", "Pham").param("login", "matthewpham").param("password", "")
                        .param("email", "MattP@gmail.com")).andExpect(status().isOk()).andExpect(view().name("error"));
    }

    @Test
    public void testRegisterUserInvalidLogin() throws Exception {
        when(usersService.registerUser(any(), any(), any(), any(), any())).thenReturn(null);
        mockMvc.perform(post("/register").param("firstName", "Matthew").param("lastName", "Pham").param("login", "")
                        .param("password", "Password123").param("email", "MattP@gmail.com")).andExpect(status().isOk()).andExpect(view().name("error"));
    }



}
