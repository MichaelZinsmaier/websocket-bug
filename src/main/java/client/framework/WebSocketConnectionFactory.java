/* Copyrights owned by Atos and Siemens, 2015. */
package client.framework;

import io.netty.channel.nio.NioEventLoopGroup;

public class WebSocketConnectionFactory {

    private static WebSocketConnectionFactory instance = null;

    private NioEventLoopGroup group = new NioEventLoopGroup();

    public static WebSocketConnectionFactory getInstance() {
        if (WebSocketConnectionFactory.instance == null) {
            WebSocketConnectionFactory.instance = new WebSocketConnectionFactory();
        }
        return instance;
    }

    public WsConnection createConnection(String host, int port, String conPath, IReceiver receiveHandler) {
        int DEFAULT_SIZE = 65536;
        return createConnection(host, port, conPath, receiveHandler, DEFAULT_SIZE);
    }

    public WsConnection createConnection(String host, int port, String conPath, IReceiver receiveHandler, int maxSizeWS) {
        WebSocketClientConnection clientCon = new WebSocketClientConnection(group, host, port, conPath, receiveHandler, maxSizeWS);
        return new WsConnection(clientCon);
    }

    public void shutdown() {
        try {
            group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
