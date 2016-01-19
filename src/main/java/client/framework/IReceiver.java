/* Copyrights owned by Atos and Siemens, 2015. */
package client.framework;

public interface IReceiver {

    /**
     * called when new data is available.
     * DO NOT block this method! Switch the thread if necessary
     * @param data received data
     * @param fin is the WebSocket object finished or will there be more
     */
    public void receive(byte[] data, boolean fin);

    /**
     * called after receiving a WebSocket Close frame
     * DO NOT block this method! Switch the thread if necessary
     */
    public void closing();
}
