package com.wc.service;

import com.wc.commons.model.Message;

import java.util.Queue;

/**
 * Created by sbenner on 05/07/2017.
 */
public interface OrderedMessageQueueProvider {

    Queue<Message> getQueue();

}
