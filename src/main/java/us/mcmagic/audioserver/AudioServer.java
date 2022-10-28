package us.mcmagic.audioserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import us.mcmagic.audioserver.dashboard.DashboardConnection;
import us.mcmagic.audioserver.handlers.Player;
import us.mcmagic.audioserver.server.AudioServerServerSocketChannel;
import us.mcmagic.audioserver.server.AudioServerSocketChannel;
import us.mcmagic.audioserver.server.WebSocketServerHandler;
import us.mcmagic.audioserver.server.WebSocketServerInitializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Created by Marc on 6/15/15
 */
public class AudioServer {
    private static DashboardConnection dashboardConnection;
    public static final String GOODBYE_MESSAGE = "See ya real soon!";
    public static final int PROTOCOL_VERSION_INT = 8;
    public static final int PROTOCOL_VERSION_MIN_COMPAT = 8;
    public static String HOST = "localhost";
    public static final int PORT = 8246;
    private static HashMap<UUID, Player> playerMap = new HashMap<>();
    private static Logger logger = Logger.getLogger("AudioServer");

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                getLogger().info("Shutting down Audio Server...");
            }
        });
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        HOST = in.readLine();
        PatternLayout layout = new PatternLayout("[%d{HH:mm:ss}] [%p] - %m%n");
        logger.addAppender(new ConsoleAppender(layout));
        logger.addAppender(new FileAppender(layout, "audioserver.log", true));
        try {
            dashboardConnection = new DashboardConnection();
        } catch (URISyntaxException e) {
            AudioServer.getLogger().error("Error", e);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!dashboardConnection.isConnected()) {
                    getLogger().error("No Dashboard connection, shutting down Audio Server");
                    System.exit(0);
                }
            }
        }, 3000);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logger.info("Connected Players: " + playerMap.size());
            }
        }, 0, 300000);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            getLogger().info("Trying to bind to " + HOST + ":" + PORT);
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(AudioServerServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer());
            Channel ch = b.bind(new InetSocketAddress(HOST, PORT)).sync().channel();
            getLogger().info("Audio Server has been started on " + HOST + ":" + PORT);
            ch.closeFuture().sync();
        } catch (Exception e) {
            AudioServer.getLogger().error("Error with Socket", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static DashboardConnection getDashboardConnection() {
        return dashboardConnection;
    }

    public static HashMap<UUID, Player> getPlayerMap() {
        return new HashMap<>(playerMap);
    }

    public static Logger getLogger() {
        return logger;
    }

    public static boolean isPlayerOnline(String username) {
        return getPlayer(username) != null;
    }

    public static boolean isPlayerOnline(UUID uuid) {
        for (Object o : WebSocketServerHandler.getGroup()) {
            AudioServerSocketChannel channel = (AudioServerSocketChannel) o;
            if (uuid.equals(channel.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public static void disconnect(Player player, String reason) {
        for (Object o : WebSocketServerHandler.getGroup()) {
            AudioServerSocketChannel channel = (AudioServerSocketChannel) o;
            if (channel.isPlayer(player))
                channel.cleanup(reason);
        }
    }

    public static void sendStringifiedMessage(Player player, String message) {
        for (Object o : WebSocketServerHandler.getGroup()) {
            AudioServerSocketChannel channel = (AudioServerSocketChannel) o;
            if (channel.getUniqueId() == null) {
                continue;
            }
            if (channel.getUniqueId().equals(player.getUniqueId())) {
                channel.send(message);
                return;
            }
        }
    }

    public static Player getPlayer(UUID uuid) {
        return getPlayerMap().get(uuid);
    }

    public static Player getPlayer(String username) {
        for (Map.Entry<UUID, Player> entry : getPlayerMap().entrySet()) {
            Player pl = entry.getValue();
            if (pl.getUsername().equalsIgnoreCase(username)) {
                return pl;
            }
        }
        return null;
    }

    public static void requestPlayer(String name) {
        getDashboardConnection().verifyPlayer(name);
    }

    public static void addPlayer(Player player) {
        playerMap.put(player.getUniqueId(), player);
    }

    public static void removePlayer(UUID uuid) {
        playerMap.remove(uuid);
    }

    public static void clearPlayers() {
        playerMap.clear();
    }
}