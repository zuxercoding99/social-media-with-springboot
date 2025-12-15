package org.example.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test de integración del flujo completo de autenticación con JWT:
 * 1. Registro de usuario
 * 2. Login (devuelve accessToken y refresh_token en cookie HttpOnly)
 * 3. Refresh (renueva el accessToken usando la cookie)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void loginAndRefreshFlow_shouldReturnNewAccessToken() throws Exception {

                // 🧩 1️⃣ Registrar nuevo usuario
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                    {
                                                      "username": "testuser",
                                                      "password": "123456",
                                                      "email": "t@t.com",
                                                      "birthDate": "2000-01-01"
                                                    }
                                                """))
                                .andExpect(status().isOk());

                // 🔐 2️⃣ Login -> devuelve accessToken y refresh_token
                MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                    {
                                                      "email": "t@t.com",
                                                      "password": "123456"
                                                    }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("refresh_token"))
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andReturn();

                // 🧠 Obtener valor dinámico del refresh_token desde la respuesta
                String refreshValue = loginResult.getResponse()
                                .getCookie("refresh_token")
                                .getValue();

                // Crear una MockCookie con el valor dinámico
                MockCookie refreshCookie = new MockCookie("refresh_token", refreshValue);
                refreshCookie.setPath("/api/v1/auth/");
                refreshCookie.setHttpOnly(true);

                // ♻️ 3️⃣ Llamar al endpoint /refresh usando la cookie
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .cookie(refreshCookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists());
        }
}
