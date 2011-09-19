package com.freegine.repo.service;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.freegine.repo.library.Config;
import com.freegine.repo.library.RepoMongo;
import com.freegine.repo.library.RepoScheme;
import com.freegine.repo.library.RepoUtil;
import com.freegine.repo.library.ZipUtil;
import com.freegine.repo.service.basic.WebService;
import com.freegine.repo.service.result.Result;
import com.mongodb.MongoException;
import com.sun.jersey.spi.resource.Singleton;


@Singleton
@Path("Tool")
public class Tool  extends WebService
{

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("Send/{IP}/{Port}/{UUID}")
	public Result send(@PathParam("IP") final String IP, @PathParam("Port") final String Port, @PathParam("UUID") final String UUID)
	{

		Log("IP : " + IP + " port = " + Port + " UUID : " + UUID);


		//mongo DB
		RepoMongo mongo = null;
		try {
			mongo = new RepoMongo(Config.MongoIP, Config.MongoPort, Config.MongoDB);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			//Internal DB Error
		} catch (MongoException e1) {
			e1.printStackTrace();
			//Internal DB Error
		} catch (IOException e1) {
			e1.printStackTrace();
			//Internal DB Error
		}

		//저장될 때 받은 실제 이름으로 파일 전송

		//호스트 정보가 존재 하면
		//mongo db에서 정보 조회 

		RepoScheme scheme = mongo.GetScheme(UUID);

		if(scheme != null)
		{

			//long 을 강제로 int 로 바꿈
			//큰 파일에서 오류 발생 가능성 있음
			//해당 주소로 파일 보냄
			RepoSocket socket = new RepoSocket(IP, Integer.parseInt(Port));
			socket.Send((int)scheme.filesize,  scheme.inputStream);

			return new Result(true, getLog());
		}

		return new Result("No Data about " + UUID);


	}


	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("Zip/{UUIDS}")
	public Response zip(@PathParam("UUIDS") final String UUIDS)
	{
		String[] UUIDList = UUIDS.split(",");

		//mongo DB
		RepoMongo mongo = null;

		try {
			mongo = new RepoMongo(Config.MongoIP, Config.MongoPort, Config.MongoDB);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			//Internal DB Error
		} catch (MongoException e1) {
			e1.printStackTrace();
			//Internal DB Error
		} catch (IOException e1) {
			e1.printStackTrace();
			//Internal DB Error
		}

		RepoScheme[] schemes = mongo.GetSchemes(UUIDList);
		File tmpFile = null;

		try {
			tmpFile = File.createTempFile("" + System.currentTimeMillis(), ".zip");

			if(ZipUtil.zip(schemes, tmpFile.getPath()))
				return Response.ok(new FileInputStream(tmpFile), MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".zip").build();


		} catch (IOException e) {
			e.printStackTrace();
		}

		return Response.serverError().build();

	}


	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("ZipSend/{IP}/{Port}/{UUIDS}")
	public Result zipSend(@PathParam("IP") final String IP, @PathParam("Port") final String Port, @PathParam("UUIDS") final String UUIDS)
	{
		String[] UUIDList = UUIDS.split(",");

		//mongo DB
		RepoMongo mongo = null;

		try {
			mongo = new RepoMongo(Config.MongoIP, Config.MongoPort, Config.MongoDB);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			//Internal DB Error
		} catch (MongoException e1) {
			e1.printStackTrace();
			//Internal DB Error
		} catch (IOException e1) {
			e1.printStackTrace();
			//Internal DB Error
		}

		RepoScheme[] schemes = mongo.GetSchemes(UUIDList);
		File tmpFile = null;

		try 
		{
			tmpFile = File.createTempFile("" + System.currentTimeMillis(), ".zip");

			if(ZipUtil.zip(schemes, tmpFile.getPath()))
			{
				FileInputStream fis = new FileInputStream(tmpFile);

				//WARNING
				//long 을 강제로 int 로 바꿈
				//큰 파일에서 오류 발생 가능성 있음
				//해당 주소로 파일 보냄
				RepoSocket socket = new RepoSocket(IP, Integer.parseInt(Port));
				socket.Send((int)tmpFile.length(),  fis);

				if(fis != null)
					fis.close();

				return new Result(true, getLog());
			}
			else
			{
				System.out.println("Zip Failed " + tmpFile.getPath());
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		return new Result("Zip Failed");

	}




	private class RepoSocket
	{
		private String IP;
		private int Port;
		private String tmpFilePath = "~/";

		public RepoSocket(String IP, int Port)
		{
			this.IP = IP;
			this.Port = Port;
		}

		public void Send(int filesize, InputStream is)
		{
			Log(IP + ":" + Port + "(" + filesize + ")");

			Socket socket;
			try {
				//지정된 서버로 접속 
				socket = new Socket(IP, Port);

				Log("Send");

				//big endian 형식으로 설정
				//4+4+4+4 해더 설정 
				ByteBuffer header = ByteBuffer.allocate(16);
				header.order(ByteOrder.BIG_ENDIAN);

				//값 설정 
				//현재는 임시로 1,2,3 값을 넣고
				//사이즈는 실제 파일 사이즈를 넣음 
				long uSource = RepoUtil.UnsignedUtil.int2uint(1);
				long uDestination = RepoUtil.UnsignedUtil.int2uint(2);
				long uPacketID = RepoUtil.UnsignedUtil.int2uint(3);
				long uBodySize = RepoUtil.UnsignedUtil.int2uint(filesize);

				header.putInt((int)uSource);
				header.putInt((int)uDestination);
				header.putInt((int)uPacketID);
				header.putInt((int)uBodySize);
				header.flip();

				//전송 
				WritableByteChannel channel = Channels.newChannel(socket.getOutputStream());
				channel.write(header);


				OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());

				//파일 전송 
				DataInputStream dis = new DataInputStream(is);
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				byte[] buffer = new byte[4096];
				int length = 0;
				while((length = dis.read(buffer)) != -1)
				{

					dos.write(buffer, 0, length);
					dos.flush();

				}

				dos.close();
				dis.close();
				writer.close();
				socket.close();
			} catch (UnknownHostException e) {
				Log(IP + ":" + Port + " 접속 불가");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


}
