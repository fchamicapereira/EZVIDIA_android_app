package ezvidia.app.domain;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {

    private DatagramSocket socket;
    private InetAddress address;

    private final int PORT = 48541;
    private final int TIMEOUT = 10 * 1000; // 10 seconds
    private final String CMD_LIST = "LIST";
    private final String CONFIG_SEPARATOR = ";;";

    public Client(String server_address) throws UnknownHostException, SocketException {
        String[] splitted = server_address.split("\\.");

        if (splitted.length != 4) {
            throw new UnknownHostException();
        }

        byte b0 = (byte) Integer.parseInt(splitted[0]);
        byte b1 = (byte) Integer.parseInt(splitted[1]);
        byte b2 = (byte) Integer.parseInt(splitted[2]);
        byte b3 = (byte) Integer.parseInt(splitted[3]);

        byte[] ipAddr = new byte[]{ b0, b1, b2, b3 };

        address = InetAddress.getByAddress(ipAddr);
        socket = new DatagramSocket(PORT);

        Log.d("CLIENT", address.getHostAddress());
    }

    public ArrayList<Config> list() throws IOException {

        ArrayList<Config> configs = new ArrayList<>();

        if (socket == null) {
            throw new SocketException("Socket not initialized");
        }

        DatagramPacket dp = new DatagramPacket(CMD_LIST.getBytes(), CMD_LIST.getBytes().length, address, PORT);

        if (dp == null) {
            throw new IOException("Null buffer");
        }

        socket.send(dp);

        byte[] message = new byte[512];
        DatagramPacket packet = new DatagramPacket(message,message.length);

        try {
            socket.setSoTimeout(TIMEOUT);
            socket.receive(packet);

            String text = new String(message, 0, packet.getLength());

            String[] names = text.split(CONFIG_SEPARATOR);
            for (String name : names) {
                configs.add(new Config(name));
            }

        } catch (SocketTimeoutException e) {
            Log.e("Timeout Exception","UDP Connection:",e);
            socket.close();
        }

        return configs;
    }

}
