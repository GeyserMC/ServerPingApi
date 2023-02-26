package org.geysermc.serverpingapi;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public void health(HttpServletResponse response) {
        response.setStatus(204);
    }
}
