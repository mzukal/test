package com.stabilit.sc.io;

import java.io.Serializable;
import java.util.Map;

public interface IMessage extends Serializable {
	public SCMPMsgType getKey();

	public IMessage newInstance();

	public Object getAttribute(String name);

	public void setAttribute(String name, Object value);

	Map<String, Object> getAttributeMap();
}
