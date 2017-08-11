package com.wc.service;

import com.wc.commons.model.Message;

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: sbenner
 * Date: 7/5/17
 * Time: 7:33 PM
 */
public class IncomingMessageChannel implements Runnable {
    private static final Logger logger = Logger.getLogger(IncomingMessageChannel.class.getName());

    final private String channelId;
    private boolean registered;
    private int transferRate = 1;
    private int msgNum = 200;
    final private Pipe p;
    final private Queue<Message> q;

    public IncomingMessageChannel(String channelId) throws Exception {
        this.p = Pipe.open();
        this.q = new LinkedBlockingQueue<>();
        this.channelId = channelId;

    }

    public Pipe.SourceChannel source() {
        return p.source();
    }


    public Pipe.SinkChannel sink() {
        return p.sink();
    }

    public void generateMessages() throws Exception {
        sendMessages(getMsgNum());
    }

    private void
    sendMessages(int num) throws Exception {

        for (int i = 0; i < num; i++) {

            String msg = UUID.randomUUID().toString();
            //    logger.info(getChannelId() + ": " + msg);
            int size = msg.length();
            ByteBuffer buf = ByteBuffer.allocate(size + 4);
            buf.clear();
            buf.putInt(size).put(msg.getBytes());
            buf.flip();
            while (buf.hasRemaining()) {
                sink().write(buf);
                // Thread.sleep(100);
            }
        }
        logger.info(getChannelId() + ": sent " + num + " messages");
    }

    public String getChannelId() {
        return channelId;
    }


    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    @Override
    public void run() {
        try {
            generateMessages();
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        IncomingMessageChannel other = (IncomingMessageChannel) obj;
        return channelId != null && channelId.equals(other.getChannelId());

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
        return result;
    }


    public Queue<Message> getQ() {
        return q;
    }

    public int getTransferRate() {
        return transferRate;
    }

    public void setTransferRate(int transferRate) {
        this.transferRate = transferRate;
    }

    public int getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }


    @Override
    public String toString() {
        return String.valueOf(getQ().size());
    }
}
