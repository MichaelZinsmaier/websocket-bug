/* Copyrights owned by Atos and Siemens, 2015. */
package client.framework;

import io.netty.channel.ChannelFuture;

public class WsConnection {

    private WebSocketClientConnection con;

    protected WsConnection(WebSocketClientConnection con) {
        this.con = con;
    }

    /** blocking call, will connect to the server or fail */
    public void connect() {
        con.connect();
    }

    /** non-blocking call, returns a Netty ChannelFuture */
    public ChannelFuture send(byte[] data) {
        return con.send(data);
    }

    /** blocking call that writes a Close WebSocket frame and wait for
     * the channel to shutdown gracefully */
    public void shutdown() {
        con.shutdown();
    }

}
