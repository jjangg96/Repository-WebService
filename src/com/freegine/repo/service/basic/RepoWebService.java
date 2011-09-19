package com.freegine.repo.service.basic;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.freegine.repo.library.Config;
import com.freegine.repo.library.RepoMongo;
import com.freegine.repo.library.RepoScheme;
import com.freegine.repo.service.result.Result;
import com.mongodb.MongoException;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

public class RepoWebService extends WebService{

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@Path("{UUID}")
	public Result create(@PathParam("UUID") final String UUID, final FormDataMultiPart multiPart)
	{
		//file이라는 이름으로 binary를 첨부해야함.
		FormDataBodyPart part = multiPart.getField(FILE);

		//meta정보로 부터 파일이름과 파일용을 가져올 수 있는 streming을 가져옴
		String filename = part.getFormDataContentDisposition().getFileName();
		InputStream stream = part.getValueAs(InputStream.class);

		//mongo DB
		RepoMongo mongo = null;
		try {
			mongo = new RepoMongo(Config.MongoIP, Config.MongoPort, Config.MongoDB);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return new Result(false, "Internal DB error");
		} catch (MongoException e1) {
			e1.printStackTrace();
			return new Result(false, "Internal DB error");
		} catch (IOException e1) {
			e1.printStackTrace();
			return new Result(false, "Internal DB error");
		}

		//mongoDB에 파일 등록
		if(mongo.Regsiter(UUID, filename, stream))
		{
			//정상 종료
			return new Result(true, getLog());
		}
		else
		{
			return new Result(false, "DB Register Error");
		}

		/*
		//파일로 로컬에 저장해 본다.
		 * 
		File file = new File("/Users/jjangg96/tmp", filename); 
		Log(file.getAbsolutePath());

		try {
			out = new FileOutputStream(file);

			byte[] buffer = new byte[4096]; 
			int length=0;

			while((length = stream.read(buffer))!= -1)
			{
				out.write(buffer, 0, length);
				out.flush();
			}

			out.close();
			out=null;

		} catch (FileNotFoundException e) {
			//파일이 없을 수 없나?

		} catch (IOException e) {
			e.printStackTrace();
		}

		 */



	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("{UUID}")
	public Response read(@PathParam("UUID") final String UUID)
	{

		//mongo DB
		RepoMongo mongo = null;
		try {
			mongo = new RepoMongo(Config.MongoIP, Config.MongoPort, Config.MongoDB);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			//Internal DB Error
			return Response.serverError().build();
		} catch (MongoException e1) {
			e1.printStackTrace();
			//Internal DB Error
			return Response.serverError().build();
		} catch (IOException e1) {
			e1.printStackTrace();
			//Internal DB Error
			return Response.serverError().build();
		}

		//저장될 때 받은 실제 이름으로 파일 전송
		RepoScheme scheme = mongo.GetScheme(UUID);

		if(scheme != null)
			return Response.ok(scheme.inputStream, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment;filename="+scheme.filename).build();
		else
			return Response.noContent().build();

	}

	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@Path("{UUID}")
	public Result update(@PathParam("UUID") final String UUID, final FormDataMultiPart multiPart)
	{
		//file이라는 이름으로 binary를 첨부해야함.
		FormDataBodyPart part = multiPart.getField(FILE);

		//meta정보로 부터 파일이름과 파일용을 가져올 수 있는 streming을 가져옴
		String filename = part.getFormDataContentDisposition().getFileName();
		InputStream stream = part.getValueAs(InputStream.class);

		//mongo DB
		RepoMongo mongo = null;
		try {
			mongo = new RepoMongo(Config.MongoIP, Config.MongoPort, Config.MongoDB);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return new Result(false, "Internal DB error");
		} catch (MongoException e1) {
			e1.printStackTrace();
			return new Result(false, "Internal DB error");
		} catch (IOException e1) {
			e1.printStackTrace();
			return new Result(false, "Internal DB error");
		}

		//mongoDB에 파일 등록
		if(mongo.Update(UUID, filename, stream))
		{
			//정상 종료
			return new Result(true, getLog());
		}
		else
		{
			return new Result(false, "DB Register Error");
		}
	}

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	@Path("{UUID}")
	public Result delete(@PathParam("UUID") final String UUID)
	{

		//mongo DB
		RepoMongo mongo = null;
		try {
			mongo = new RepoMongo(Config.MongoIP, Config.MongoPort, Config.MongoDB);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return new Result(false, "Internal DB error");
		} catch (MongoException e1) {
			e1.printStackTrace();
			return new Result(false, "Internal DB error");
		} catch (IOException e1) {
			e1.printStackTrace();
			return new Result(false, "Internal DB error");
		}

		if(mongo.Delete(UUID))
		{
			return new Result(true, "Delete Success");
		}
		else
		{
			return new Result(false, "Delete Faile");
		}


	}
}
