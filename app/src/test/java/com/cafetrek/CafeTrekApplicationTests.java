package com.cafetrek;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * The single most valuable test in this project: if Spring can't wire up
 * every bean (security config, JPA repositories, the web-push service,
 * etc.), this fails immediately in CI instead of showing up as a mysterious
 * crash on Render after deploy. Uses the H2 `local` profile so it needs no
 * external database.
 */
@SpringBootTest
@ActiveProfiles("local")
class CafeTrekApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: reaching this point means the application
        // context started successfully.
    }
}
