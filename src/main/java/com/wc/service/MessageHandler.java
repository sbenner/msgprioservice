package com.wc.service;

import com.wc.commons.model.Message;

import java.util.List;

/**
 * Created by sbenner on 05/07/2017.
 */
public interface MessageHandler {

    void messageReceived(Message m);

    void messagesReceived(List<Message> messageList);

    void channelCreated(String channelId);

    void channelDestroyed(String channelId);

}
