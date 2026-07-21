package com.tvtracker.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tvtracker.common.exception.NotFoundException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class HealthControllerTest {

    private final HealthController controller = new HealthController();

    @Test
    void healthReturnsOk() {
        ResponseEntity<Map<String, String>> response = controller.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody().get("status"));
    }

    @Test
    void demoErrorWithoutTriggerReturnsOk() {
        ResponseEntity<Void> response = controller.demoError(false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void demoErrorWithTriggerThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> controller.demoError(true));
    }
}
