package com.freegine.repo.service.basic;

public class WebService {
	private StringBuilder logger = new StringBuilder(); 
	protected final String FILE = "file";

	protected String getLog()
	{
		String log = logger.toString();
		logger.setLength(0);

		return log;
	}

	protected void Log(String message)
	{
		logger.append(message + "\n");
	}
}
