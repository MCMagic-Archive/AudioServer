package us.mcmagic.audioserver.server;

import io.netty.channel.socket.nio.NioServerSocketChannel;
import us.mcmagic.audioserver.AudioServer;

import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by Marc on 6/15/15
 */
public class AudioServerServerSocketChannel extends NioServerSocketChannel {

    protected int doReadMessages(List<Object> buf)
            throws Exception {
        SocketChannel ch = javaChannel().accept();
        try {
            if (ch != null) {
                buf.add(new AudioServerSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            AudioServer.getLogger().error("Failed to create a new channel from an accepted socket.", t);
            try {
                ch.close();
            } catch (Throwable t2) {
                AudioServer.getLogger().error("Failed to close a socket.", t2);
            }
        }

        return 0;
    }
}