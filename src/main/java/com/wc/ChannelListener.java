package com.wc;

import com.wc.commons.ContextProvider;
import com.wc.service.IncomingMessageChannel;
import com.wc.service.MessageHandler;
import com.wc.service.OrderingService;
import com.wc.service.channel.ChannelMessageHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ChannelListener {

    private static final Logger logger = Logger.getLogger(ChannelListener.class.getName());

    private static ChannelListener _listener;


    private ContextProvider contextProvider;

    private MessageHandler handler;

    static ChannelListener getInstance() {
        if (_listener == null) {
            _listener = new ChannelListener();
            return _listener;
        } else {
            return _listener;
        }
    }

    public static void main(String[] args) {

        ContextProvider contextProvider = new ContextProvider();
        ChannelListener.getInstance().setContext(contextProvider);
      

        try {

            ChannelMessageHandler channelMessageHandler = new ChannelMessageHandler();
            ChannelListener.getInstance().registerHandler(channelMessageHandler);

            new Thread(new CommandLineHandler(channelMessageHandler)).start();

            //wait for printing of the app usage
            Thread.sleep(500);

            ChannelListener.getInstance().initialRun();

            //waiting for some messages to be pushed to out queues
            Thread.sleep(2000);

            OrderingService.getInstance().initOrderService(channelMessageHandler);


        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.toString(), ex);
        }


    }

    public void initialRun() throws Exception{


        IncomingMessageChannel ic1 = new IncomingMessageChannel("cn-01");
        IncomingMessageChannel ic2 = new IncomingMessageChannel("cn-02");
        IncomingMessageChannel ic3 = new IncomingMessageChannel("cn-03");
        IncomingMessageChannel ic4 = new IncomingMessageChannel("cn-04");


        //registering channels



        ChannelListener.getInstance().registerChannel(ic1);
        ChannelListener.getInstance().registerChannel(ic2);
        ChannelListener.getInstance().registerChannel(ic3);
        ChannelListener.getInstance().registerChannel(ic4);

        ///executing the message sending threads


        ContextProvider.getExecutor().submit(ic1);
        ContextProvider.getExecutor().submit(ic2);
        ContextProvider.getExecutor().submit(ic3);
        ContextProvider.getExecutor().submit(ic4);




    }


    public void registerHandler(MessageHandler handler) {
        this.setHandler(handler);
    }

    public void setContext(ContextProvider context) {
        this.contextProvider = context;
    }

    public void registerChannel(IncomingMessageChannel channel) {
        contextProvider.setChannelToChannelMap(channel);
        getHandler().channelCreated(channel.getChannelId());

    }

    public void removeChannel(IncomingMessageChannel channel) {
        getHandler().channelDestroyed(channel.getChannelId());
    }

    public MessageHandler getHandler() {
        return handler;
    }

    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }


}
