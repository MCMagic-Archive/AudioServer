package us.mcmagic.audioserver.dashboard;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import us.mcmagic.audioserver.AudioServer;
import us.mcmagic.audioserver.AudioServerHandler;
import us.mcmagic.audioserver.handlers.Player;
import us.mcmagic.audioserver.packets.BasePacket;
import us.mcmagic.audioserver.packets.audio.PacketContainer;
import us.mcmagic.audioserver.packets.audio.PacketGetPlayer;
import us.mcmagic.audioserver.packets.audio.PacketPlayerInfo;
import us.mcmagic.audioserver.packets.dashboard.PacketConnectionType;
import us.mcmagic.audioserver.packets.dashboard.PacketPlayerChat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Created by Marc on 5/22/16
 */
public class DashboardConnection {
    protected WebSocketClient ws;
    private boolean attempted = false;

    public DashboardConnection() throws URISyntaxException {
        connect();
    }

    private void connect() throws URISyntaxException {
        ws = new WebSocketClient(new URI("ws://socket.dashboard.mcmagic.us:7892"), new Draft_10()) {
            @Override
            public void onMessage(String message) {
                JsonObject object = (JsonObject) new JsonParser().parse(message);
                if (!object.has("id")) {
                    return;
                }
                int id = object.get("id").getAsInt();
                AudioServer.getLogger().info(object.toString());
                switch (id) {
                    case 14: {
                        PacketPlayerInfo packet = new PacketPlayerInfo().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        String username = packet.getUsername();
                        int auth = packet.getAuth();
                        String server = packet.getServer();
                        AudioServerHandler.verifyPlayer(uuid, username, auth, server);
                        break;
                    }
                    case 17: {
                        PacketContainer packet = new PacketContainer().fromJSON(object);
                        UUID uuid = packet.getUniqueId();
                        String container = packet.getContainer();
                        Player tp = AudioServer.getPlayer(uuid);
                        if (tp == null) {
                            return;
                        }
                        AudioServer.sendStringifiedMessage(tp, container);
                    }
                }
            }

            @Override
            public void onOpen(ServerHandshake handshake) {
                AudioServer.getLogger().info("Successfully connected to Dashboard");
                DashboardConnection.this.send(new PacketConnectionType(PacketConnectionType.ConnectionType.AUDIOSERVER)
                        .getJSON().toString());
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                AudioServer.getLogger().warn(code + " Disconnected from Dashboard! Shutting Audio Server...");
                System.exit(0);
            }

            @Override
            public void onError(Exception ex) {
                AudioServer.getLogger().warn("Error in Dashboard connection");
                ex.printStackTrace();
            }

        };
        ws.connect();
    }

    public void send(String s) {
        ws.send(s);
    }

    public boolean isConnected() {
        return ws != null && ws.getConnection() != null;
    }

    public void verifyPlayer(String username) {
        PacketGetPlayer packet = new PacketGetPlayer(username);
        send(packet.getJSON().toString());
    }

    public void send(BasePacket packet) {
        send(packet.getJSON().toString());
    }

    public void playerChat(UUID uuid, String message) {
        PacketPlayerChat packet = new PacketPlayerChat(uuid, message);
        send(packet);
    }

    private String formatName(String s) {
        String ns = "";
        if (s.length() < 4) {
            for (char c : s.toCharArray()) {
                ns += Character.toString(Character.toUpperCase(c));
            }
            return ns;
        }
        Character last = null;
        for (char c : s.toCharArray()) {
            if (last == null) {
                last = c;
                ns += Character.toString(Character.toUpperCase(c));
                continue;
            }
            if (Character.toString(last).equals(" ")) {
                ns += Character.toString(Character.toUpperCase(c));
            } else {
                ns += Character.toString(c);
            }
            last = c;
        }
        return ns;
    }
}