/* Copyrights owned by Atos and Siemens, 2015. */
package client.framework;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;

import java.net.URI;

public class WebSocketClientConnection implements IPingPongHandler {

    private String hostname;
    private String path;
    private int port;
    private int maxSizeWS;
    private IReceiver receiveHandler;



    private HttpHeaders headers = new DefaultHttpHeaders().add("Authorization", "actually-not-tested");
    private Bootstrap bootstrap = new Bootstrap();

    private URI uri;
    private WebSocketClientHandshaker handshaker;
    private ConnectionHandler handler;
    private Channel channel = null;
    private NioEventLoopGroup group;


    protected WebSocketClientConnection(NioEventLoopGroup group, String hostname, int port, String path, IReceiver receiveHandler, int maxSizeWS) {
        this.group = group;
        this.hostname = hostname;
        this.path = path;
        this.port = port;
        this.maxSizeWS = maxSizeWS;
        this.receiveHandler = receiveHandler;

        try {
            uri = new URI("ws://" + hostname + ":" + port + "/" + path);
        } catch (Exception e) {
            throw new IllegalArgumentException("was not able to create the URI from host, port and path");
        }
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, headers, maxSizeWS);

        handler = new ConnectionHandler(handshaker, receiveHandler, this);
    }


    /**
     * Connects to the server.
     */
    protected void connect() {
        if (channel == null) {
            ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel channel) {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), handler);
                }
            };

            bootstrap.group(group).channel(NioSocketChannel.class).handler(initializer);

            try {
                channel = bootstrap.connect(hostname, port).sync().channel();
                handler.handshake().sync();
            } catch (Exception e) {
                System.out.println("Connection: error during shutdown " + e.getMessage());
            }
        } else {
            throw new IllegalStateException("Channel is already open.");
        }
    }

    /**
     * Sends something over the connection.
     */
    protected ChannelFuture send(byte[] data) {
        if (channel != null) {
            BinaryWebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.wrappedBuffer(data));
            return channel.writeAndFlush(frame);
        } else {
            throw new IllegalStateException("Channel is not open.");
        }
    }

    /**
     * Shuts down the client.
     */
    protected void shutdown() {
        try {
            if (channel != null) {
                channel.writeAndFlush(new CloseWebSocketFrame());
                channel.closeFuture().sync();
            }

            System.out.println("Connection: Shutdown.");
        } catch (Exception e) {
            System.out.println("Connection: error during shutdown " + e.getMessage());
        }
    }

    @Override
    public void receivedPing() {
        System.out.println("Connection: Received a ping - answered with pong");
        send(new PongWebSocketFrame().content().array());
    }

    @Override
    public void receivedPong()
    {
        System.out.println("Connection: Received a pong - ignored it");
    }
}
