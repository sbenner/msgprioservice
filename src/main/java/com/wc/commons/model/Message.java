package com.wc.commons.model;

import java.io.Serializable;

/**
 * Created by sergey on 12/3/16.
 */
public class Message implements Serializable {

    private long messageId;
    private String sourceChannelId;
    private byte[] payload;

    public Message() {
    }


    public Message prepare() throws Exception {
        Thread.sleep(500);
        return this;
    }

    public long getMessageId() {
        return messageId;
    }

    public Message setMessageId(long messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getSourceChannelId() {
        return sourceChannelId;
    }

    public Message setSourceChannelId(String sourceChannelId) {
        this.sourceChannelId = sourceChannelId;
        return this;
    }

    public byte[] getPayload() {
        return payload;
    }

    public Message setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }
}
