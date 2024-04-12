package org.geysermc.serverpingapi.models;

import com.nukkitx.protocol.bedrock.BedrockPong;

public record PingData(boolean tcpFirst, BedrockPong pong) {
}
