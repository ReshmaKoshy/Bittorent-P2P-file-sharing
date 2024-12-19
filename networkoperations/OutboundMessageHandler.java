package networkoperations;

import java.net.Socket;
import nodeoperations.PeerProcess;
import java.io.ObjectOutputStream;
import nodeoperations.OutMessage;
import java.io.IOException;

public class OutboundMessageHandler extends Thread {

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.err.println(e);
            }

            if(!PeerProcess.outgoingMessageQueue.isEmpty()) {
                synchronized (PeerProcess.outgoingMessageQueue) {
                    OutMessage pendingMessage = PeerProcess.outgoingMessageQueue.poll();
                    if (pendingMessage != null) {
                        Socket targetSocket = pendingMessage.getConnectionSocket();
                        byte[] messageContent = pendingMessage.getMessage();
                        transmitMessage(targetSocket, messageContent);
                    }
                }
            }
        }
    }

    public void transmitMessage(Socket connectionSocket, byte[] messageData) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
            synchronized (connectionSocket) {
                outputStream.writeObject(messageData);
            }
        } catch (IOException communicationError) {
            System.err.println(communicationError);
        }
    }
}