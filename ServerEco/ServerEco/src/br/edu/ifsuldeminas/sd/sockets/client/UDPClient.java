package br.edu.ifsuldeminas.sd.sockets.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class UDPClient {
    private static int SERVER_PORT = 3000;
    private static int BUFFER_SIZE = 200;
    private static String KEY_TO_EXIT = "q";

    public static void main(String[] args) {
        DatagramSocket datagramSocket = null;
        Scanner reader = new Scanner(System.in);
        String stringMessage = "";

        try {
            datagramSocket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName("192.168.3.8");

            while (!stringMessage.equals(KEY_TO_EXIT)) {
                System.out.printf("Escreva uma mensagem (%s para sair): ", KEY_TO_EXIT);
                stringMessage = reader.nextLine();

                if (!stringMessage.equals(KEY_TO_EXIT)) {
                    try {
                        byte[] message = stringMessage.getBytes();
                        DatagramPacket datagramPacketToSend = new DatagramPacket(message, message.length, serverAddress, SERVER_PORT);
                        datagramSocket.send(datagramPacketToSend);

                        byte[] responseBuffer = new byte[BUFFER_SIZE];
                        DatagramPacket datagramPacketForResponse = new DatagramPacket(responseBuffer, responseBuffer.length);
                        datagramSocket.receive(datagramPacketForResponse);
                        System.out.printf("Resposta do servidor: %s\n", new String(datagramPacketForResponse.getData(), 0, datagramPacketForResponse.getLength()));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        } catch (SocketException socketException) {
            socketException.printStackTrace();
        } catch (UnknownHostException unknownHostException) {
            unknownHostException.printStackTrace();
        } finally {
            closeOpenedResources(datagramSocket, reader);
            System.out.printf("Cliente saindo com %s ...\n", KEY_TO_EXIT);
        }
    }

    private static void closeOpenedResources(DatagramSocket datagramSocket, Scanner reader) {
        if (datagramSocket != null)
            datagramSocket.close();
        if (reader != null)
            reader.close();
    }
}
