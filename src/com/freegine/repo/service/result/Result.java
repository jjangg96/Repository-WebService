package com.freegine.repo.service.result;


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Result")
public class Result {
	private boolean result;
	private String message;
	
	public Result()
	{
		this.result = false;
	}
	
	public Result(String message)
	{
		this.result = false;
		this.message = message;
	}
	
	public Result(boolean result, String message)
	{
		this.result = result;
		this.message = message;
	}
	
	public void setResult(boolean result)
	{
		this.result = result;
	}
	
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public boolean getResult()
	{
		return this.result;
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
}