package com.demo.account.service;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadRejectedExecutionHandler implements RejectedExecutionHandler {

	final static Logger logger = LoggerFactory.getLogger(ThreadRejectedExecutionHandler.class);
	
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		logger.error("Rejected Tasks : "+r.toString());
	}
}
