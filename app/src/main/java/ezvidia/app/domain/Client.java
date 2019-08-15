package ezvidia.app.domain;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    private InetAddress address;

    private final int PORT = 48541;
    private final int TIMEOUT = 1000; // 1 second

    private final String CMD_LIST = "LIST";
    private final String CMD_APPLY = "APPLY";
    private final String CONFIG_SEPARATOR = ";;";

    private Pattern ipPattern;

    public Client() {
        ipPattern = Pattern.compile(
                "(^192\\.168\\.([0-9]|[0-9][0-9]|[0-2][0-5][0-5])\\." +
                        "([0-9]|[0-9][0-9]|[0-2][0-5][0-5])$)|(^172\\." +
                        "([1][6-9]|[2][0-9]|[3][0-1])\\." +
                        "([0-9]|[0-9][0-9]|[0-2][0-5][0-5])\\." +
                        "([0-9]|[0-9][0-9]|[0-2][0-5][0-5])$)|(^10\\." +
                        "([0-9]|[0-9][0-9]|[0-2][0-5][0-5])\\." +
                        "([0-9]|[0-9][0-9]|[0-2][0-5][0-5])\\." +
                        "([0-9]|[0-9][0-9]|[0-2][0-5][0-5])$)");

    }

    public boolean validateIpAddress(String address) {
        Matcher m = ipPattern.matcher(address);
        return m.matches();
    }

    public void setAddress(String server_address) throws UnknownHostException {
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
    }

    public String getAddress() {
        return address != null ? address.getHostAddress() : "";
    }

    public ArrayList<Config> list() throws IOException {

        ArrayList<Config> configs = new ArrayList<>();

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dp = new DatagramPacket(CMD_LIST.getBytes(), CMD_LIST.getBytes().length, address, PORT);

        socket.send(dp);

        byte[] message = new byte[512];
        DatagramPacket packet = new DatagramPacket(message,message.length);

        socket.setSoTimeout(TIMEOUT);
        socket.receive(packet);

        String text = new String(message, 0, packet.getLength());

        String[] names = text.split(CONFIG_SEPARATOR);
        for (String name : names) {
            configs.add(new Config(name));
        }

        socket.close();

        return configs;
    }

    public boolean apply(String config) throws IOException {

        String command = String.format("%s %s", CMD_APPLY, config);

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dp = new DatagramPacket(command.getBytes(), command.getBytes().length, address, PORT);

        socket.send(dp);

        byte[] message = new byte[512];
        DatagramPacket packet = new DatagramPacket(message,message.length);

        try {
            socket.setSoTimeout(TIMEOUT);
            socket.receive(packet);

            String response = new String(message, 0, packet.getLength());

            if (response.equals("OK")) {
                return true;
            }

        } catch (SocketTimeoutException e) {
            Log.e("Timeout Exception","UDP Connection:",e);
        }

        socket.close();

        return false;
    }

}
