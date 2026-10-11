package org.geysermc.serverpingapi.models;

/**
 * @param pong A {@link com.nukkitx.protocol.bedrock.BedrockPong} for RakNet, or the {@code /v1/join} status document for NetherNet
 */
public record PingData(PingTransport transport, boolean tcpFirst, Object pong) {
}
