package com.studentreadiness.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentreadiness.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("render")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:render-admin-${random.uuid};DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.admin.bootstrap.email=render-admin@example.test",
        "app.admin.bootstrap.password=render-test-password-123"
})
@AutoConfigureMockMvc
class RenderAdminInitializerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void bootstrappedRenderAdminCanSignInAndPasswordIsStoredEncoded() throws Exception {
        var admin = userRepository.findByEmail("render-admin@example.test")
                .orElseThrow();

        assertTrue(admin.getPassword().startsWith("$2"));
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "render-admin@example.test",
                                "password", "render-test-password-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
}
