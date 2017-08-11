package com.wc;

import com.wc.commons.ContextProvider;
import com.wc.commons.enums.Command;
import com.wc.commons.model.Message;
import com.wc.service.IncomingMessageChannel;
import com.wc.service.MessageHandler;

import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: sbenner
 * Date: 7/9/17
 * Time: 6:08 PM
 */
public class CommandLineHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(CommandLineHandler.class.getName());

    private MessageHandler handler;

    public CommandLineHandler(MessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        enableCmdLine();
    }


    private void enableCmdLine() {
        System.out.println("CMD Line interface is enabled:");
        printUsage();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            try {
                String vals[] = scanner.nextLine().split(" ");


                if (vals.length < 1) {
                    printUsage();
                } else {
                    if (vals[0].trim().length() < 2) {
                        continue;
                    }
                    try {
                        Command command = Command.valueOf(vals[0].trim().toUpperCase());
                        commandHandler(command, vals);
                    } catch (IllegalArgumentException iea) {
                        printUsage();
                    }

                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.toString(), e);
            }
        }
    }


    private void commandHandler(Command command, String[] vals) throws Exception {
        if (command != null) {
            switch (command) {
                case RC:  //register channel
                    if (vals[1] != null && vals[1].startsWith("cn-")) {
                        IncomingMessageChannel imc = new IncomingMessageChannel(vals[1]);
                        if (vals.length == 3 && vals[2] != null) {
                            try {
                                imc.setTransferRate(Integer.valueOf(vals[2]));
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                            }
                        } else {
                            Integer rate = ContextProvider.getTransferRates().get(vals[1]);
                            if (rate != null) imc.setTransferRate(rate);
                        }
                        ChannelListener.getInstance().registerChannel(imc);
                    }
                    break;
                case DC: //delete channel
                    if (vals[1] != null) {
                        handler.channelDestroyed(vals[1]);                         //tc.put(key, val);
                    }
                    break;
                case DAC: //delete all channels

                    deleteAllChannels();

                    break;
                case PU: //delete channel
                    printUsage();
                    break;
                case PCR: //delete channel
                    System.out.println("Channel rates : " + ContextProvider.getTransferRates());
                    break;
                case CSM: //send messages
                    if (vals[1] != null) {
                        IncomingMessageChannel icm = ContextProvider.getChannelMap().get(vals[1]);
                        if (icm != null) {
                            if (vals.length == 3 && vals[2] != null) {
                                int msgNum = Integer.parseInt(vals[2]);
                                icm.setMsgNum(msgNum);
                            }
                            icm.generateMessages();
                        } else {
                            logger.info("There're no registered channels with " + vals[1] + " name");
                        }
                    }
                    break;
                case SCR:  //set channel rate
                    if (vals.length == 3 && (vals[1] != null && vals[1].startsWith("cn-")) && vals[2] != null) {
                        ContextProvider.getTransferRates().putIfAbsent(vals[1], Integer.valueOf(vals[2]));
                    }
                    break;
                case PQS:  //print q statuses
                    printQueueStatuses();
                    break;
                case POQ:
                    ContextProvider.getOutQueue().clear();
                    break;
                case INIT:
                    ContextProvider.getOutQueue().clear();
                    deleteAllChannels();
                    ContextProvider.getExecutor().shutdownNow();
                    while (ContextProvider.getExecutor().isShutdown()) {
                        Thread.sleep(1000);
                    }
                    ChannelListener.getInstance().initialRun();
                    break;
                case QUIT:
                    System.exit(0);
                    break;
                default:
                    break;

            }
        }
    }


    private void deleteAllChannels() {

        for (Map.Entry<String, IncomingMessageChannel> e : ContextProvider.getChannelMap().entrySet()) {
            String chanName = e.getKey();
            handler.channelDestroyed(chanName);                         //tc.put(key, val);
        }
    }

    private void printQueueStatuses() {
        try {
            Map<String, Long> sum = ContextProvider.getOutQueue().
                    stream().collect(
                    Collectors.groupingBy(Message::getSourceChannelId, Collectors.counting()));

            System.out.println("Source QUEUESs contents: " + ContextProvider.getChannelMap().toString());
            System.out.println("OUT_QUEUE contents: " + sum);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }
    }

    private void printUsage() {
        System.out.println(
                "<rc> cn-(your_chan_number) - register channel\n" +
                        "<csm> cn-(your_chan_number) <optional num> - send messages from particular channel\n" +
                        "<dc> cn-(your_chan_number) - deregister channel\n" +
                        "<pqs> print current queue statuses\n" +
                        "<scr> cn-(your_chan_number) <msg_pull_rate_num> - set channel rate\n" +
                        "<poq> - purge out queue\n" +
                        "<pu> - print usage (this help text)\n" +
                        "<dac> - delete all channels\n" +
                        "<pcr> - print channels' transfer rate\n" +
                        "<init> - re-run init procedure (creates 4 IncomingMessageChannels and sends 800 msgs to 4 queues). Please run <DAC> and <POQ> commands prior to another <INIT> just in case.\n" +
                        "<q> - quit app\n"
        );
    }


}
