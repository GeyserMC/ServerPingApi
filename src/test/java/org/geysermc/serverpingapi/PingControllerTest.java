package org.geysermc.serverpingapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PingControllerTest {

    @Autowired
    private PingController controller;

    @Test
    public void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }

    @Test
    public void bedrockPings() throws Exception {
        PingResponse response = controller.ping("test.geysermc.org", 19132, null);
        assertThat(response).isNotNull();
        assertThat(response.success()).isTrue();
        assertThat(response.query()).isNotNull();
        assertThat(response.ping()).isNotNull();
    }

    @Test
    public void badHostname() throws Exception {
        PingResponse response = controller.ping("invalid.domain", 19132, null);
        assertThat(response).isNotNull();
        assertThat(response.success()).isFalse();
        assertThat(response.message()).isNotNull();
        assertThat(response.message()).isNotBlank();
    }

    @Test
    public void badPort() throws Exception {
        PingResponse response = controller.ping("test.geysermc.org", -1, null);
        assertThat(response).isNotNull();
        assertThat(response.success()).isFalse();
        assertThat(response.message()).isNotNull();
        assertThat(response.message()).isNotBlank();
    }

}