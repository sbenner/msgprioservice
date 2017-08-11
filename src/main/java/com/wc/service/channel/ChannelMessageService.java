package com.wc.service.channel;

import com.wc.commons.ContextProvider;
import com.wc.commons.model.Message;
import com.wc.service.IncomingMessageChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelMessageService implements Runnable {

    private static final Logger logger = Logger.getLogger(ChannelMessageService.class.getName());

    private static AtomicLong messageNumber = new AtomicLong();


    private final Semaphore semaphore = new Semaphore(ContextProvider.numThreads);
    private int sizeOut = 0;
    private byte[] rest = null;

    private Map<IncomingMessageChannel, Selector> selectorMap
            = new ConcurrentHashMap<>();

    private IncomingMessageChannel channel;
    private Executor executor = Executors.newFixedThreadPool(ContextProvider.numThreads);


    ChannelMessageService(IncomingMessageChannel incomingMessageChannel) throws Exception {
        this.channel = incomingMessageChannel;
    }

    void registerChannel(IncomingMessageChannel incomingMessageChannel) throws Exception {
        sizeOut = 0;
        rest = null;
        incomingMessageChannel.source().configureBlocking(false);
        Selector selector = Selector.open();

        incomingMessageChannel.source().register(selector, SelectionKey.OP_READ);
        incomingMessageChannel.setRegistered(true);
        selectorMap.put(incomingMessageChannel, selector);
        initSelector(channel);
    }

    public void shutdownMessageService() {
        Thread.currentThread().interrupt();
    }


    private void initSelector(final IncomingMessageChannel incomingMessageChannel) {
        try {
            while (incomingMessageChannel.source().isOpen()) {
                final Selector selector = selectorMap.get(incomingMessageChannel);
                selector.select();
                final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    final SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isReadable()) this.handleRead(key);
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        Pipe.SourceChannel sc = ((Pipe.SourceChannel) key.channel());
        readAndProcessMessage(sc);
    }

    private List<byte[]> parseMessages(byte[] buffer) {
        List<byte[]> msg = new ArrayList<>();
        //logger.info("Parsing messages for channel : " + channel.getChannelId());
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.rewind();
        if (sizeOut > 0) {

            try {
                byte[] restOfMessage = new byte[sizeOut - rest.length];
                bb.get(restOfMessage);
                ByteBuffer chunkedMessage =
                        ByteBuffer.allocate(sizeOut).put(rest).put(restOfMessage);
                msg.add(chunkedMessage.array());
                sizeOut = 0;
                rest = null;
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.toString(), e);
            }
        }

        while (bb.hasRemaining()) {
            try {
                int size = bb.getInt();
                int restBufSize = bb.capacity() - bb.position();
                if (size > restBufSize) {
                    sizeOut = size;
                    rest = new byte[restBufSize];
                    bb.get(rest);
                    return msg;
                }
                byte[] b = new byte[size];
                bb.get(b);

                msg.add(b);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.toString(), e);
            }
        }

        return msg;
    }


    private void readAndProcessMessage(Pipe.SourceChannel ch) {
        int read;

        try {

            ByteBuffer body = ByteBuffer.allocate(1024);

            body.clear();

            // logger.info("readMessages() for channel : " + channel.getChannelId());

            while ((read = ch.read(body)) > 0) {
                //   logger.info("read bytes : " + read);

                body.flip();
                byte chunk[] = new byte[read];
                body.get(chunk);

                List<byte[]> messages = parseMessages(chunk);

                for (byte[] b : messages) {
                    semaphore.acquire();
                    executor.execute(
                            () -> {
                                try {

                                    Message m = new Message().
                                            setMessageId(messageNumber.incrementAndGet()).
                                            setSourceChannelId(channel.getChannelId()).
                                            setPayload(b);
                                    try {
                                        ContextProvider.getChannelMap().
                                                get(m.getSourceChannelId()).getQ().offer(m);

                                    } catch (Exception e) {
                                        logger.log(Level.SEVERE, e.getMessage(), e);
                                    }


                                } catch (Exception e) {
                                    logger.log(Level.SEVERE, e.toString(), e);
                                } finally {
                                    semaphore.release();
                                }
                            }
                    );
                }
            }


        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }

    }


    @Override
    public void run() {
        logger.info("Polling channel: " + channel.getChannelId());
        try {
            registerChannel(channel);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

    }
}
