package us.mcmagic.audioserver.handlers;

import us.mcmagic.audioserver.AudioServer;
import us.mcmagic.audioserver.packets.BasePacket;
import us.mcmagic.audioserver.packets.dashboard.PacketMessage;
import us.mcmagic.audioserver.packets.dashboard.PacketPlayerChat;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * Created by Marc on 5/21/16
 */
public class Player {
    private UUID uuid;
    private String username;
    private InetSocketAddress address;
    private String server;

    public Player(UUID uuid, String username, InetSocketAddress address) {
        this.uuid = uuid;
        this.username = username;
        this.address = address;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public String getServer() {
        return server;
    }

    public void chat(String msg) {
        sendPacket(new PacketPlayerChat(uuid, msg));
    }

    public void sendMessage(String msg) {
        sendPacket(new PacketMessage(uuid, msg));
    }

    private void sendPacket(BasePacket p) {
        AudioServer.getDashboardConnection().send(p);
    }
}
