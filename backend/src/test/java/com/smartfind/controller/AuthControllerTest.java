package com.smartfind.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mvc;

    private static final String REGISTER = """
            {"name":"Test User","email":"api.test@smartfind.com","phone":"9876543210","password":"Passw0rdX"}""";

    @Test
    void registerLoginAndProtectedEndpoints() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.role").value("USER"));

        // duplicate email -> 409
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
                .andExpect(status().isConflict());

        // wrong password -> 401
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"api.test@smartfind.com\",\"password\":\"wrongpass1\"}"))
                .andExpect(status().isUnauthorized());

        // correct login -> 200
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"api.test@smartfind.com\",\"password\":\"Passw0rdX\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // no token -> 401
        mvc.perform(get("/api/lost-items")).andExpect(status().isUnauthorized());
    }

    @Test
    void weakPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"email\":\"x@y.com\",\"phone\":\"9876543210\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }
}
