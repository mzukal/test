package com.stabilit.sc.io;

import com.stabilit.sc.context.IRequestContext;
import com.stabilit.sc.job.IJob;

public interface IRequest {

	public String getKey();

	public IRequestContext getContext();
	
	public ISession getSession(boolean fCreate);
	
	public IJob getJob();
	
}
