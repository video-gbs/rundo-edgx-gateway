package com.runjian.gb28181.bean;

import javax.sip.Dialog;
import java.util.EventObject;

public class CmdSendFailEvent extends EventObject {

    private String callId;

    /**
     * Constructs a prototypical Event.
     *
     * @param dialog
     * @throws IllegalArgumentException if source is null.
     */
    public CmdSendFailEvent(Dialog dialog) {
        super(dialog);
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }
}
