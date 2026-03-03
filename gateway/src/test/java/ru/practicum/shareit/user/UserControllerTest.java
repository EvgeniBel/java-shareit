package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    private UserDto validUserDto;

    @BeforeEach
    void setUp() {
        validUserDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();
    }

    @Test
    void testGetUsers() throws Exception {
        when(userClient.getUsers())
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserByIdWithValidId() throws Exception {
        when(userClient.getUserById(1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUserWithValidData() throws Exception {
        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUserWithNullName() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .name(null)
                .email("john@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserWithBlankName() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .name("   ")
                .email("john@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserWithNullEmail() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .name("John")
                .email(null)
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserWithBlankEmail() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .name("John")
                .email("   ")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserWithInvalidEmail() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .name("John")
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserWithValidData() throws Exception {
        when(userClient.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateUserWithInvalidEmail() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .email("not-an-email")
                .build();

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteUserWithValidId() throws Exception {
        when(userClient.deleteUser(1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}