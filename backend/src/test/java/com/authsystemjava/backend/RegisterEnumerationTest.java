package com.authsystemjava.backend;

import com.authsystemjava.backend.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegisterEnumerationTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean EmailService emailService; // don't hit Resend in CI

    @Test
    void registerReturnsIdenticalResponseForNewAndExistingEmail() throws Exception {
        String body = "{\"email\":\"kev@test.com\",\"name\":\"Kev\",\"password\":\"Sup3rSecure-Passw0rd!\"}";

        String first = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String second = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertEquals(first, second); // byte-identical, per the README claim
    }

    @Test
    void registerRejectsShortPasswordWithStructuredError() throws Exception {
        String body = "{\"email\":\"kev@test.com\",\"name\":\"Kev\",\"password\":\"short\"}";

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields.password").exists());
    }
}