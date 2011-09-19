package com.freegine.repo.service;



import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.freegine.repo.service.basic.RepoWebService;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("Item")
public class Item extends RepoWebService 
{

	@DELETE
	@Path("*")
	public Response blockDelete()
	{
		return Response.status(405).build();
	}
	
	@DELETE
	@Path("{UUID}")
	public Response blockDelete2()
	{
		return Response.status(405).build();
	}
	

}
