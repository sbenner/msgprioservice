package com.wc.service.channel;

import com.wc.commons.ContextProvider;
import com.wc.commons.model.Message;
import com.wc.service.IncomingMessageChannel;
import com.wc.service.MessageHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: sbenner
 * Date: 7/5/17
 * Time: 11:19 PM
 */
public class ChannelMessageHandler implements MessageHandler {

    private static final Logger logger = Logger.getLogger(ChannelMessageHandler.class.getName());

    private Map<ChannelMessageService, IncomingMessageChannel> servicesChannelMap = new ConcurrentHashMap<>();

   
    public ChannelMessageHandler() {
    }


    public void messageReceived(Message m) {
        //executor.execute(new MessageConsumer(m));
        //messageReceived to be implemented by children
    }

    @Override
    public void messagesReceived(List<Message> messageList) {
        for (Message m : messageList) {
            messageReceived(m);
        }
    }

    @Override
    public void channelCreated(String channelId) {
        try {
            IncomingMessageChannel incomingMessageChannel =
                    ContextProvider.getChannelMap().get(channelId);
            ChannelMessageService service = new ChannelMessageService(incomingMessageChannel);
            servicesChannelMap.put(service, incomingMessageChannel);
            ContextProvider.getExecutor().execute(service);
        } catch (Exception e) {
            logger.severe(e.getMessage());

        }

    }

    @Override
    public void channelDestroyed(String channelId) {
        

        new Thread(
                () -> {
                    ChannelMessageService service = null;
                    ContextProvider.getChannelMap().remove(channelId);
                    for (Map.Entry<ChannelMessageService, IncomingMessageChannel> e : servicesChannelMap.entrySet()) {
                        IncomingMessageChannel imc = e.getValue();
                        if (imc.getChannelId().equals(channelId)) {
                            service = e.getKey();
                            break;
                        }
                    }
                    if (service != null) {
                        servicesChannelMap.remove(service);
                    }

                }).start();


    }


}
