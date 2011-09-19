package com.freegine.repo.service;



import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.freegine.repo.service.basic.RepoWebService;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("Bundle")
public class Bundle extends RepoWebService
{
	
	
	@PUT
	@Path("*")
	public Response blockUpdate()
	{
		return Response.status(405).build();
	}
	
	@PUT
	@Path("{UUID}")
	public Response blockUpdate2()
	{
		return Response.status(405).build();
	}
	
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
