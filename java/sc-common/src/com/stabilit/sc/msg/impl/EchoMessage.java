package com.stabilit.sc.msg.impl;

import com.stabilit.sc.msg.Message;
import com.stabilit.sc.msg.MsgType;

public class EchoMessage extends Message {
	
	private static final long serialVersionUID = -5461603317301105352L;
	
	public static MsgType ID = MsgType.ECHO;

	public EchoMessage() {
    	super(ID);
    }
}
