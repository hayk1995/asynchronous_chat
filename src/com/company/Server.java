package com.company;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.*;
import java.util.concurrent.Future;


public class Server {
    private static final int DEFAULT_NUMBER_OF_THREADS = 4;
    private int numberOfThreads;
    private PendingActionsPool actionsPool;
    private AsynchronousServerSocketChannel server;
    private List<AsynchronousSocketChannel> clients = new ArrayList<>();
    private Map<AsynchronousSocketChannel, String> clientNames = new HashMap<>();

    Server() throws Exception{
        this(DEFAULT_NUMBER_OF_THREADS);
    }

    Server(int numberOfThreads) throws Exception{
        this.numberOfThreads = numberOfThreads;
        InetSocketAddress address = new InetSocketAddress(5000);
        server = AsynchronousServerSocketChannel.open().bind(address);
        actionsPool = new PendingActionsPool(this.numberOfThreads);
    }

    public void start() {
        actionsPool.start();
        receiveConnection();
    }

    private void receiveConnection() {
        Future client = server.accept();
        actionsPool.add(new PendingAction(client, new ConnectionReceivedHandler()));
    }

    private class ConnectionReceivedHandler implements Handler {
        @Override
        public void run(Object clientSocket) {
            try {
                AsynchronousSocketChannel channel = (AsynchronousSocketChannel) clientSocket;
                clients.add(channel);
                new SendMessageHandler(channel, "Please Enter Your Name");
                new ReceiveMessageHandler(channel);
                receiveConnection();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private class ReceiveMessageHandler implements Handler {
        private StringBuilder currentMessage = new StringBuilder();
        private ByteBuffer buffer = ByteBuffer.allocate(64);
        private AsynchronousSocketChannel channel;

        ReceiveMessageHandler(AsynchronousSocketChannel clienSocketChannel) {
            channel = clienSocketChannel;
            Future readResult = clienSocketChannel.read(this.buffer);
            actionsPool.add(new PendingAction(readResult, this));
        }

        @Override
        public void run(Object nioResult) {
            if ((Integer) nioResult == -1) {
                clients.remove(channel);
                return;
            }

            buffer.flip();
            while (buffer.hasRemaining()) {
                char read = (char) buffer.get();
                if (read == '\n') {
                    handleClientMessage(channel, currentMessage.toString());
                    currentMessage = new StringBuilder();
                } else {
                    currentMessage.append(read);
                }
            }

            buffer.clear();
            Future readResult = channel.read(this.buffer);
            actionsPool.add(new PendingAction(readResult, this));
        }
    }

    private void handleClientMessage(AsynchronousSocketChannel channel, String message) {
        if (clientNames.containsKey(channel)) {
            broadCast(channel, clientNames.get(channel) + ':' + message);
        } else {
            clientNames.put(channel, message);
        }
    }

    private void broadCast(AsynchronousSocketChannel sender, String message) {
        for (AsynchronousSocketChannel channel : clients) {
            if (channel != sender && clientNames.containsKey(channel)) {
                new SendMessageHandler(channel, message);
            }

        }
    }

    class SendMessageHandler implements Handler {
        ByteBuffer buffer;
        AsynchronousSocketChannel channel;

        SendMessageHandler(AsynchronousSocketChannel channel, String message) {
            this.channel = channel;
            buffer = ByteBuffer.allocate(message.length() + 1);
            buffer.put((message + "\n").getBytes());
            buffer.flip();
            run(new Object());
        }

        @Override
        public void run(Object nioResult) {
            if (buffer.hasRemaining()) {
                Future result = channel.write(buffer);
                actionsPool.add(new PendingAction(result, this));
            }
        }
    }
}
