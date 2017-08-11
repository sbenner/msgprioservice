package com.wc.commons.enums;

/**
 * Created with IntelliJ IDEA.
 * User: sbenner
 * Date: 7/7/17
 * Time: 6:31 PM
 */
public enum Command {
    //register channel
    //delete channel
    //channel expunge messages (num)
    //add priority for a channel
    //set channel eject rate

    RC("regchan"), DC("delchan"),
    DAC("delallchannels"),
    CSM("chansendmsgs"),
    SCR("setchanrate"),
    PQS("printqstatuses"),
    PCR("printchannelsrate"),
    PU("printusage"),
    POQ("purgeoutqueue"),
    INIT("initializechannels"),
    QUIT("quit");

    private String name;

    public String getName() {
        return name.toUpperCase();
    }

    private Command(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Command fromString(String name) {
        Command command = null;

        if (name != null) {
            for (Command cmd : Command.values()) {
                if (name.equalsIgnoreCase(cmd.name)) {
                    return cmd;
                }
            }
        } else {
            throw new IllegalArgumentException(name + " has no corresponding value");
        }
        return command;
    }

}