package br.edu.ifsuldeminas.sd.sockets.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class UDPEchoServer {
    private static int MIN_BUFFER_SIZE = 100;
    private static DatagramSocket datagramSocket = null;
    private static byte[] incomingBuffer = null;
    private static int portNumber;
    private static int bufferSize;
    private static boolean isRunning = false;
    private static Set<ClientInfo> clients = new HashSet<>();

    public static void start(int portNumber, int bufferSize) throws UDPEchoServerException {
        validateAttributes(portNumber, bufferSize);
        assignAttributes(portNumber, bufferSize);
        try {
            prepare();
            run();
        } catch (IOException ioException) {
            isRunning = false;
            throw new UDPEchoServerException("Houve algum erro ao executar o servidor de eco UDP.", ioException);
        } finally {
            closeResources();
            System.out.println("Servidor parou devido a erros.");
        }
    }

    public static void stop() {
        if (isRunning) {
            closeResources();
            isRunning = false;
            System.out.println("Servidor parado.");
        } else {
            System.out.println("Servidor já está parado.");
        }
    }

    private static void validateAttributes(int portNumber, int bufferSize) {
        if (portNumber <= 1024)
            throw new IllegalArgumentException("O servidor UDP não pode usar portas reservadas.");

        if (bufferSize < MIN_BUFFER_SIZE)
            throw new IllegalArgumentException(
                    String.format("O buffer de mensagem precisa ser maior que %d", MIN_BUFFER_SIZE));
    }

    private static void assignAttributes(int portNumber, int bufferSize) {
        UDPEchoServer.portNumber = portNumber;
        UDPEchoServer.bufferSize = bufferSize;
    }

    private static void prepare() throws SocketException {
        if (isRunning)
            stop();
        datagramSocket = new DatagramSocket(portNumber);
        incomingBuffer = new byte[bufferSize];
    }

    private static void run() throws IOException {
        System.out.printf("Servidor de eco rodando em '%s:%d' ...\n", InetAddress.getLocalHost().getHostAddress(), portNumber);
        isRunning = true;
        DatagramPacket received = null;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            received = receive();
            clients.add(new ClientInfo(received.getAddress(), received.getPort()));
            System.out.print("Escreva uma resposta: ");
            String responseMessage = scanner.nextLine();
            sendResponse(received, responseMessage);
        }
    }

    private static DatagramPacket receive() throws IOException {
        DatagramPacket received = new DatagramPacket(incomingBuffer, incomingBuffer.length);
        datagramSocket.receive(received);
        System.out.printf("Mensagem: '%s' de '%s'\n", new String(received.getData(), 0, received.getLength()), received.getAddress().getHostAddress());
        return received;
    }

    private static void sendResponse(DatagramPacket received, String responseMessage) throws IOException {
        byte[] responseBuffer = responseMessage.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(
                responseBuffer,
                responseBuffer.length,
                received.getAddress(),
                received.getPort()
        );
        datagramSocket.send(responsePacket);
    }

    private static void closeResources() {
        if (datagramSocket != null)
            datagramSocket.close();
        datagramSocket = null;
    }

    private static class ClientInfo {
        private InetAddress address;
        private int port;

        public ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClientInfo that = (ClientInfo) o;
            return port == that.port && address.equals(that.address);
        }

        @Override
        public int hashCode() {
            return address.hashCode() + port;
        }
    }
}
