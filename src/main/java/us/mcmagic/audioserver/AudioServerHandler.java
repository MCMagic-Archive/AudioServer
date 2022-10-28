package us.mcmagic.audioserver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import us.mcmagic.audioserver.handlers.Player;
import us.mcmagic.audioserver.packets.PacketID;
import us.mcmagic.audioserver.packets.audio.PacketClientAccept;
import us.mcmagic.audioserver.packets.audio.PacketKick;
import us.mcmagic.audioserver.packets.audio.PacketLogin;
import us.mcmagic.audioserver.packets.audio.PacketWarp;
import us.mcmagic.audioserver.server.AudioServerSocketChannel;
import us.mcmagic.audioserver.server.WebSocketServerHandler;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Marc on 6/15/15
 */

public class AudioServerHandler {
    private static HashMap<String, PacketLogin> authMap = new HashMap<>();
    private static Integer[] clientPackets = new Integer[]{3, 4, 5, 7, 8, 9, 10, 11, 12};

    public static void handleOpen(ChannelHandlerContext ctx, FullHttpRequest req) {
        final AudioServerSocketChannel channel = (AudioServerSocketChannel) ctx.channel();
        AudioServer.getLogger().info("onOpen " + channel.remoteAddress().getAddress().getHostAddress());
    }

    public static void verifyPlayer(UUID uuid, String username, int auth, String server) {
        PacketLogin packet = authMap.remove(username);
        AudioServerSocketChannel channel = null;
        for (Object o : WebSocketServerHandler.getGroup()) {
            AudioServerSocketChannel ch = (AudioServerSocketChannel) o;
            if (ch.getUsername() == null) {
                continue;
            }
            if (ch.getUsername().equalsIgnoreCase(username)) {
                channel = ch;
                break;
            }
        }
        if (channel == null) {
            channel.send(new PacketKick("Authorization failed. Please try to connect again!").getJSON().toString());
            channel.close();
            return;
        }
        if (isInt(packet.getAuth()) && auth == Integer.valueOf(packet.getAuth())) {
            Player player = new Player(uuid, username, channel.remoteAddress());
            AudioServer.addPlayer(player);
            channel.setUniqueId(uuid);
            channel.setProtocolVersion(packet.getProtocolVersion());
            channel.send(new PacketClientAccept(server).getJSON().toString());
            player.sendMessage("&aYou connected to the MCMagic Audio Server!");
            player.chat("/as loginsync");
        } else {
            channel.send(new PacketKick("Authorization failed. Please try to connect again!").getJSON().toString());
            channel.close();
        }
    }

    private static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static void handleClose(ChannelHandlerContext ctx) {
        AudioServerSocketChannel channel = (AudioServerSocketChannel) ctx.channel();
        Player player = null;
        String playerLog;
        if (channel.getUniqueId() != null) {
            player = AudioServer.getPlayer(channel.getUniqueId());
        }
        AudioServer.getLogger().info("onClose " + channel.remoteAddress().getAddress().getHostAddress());
        if (player != null) {
            player.sendMessage("&aYou left the MCMagic Audio Server!");
            AudioServer.removePlayer(player.getUniqueId());
        }
        channel.setUniqueId(null);
        channel.setUsername(null);
    }

    public static void handleFrame(ChannelHandlerContext ctx, WebSocketFrame frame, String message) {
        AudioServerSocketChannel channel = (AudioServerSocketChannel) ctx.channel();
        Player player = null;
        String playerLog;
        if (channel.getUniqueId() != null) {
            player = AudioServer.getPlayer(channel.getUniqueId());
            playerLog = " (" + player.getUsername() + ") ";
        } else {
            playerLog = "unknown";
        }
        try {
            JsonObject object = (JsonObject) new JsonParser().parse(message);
            if (object.has("id")) {
                int id = object.get("id").getAsInt();
                if (player != null) {
                    if (id == PacketID.INCOMING_WARP.getID()) {
                        PacketWarp packet = new PacketWarp().fromJSON(object);
                        AudioServer.getPlayer(player.getUniqueId()).chat("/warp " + packet.getWarpName());
                    } else {
                        AudioServer.getLogger().warn("Received unknown packet from IP " +
                                channel.remoteAddress().getAddress().getHostAddress() + playerLog + " with id " + id +
                                " value '" + message + "'");
                        if (channel.isOpen()) {
                            channel.send(new PacketKick("Data corruption detected. Please reload this webpage!")
                                    .getJSON().toString());
                            channel.close();
                        }
                    }
                } else {
                    if (id == PacketID.LOGIN.getID()) {
                        if (!AudioServer.getDashboardConnection().isConnected()) {
                            channel.send(new PacketKick("Error establishing connection to Dashboard!").getJSON().toString());
                            channel.close();
                            return;
                        }
                        PacketLogin login = new PacketLogin().fromJSON(object);
                        if (channel.isOpen()) {
                            if (login.getProtocolVersion() > AudioServer.PROTOCOL_VERSION_INT) {
                                channel.send(new PacketKick("You are using a future version of the Audio Server " +
                                        "client which is currently unsupported!").getJSON().toString());
                                channel.close();
                                AudioServer.getLogger().warn("Future protocol from IP " +
                                        channel.remoteAddress().getAddress().getHostAddress() + playerLog +
                                        " with id " + id + " value '" + message + "'");
                                return;
                            } else if (login.getProtocolVersion() < AudioServer.PROTOCOL_VERSION_MIN_COMPAT) {
                                channel.send(new PacketKick("You are using an outdated Audio Server client!")
                                        .getJSON().toString());
                                channel.close();
                                AudioServer.getLogger().warn("Outdated protocol from IP " +
                                        channel.remoteAddress().getAddress().getHostAddress() + playerLog +
                                        " with id " + id + " value '" + message + "'");
                                return;
                            } else if (AudioServer.isPlayerOnline(login.getPlayerName())) {
                                channel.send(new PacketKick("You are already connected to the Audio Server!")
                                        .getJSON().toString());
                                channel.close();
                                return;
                            }
                            channel.setUsername(login.getPlayerName());
                            authMap.put(login.getPlayerName(), login);
                            AudioServer.requestPlayer(login.getPlayerName());
                        }
                    } else {
                        AudioServer.getLogger().warn("Received unknown packet from IP " + channel.remoteAddress()
                                .getAddress().getHostAddress() + playerLog + " with id " + id + " value '" + message +
                                "' isOpen? " + channel.isOpen());
                        if (channel.isOpen()) {
                            channel.send(new PacketKick("Unknown protocol subset used in a TextFrame send by the client")
                                    .getJSON().toString());
                            channel.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            AudioServer.getLogger().error("Error on packet handling with message {" + message + "}", e);
            if (channel.isOpen()) {
                channel.send(new PacketKick("Data corruption detected. Please reload this webpage!").getJSON().toString());
                channel.close();
            }
        }
    }

    public static void handleError(ChannelHandlerContext ctx, Throwable cause) {
        AudioServerSocketChannel channel = (AudioServerSocketChannel) ctx.channel();
        Player player = null;
        if (channel.getUniqueId() != null) {
            player = AudioServer.getPlayer(channel.getUniqueId());
        }
        String playerLog = " (" + player.getUsername() + ") ";
        AudioServer.getLogger().warn("Error occured on IP " + channel.remoteAddress().getAddress().getHostAddress() +
                playerLog + " with value '" + cause.getMessage() + "'");
        channel.cleanup("An unknown error occured");
    }
}