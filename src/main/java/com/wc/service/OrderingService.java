package com.wc.service;

import com.wc.commons.ContextProvider;
import com.wc.commons.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: sbenner
 * Date: 7/6/17
 * Time: 11:20 AM
 */
public class OrderingService {

    private static final Logger logger = Logger.getLogger(OrderingService.class.getName());
    private Queue<Message> outQueue = ContextProvider.getOutQueue();

    private static ExecutorService executor =
            new ThreadPoolExecutor(ContextProvider.numThreads, 150,
                    1800, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());

    private static OrderingService _orderService;
    private MessageHandler handler;

    public static OrderingService getInstance() {
        if (_orderService == null) _orderService = new OrderingService();
        return _orderService;
    }


    public OrderingService() {
    }

    public void initOrderService(MessageHandler handler) {
        //we should poll for queues and dequeue basing on the number of transferRate per queue
        this.handler = handler;
        Thread run = new Thread(new QueueHandler());

        run.setUncaughtExceptionHandler(
                (t, e) -> e.printStackTrace()
        );
        run.start();
    }


    public Queue getQueue() {
        return outQueue;
    }


    class QueueHandler implements Runnable {

        QueueHandler() {
            logger.info("QueueHandler  added...");
        }

        @Override
        public void run() {
            List<Message> list = new ArrayList<>();

            try {

                while (!Thread.currentThread().isInterrupted()) {

                    for (Map.Entry<String, IncomingMessageChannel> e : ContextProvider.getChannelMap().entrySet()) {

                        final String qname = e.getKey();
                        final Queue<Message> q = e.getValue().getQ();
                        final int LIST_MAX_SIZE = ContextProvider.getTransferRates().get(qname);

                        if (!q.isEmpty()) {
                            try {
                                if (outQueue.size() < (ContextProvider.OUT_Q_MAX_SIZE - LIST_MAX_SIZE)) {

                                    for (int i = 0; i < LIST_MAX_SIZE; i++) {
                                        Message m = q.poll();
                                        if (m != null) {
                                            list.add(m.prepare());
                                        }
                                    }

//                                    while (list.size() != LIST_MAX_SIZE) {
//                                        Message m = q.poll();
//                                        if (m != null) {
//                                            list.add(m.prepare());
//                                        } else {
//                                            //this makes all the difference
//                                            break;
//                                        }
//                                    }

                                    if (list.size() <= LIST_MAX_SIZE) {
                                        outQueue.addAll(list);
                                        handler.messagesReceived(list);
                                        list.clear();
                                    }

                                } else {
                                    Thread.sleep(1000);
                                    //logger.info("waiting for out queue to be drained...");
                                }

                            } catch (Exception ex) {
                                logger.log(Level.SEVERE, ex.toString(), ex);
                            }
                        }

                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.toString(), ex);
            }


        }

    }

}
