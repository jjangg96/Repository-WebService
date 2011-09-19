package com.freegine.example;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("CRUDService")
public class CRUDService {
	
	@POST
	@Consumes("multipart/mixed")
	@Produces(MediaType.TEXT_XML)
	@Path("Create")
	public void create()
	{
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public String read()
	{
		return "null";
	}

	@PUT
	@Consumes("multipart/mixed")
	@Produces(MediaType.TEXT_XML)
	@Path("Update")
	public void update()
	{
	}
	
	@DELETE
	@Produces(MediaType.TEXT_XML)
	@Path("Delete")
	public void delete()
	{
	}
	
}
