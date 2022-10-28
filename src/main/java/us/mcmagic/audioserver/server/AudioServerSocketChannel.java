package us.mcmagic.audioserver.server;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import us.mcmagic.audioserver.AudioServer;
import us.mcmagic.audioserver.handlers.Player;
import us.mcmagic.audioserver.packets.audio.PacketKick;

import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Marc on 6/15/15
 */
@SuppressWarnings("unchecked")
public class AudioServerSocketChannel extends NioSocketChannel {
    private static AtomicLong nextId = new AtomicLong(0L);

    protected long id = nextId.getAndIncrement();
    private int protocolVersion;
    private String username;
    private UUID uuid;

    public AudioServerSocketChannel() {
    }

    public AudioServerSocketChannel(SelectorProvider provider) {
        super(provider);
    }

    public AudioServerSocketChannel(SocketChannel socket) {
        super(socket);
    }

    public AudioServerSocketChannel(Channel parent, SocketChannel socket) {
        super(parent, socket);
    }

    public long getId() {
        return this.id;
    }

    public void send(String message) {
        writeAndFlush(new TextWebSocketFrame(message));
    }

    public boolean isPlayer(Player player) {
        return getUniqueId() != null && getUniqueId().equals(player.getUniqueId());
    }

    public void cleanup(String reason) {
        if (isOpen()) {
            if (reason != null)
                send(new PacketKick(reason).getJSON().toString());
            close();
        }
    }

    protected void doFinishConnect() throws Exception {
        super.doFinishConnect();
        synchronized (AudioServerSocketChannel.this) {
            if (isOpen()) {
                send(new PacketKick("You failed to authenticate yourself").getJSON().toString());
                close();
            }
        }
    }

    protected void doDisconnect() throws Exception {
        send(new PacketKick(AudioServer.GOODBYE_MESSAGE).getJSON().toString());
        super.doDisconnect();
    }

    protected void doClose() throws Exception {
        send(new PacketKick(AudioServer.GOODBYE_MESSAGE).getJSON().toString());
        super.doClose();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}