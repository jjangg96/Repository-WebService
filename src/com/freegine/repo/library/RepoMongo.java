package com.freegine.repo.library;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.tomcat.jni.Time;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

public class RepoMongo {
	Mongo mongo;
	DB db;
	GridFS fs;
	DBCollection collection;

	public RepoMongo(String database) throws MongoException, IOException
	{
		//default port 27017
		this("localhost", 27017, database);
	}

	public RepoMongo(String host, String database) throws MongoException, IOException
	{
		this(host, 27017, database);
	}

	public RepoMongo(String host, int port, String database) throws UnknownHostException, MongoException, IOException
	{
		mongo = new Mongo(host, port);
		db = mongo.getDB(database);

		//몽고가 사용하는 file system
		fs = new GridFS(db);

		//default collection repo
		collection = db.getCollection("repo");

	}
	public boolean Delete(String UUID)
	{
		//UUID로 파일 삭제 
		//1. UUID로 저장된 파일 이름 가져오기
		//2. DB삭제
		//3. 저장된 파일이름을 파일시스템에서 삭제

		//where 절
		BasicDBObject where = new BasicDBObject();
		where.put("UUID", UUID);

		//where 절로 row를 가져옴 
		DBObject item = collection.findOne(where);

		if(item == null)
			return false;

		//1. 이름 가져오기 
		//row에서 각각의 데이터 빼냄
		String saveFilename = (String) item.get("SAVED_FILENAME");

		//2. 삭제!
		collection.remove(where);

		//3. 파일시스템에서 삭제 
		fs.remove(saveFilename);

		return true;
	}

	public boolean Regsiter(String UUID, String filename, InputStream is)
	{
		//UUID_기존filename_Tick 형식의 새로운 이름 생성 
		String saveFilename = UUID + "_" + filename + "_" + System.currentTimeMillis();

		//fs에 파일 업로드 
		GridFSInputFile file = fs.createFile(is);
		file.setFilename(saveFilename);
		file.save();

		//저장소 db에 등록
		//where절
		BasicDBObject where = new BasicDBObject();
		where.put("UUID", UUID);

		//update절
		BasicDBObject query = new BasicDBObject();
		query.append("FILENAME", filename);
		query.append("FILE_ID", file.getId());
		query.append("SAVED_FILENAME", saveFilename);
		query.append("FILELENGTH", file.getLength());
		query.append("UPDATED_AT", new Date());

		//set형식으로 등록
		BasicDBObject set = new BasicDBObject("$set", query);
		collection.update(where, set, true, false);

		return true;
	}

	public boolean Update(String UUID, String filename, InputStream is)
	{
		//UUID_기존filename 형식의 새로운 이름 생성 
		String saveFilename = "";

		//where절
		BasicDBObject where = new BasicDBObject();
		where.put("UUID", UUID);

		DBObject item = collection.findOne(where);

		if(item != null)
		{
			//기존 파일 삭제(덮어씀)
			saveFilename = (String) item.get("SAVED_FILENAME");
			fs.remove(saveFilename);
		}

		//새로 저장될 이름
		saveFilename = UUID + "_" + filename + "_" + System.currentTimeMillis();

		//fs에 파일 업로드 
		GridFSInputFile file = fs.createFile(is);
		file.setFilename(saveFilename);
		file.save();

		//저장소 db에 등록

		//update절
		BasicDBObject query = new BasicDBObject();
		query.append("FILENAME", filename);
		query.append("FILE_ID", file.getId());
		query.append("SAVED_FILENAME", saveFilename);
		query.append("FILELENGTH", file.getLength());
		query.append("UPDATED_AT", new Date());

		//set형식으로 등록
		BasicDBObject set = new BasicDBObject("$set", query);
		collection.update(where, set, true, false);

		return true;
	}

	public RepoScheme[] GetSchemes(String[] UUIDList)
	{
		ArrayList<RepoScheme> schemeList = new ArrayList<RepoScheme>();
		
		for(String UUID : UUIDList)
		{
			schemeList.add(GetScheme(UUID));
		}
		
		return schemeList.toArray(new RepoScheme[schemeList.size()]);
	}
	
	public RepoScheme GetScheme(String UUID)
	{
		//저장소 db에 정보 조회

		//사용할 기본 스키마 
		RepoScheme scheme = null;

		//where 절
		BasicDBObject where = new BasicDBObject();
		where.put("UUID", UUID);

		//where 절로 row를 가져옴 
		DBObject item = collection.findOne(where);

		if(item != null)
		{
			scheme = new RepoScheme();

			//row에서 각각의 데이터 빼냄
			String filename = (String) item.get("FILENAME");
			String saveFilename = (String) item.get("SAVED_FILENAME");
			long filesize = (Long)item.get("FILELENGTH");

			//위에서 빼낸 정보로 fs에서 파일 가져옴 
			GridFSDBFile file = fs.findOne(saveFilename);

			//스키마에 맞춰서 데이터 입력 
			scheme.UUID = UUID;
			scheme.filename = filename;
			scheme.inputStream = file.getInputStream();
			scheme.filesize = filesize;
		}
		//리턴 
		return scheme;
	}
}
