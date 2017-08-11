package com.wc.commons;

import com.wc.commons.model.Message;
import com.wc.service.IncomingMessageChannel;
import com.wc.service.OrderedMessageQueueProvider;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: sbenner
 * Date: 7/5/17
 * Time: 8:13 PM
 */
public class ContextProvider implements OrderedMessageQueueProvider {

    public static final int numThreads = 10;
    public static final int OUT_Q_MAX_SIZE = 500;

    private static ExecutorService executor =
            new ThreadPoolExecutor(ContextProvider.numThreads, 150,
                    1800, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());

    private static Queue<Message> outQueue = new LinkedBlockingQueue<>(OUT_Q_MAX_SIZE);
    private static Map<String, IncomingMessageChannel> channelMap = new ConcurrentHashMap<>();
    private static Map<String, Integer> transferRates;

    static {
        setTransferRates(new ConcurrentHashMap<>());
        getTransferRates().put("cn-01", 1);
        getTransferRates().put("cn-02", 1);
        getTransferRates().put("cn-03", 3);
        getTransferRates().put("cn-04", 5);
    }
        
    public static Map<String, Integer> getTransferRates() {
        return transferRates;
    }

    private static void setTransferRates(Map<String, Integer> transferRates) {
        ContextProvider.transferRates = transferRates;
    }

    public static Queue<Message> getOutQueue() {
        return outQueue;
    }

   public static Map<String, IncomingMessageChannel> getChannelMap() {
        return channelMap;
    }

    public static ExecutorService getExecutor() {


        if(executor.isShutdown()){
            executor = new ThreadPoolExecutor(numThreads, 150,
                    5, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());
        }


        return executor;
    }

    public static void setExecutor(ExecutorService executor) {
        ContextProvider.executor = executor;
    }

    public void setChannelToChannelMap(IncomingMessageChannel channel) {
        getChannelMap().putIfAbsent(channel.getChannelId(), channel);
    }

    @Override
    public Queue<Message> getQueue() {
        return getOutQueue();
    }


}
