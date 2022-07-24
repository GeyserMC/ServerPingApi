package org.geysermc.serverpingapi;

import com.nukkitx.protocol.bedrock.BedrockPong;

public record PingData(boolean tcpFirst, BedrockPong pong) {
}
