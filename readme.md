ChannelListener / MessageHandler Utility
----------------------------------------
[TOC]

**Build**
------------
```
$mvn clean compile assembly:single
```
**Run**
-------
```
$java -jar ./target/msgqservice-1.0-SNAPSHOT.jar
```
**Description**
---------------
Application starts with 4 pre-created `IncomingMessageChannel`  instances.

`IncomingMessageChannel` class has default transfer rate of 1. 

The transfer rates are taken from a static map which is pre-defined in the `ContextProvider` class. 
```
static {
        setTransferRates(new ConcurrentHashMap<>());
        getTransferRates().put("cn-01", 1);
        getTransferRates().put("cn-02", 1);
        getTransferRates().put("cn-03", 3);
        getTransferRates().put("cn-04", 5);
        
    }
```
New transfer rates can be created for new channels via command line interface.
`IncomingMessageChannel` is `Runnable` and when 
their appropriate thread is executed - they start to generate 200 messages 
per channel. They're sent via `Pipe.SinkChannel sink()`

These `byte[]` messages are being read and processed by the 
```
ChannelMessageService.readAndProcessMessage(Pipe.SourceChannel ch) 
```
Messages are being prepared - their respective `messageId, channelId` and `payload` are being set. According to the documentation "one queue per source channel is suggested" - each of 4 channels has their own Queue. 

The `OrderingService` runs a separate thread to poll and order the Messages 
from the `IncomingMessageChannel` Queues. It orders them basing on the queue rates taken from the mentioned static map. The delay is executed on `Message.prepare()` for *500ms* before sending to the `OUT Queue` as it's required by the documentation.

Print `pqs` in the command line in order to see the statuses of the source and out queues after all of the handles are being set.
If you do it periodically after the messages are sent you can see the dynamic of change of queues' sizes.

CMD Line interface:
-------------------------
All of the cmds can be typed in lower case 
e.g. : 
`dc cn-02`
`scr cn-05 7` - sets channel cn-05 to 7 messages per call rate 
`rc cn-05` - register channel cn-05 for handling
`pcr`
output
```
Channel rates : {cn-03=3, cn-02=1, cn-04=5, cn-01=1}
```
> **Commands** 

 - `<RC> <cn-(your_chan_number)>` - register channel 
 - `<DC> <cn-(your_chan_number)>` -   deregister channel 
 - `<CSM> <cn-(your_chan_number)> <optional num>` - send 200 messages for channel per default - if optional messages `num` is not supplied
 - `<SCR> cn-(your_chan_number)> <msg_pull_rate_num>` - set channel rate
 - `<PQS>` - print queue statuses
 - `<POQ>` - purge out queue
 - `<DAC>` - delete all channels
 - `<PU>` - print usage
 - `<PCR>` - print all channels' transfer rates
 - `<INIT>` - re-run init procedure (creates 4 IncomingMessageChannels and sends 800 msgs to 4 queues). Run `dac` and `poq` prior to `init` to cleanup the channels.
 - `<QUIT>` - quit app

