/* Copyrights owned by Atos and Siemens, 2015. */
package client.framework;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;

public class ConnectionHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketClientHandshaker handshaker;
    private IReceiver receiverHandler;
    private IPingPongHandler pingHanlder;

    private ChannelPromise handshake;

    protected ConnectionHandler(WebSocketClientHandshaker handshaker, IReceiver receiver, IPingPongHandler pinger) {
        this.handshaker = handshaker;
        this.receiverHandler = receiver;
        this.pingHanlder = pinger;
    }

    protected ChannelPromise handshake() {
        if (handshake != null) {
            return handshake;
        } else {
            throw new IllegalStateException("Handshake future doesn't exist.");
        }
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshake = ctx.newProgressivePromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.print("Handler: Disconnected");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();

        // handshake handling
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(channel, (FullHttpResponse) msg);
            System.out.println("Handler: Connected");

            if (handshake != null) {
                handshake.setSuccess();
            } else {
                throw new IllegalStateException("Handshake future doesn't exist.");
            }
        }

        // message handling
        if (msg instanceof FullHttpResponse) {
            // ignore for now
            FullHttpResponse response = (FullHttpResponse) msg;
            System.out.println("Handler: Received response: " + response);
        } else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) msg;
            System.out.println("Handler: Received binary frame of size " + binaryFrame.content().readableBytes());

            receiverHandler.receive(extractData(binaryFrame), binaryFrame.isFinalFragment());
        } else if (msg instanceof ContinuationWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) msg;
            System.out.println("Handler: Received binary continuation frame of size " + binaryFrame.content().readableBytes());

            receiverHandler.receive(extractData(binaryFrame), binaryFrame.isFinalFragment());
        } else if (msg instanceof PongWebSocketFrame) {
            System.out.println("Handler: Received pong");

            pingHanlder.receivedPong();
        } else if (msg instanceof PingWebSocketFrame) {
            System.out.println("Handler: Received closing frame");

            pingHanlder.receivedPing();
        } else if (msg instanceof CloseWebSocketFrame) {
            System.out.println("Handler: Received closing frame");

            receiverHandler.closing();
            channel.close();
        }
    }

    private byte[] extractData(WebSocketFrame binaryFrame) {
        // SimpleChannelInputHandler will free the buffer on its own convert it before that happens
        // converting is less efficient than exposing the buffers but also saver
        ByteBuf byteBuf = binaryFrame.content();
        byte[] dataCopy = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(dataCopy);

        return dataCopy;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (handshake != null && !handshake.isDone()) {
            handshake.setFailure(cause);
        }
        ctx.close();
    }
}
