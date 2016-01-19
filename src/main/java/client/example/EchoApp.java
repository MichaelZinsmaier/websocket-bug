package client.example;

import client.framework.IReceiver;
import client.framework.WebSocketConnectionFactory;
import client.framework.WsConnection;

/**
 * Small example App that writes one message to the server and waits for one message
 * to come back.
 */
public class EchoApp {

    public static void main(String[] args) throws InterruptedException {
        new EchoApp();
    }

    private WsConnection client;
    private WebSocketConnectionFactory factory = WebSocketConnectionFactory.getInstance();

    public EchoApp() throws InterruptedException {
        // create a factory


        // create a WebSocket connections to the server
        client = factory.createConnection("localhost", 8080, "my-client", receiveCallBack);

        // connect the client
        client.connect();

        // send some message to the server (non-blocking)
        client.send("Hi there".getBytes());


        // send a close, upon receiving the echoed close the client shuts down the WebSocket connection
        client.send("close".getBytes());
    }


    /**
     * Receiver callback for the "sending" WebSocket Client
     */
    private IReceiver receiveCallBack = new IReceiver() {

        @Override
        public void receive(byte[] data, boolean fin) {
            if (fin) {
                System.out.println("received a fin");
            }
            if (data.length > 0) {
                String content = new String(data);
                System.out.println("received some data: " + content);

                if (content.equals("close")) {
                    new Thread() {
                        public void run() {
                            client.shutdown();
                        }
                    }.start();
                }
            }
        }

        @Override
        public void closing() {
            // client closes, should not happen
            System.out.println("received a close event - shutting down");
            factory.shutdown();
            System.exit(0);
        }
    };
}
