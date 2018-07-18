package com.flosum.dao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.model.CommitFutures;
import com.flosum.model.GitCredentials;
import com.flosum.model.GitPath;
import com.flosum.model.Payload;

public class CacheService {
	private static final Logger LOG = LoggerFactory.getLogger(CacheService.class);
	private static final AtomicLong generateID = new AtomicLong();
	private static final String TEMP_DIR = "data";
	private static final String TEMP_DIR_PATH = TEMP_DIR +File.pathSeparatorChar;
	public static final Long PAYLOAD 		=  1000000000L;
	public static final Long GITCREDS 		=  2000000000L;
	public static final Long COMMITFUTURES  =  4000000000L;
	public static final Long GITPATH 		=  8000000000L;
	public static final Long RAWDATA 		= 16000000000L;
	public static final Long RESULTS 		= 32000000000L;
	public static final Boolean inMemory = false;
	
	private ConcurrentMap<Long,Object> resources;
	
	public CacheService() {
		new ConcurrentHashMap<Long,Payload>();
		new ConcurrentHashMap<Long,GitCredentials>();
		new ConcurrentHashMap<Long,CommitFutures>();
		resources = new ConcurrentHashMap<Long,Object>();
	}

	public Boolean push(String s, Long opId){
		if (s == null || opId == null){
			LOG.info("attempt to push null, opId = {}",opId);
			return false;
		}
		LOG.info(" push s = {}",s);
		resources.putIfAbsent(opId, s);
		return true;
	}

	public String pop( Long opId){
		String out = null;
		if (resources.containsKey(opId)){
			out = (String) resources.remove(opId);
		}
		LOG.info(" pop out = {}",out);
		return out;
	}

	
	public Boolean push(Object o, Long type, Long opId) throws FileNotFoundException, IOException{
		if (o == null){
			LOG.info("attempt to push null, opId = {}",opId);
			return false;
		}
		if (type == PAYLOAD){
			return write(o, getPath(type+opId));
		}else if (type == GITCREDS){
			return write(o, getPath(type+opId));
		}else if (type == COMMITFUTURES){
			return write(o, getPath(type+opId));
		}else if (type == GITPATH){
			return write(o, getPath(type+opId));
		}else if (type == RESULTS){
			return write(o, getPath(type+opId));
		}
		if (inMemory){
			resources.putIfAbsent(opId, o);
		}
		return true;
	}
	
	public Boolean push(Object o, Long type, Long opId, Integer part) throws FileNotFoundException, IOException{
		if (o == null){
			LOG.info("attempt to push null, opId = {}",opId);
			return false;
		}
		if (type == RAWDATA){
			return write(o, getPartialPath(type+opId, part));
		}
		return true;
	}
	
	public Object pop (Long type, Long opId) throws FileNotFoundException, IOException{
		if (type != null && type > 0){
			return read(getPath(type+opId));
		}else{
			if (resources.containsKey(opId)){
				Object obj = resources.get(opId);
				resources.remove(opId);
				return obj;
			}
		}
		return null;
	}

	public Object pop (Long type, Long opId, Integer part) throws FileNotFoundException, IOException{
		if (type == RAWDATA){
			return read(getPartialPath(type+opId, part));
		}
		return null;
	}

	public void drop (Long type, Long opId){
		if (type == null){
			if (resources.containsKey(opId)){
				resources.remove(opId);
			}
		}else{
			File f = new File( getPath(type + opId));
			if (f.exists()){
				f.delete();
			}
		}
	}

	public void drop (Long opId, Integer part){
			File f = new File( getPartialPath(RAWDATA + opId, part));
			if (f.exists()){
				f.delete();
			}
	}
	
	
	private Boolean write(Object o, String path) throws FileNotFoundException, IOException{
		 // verify directory existence
		mkdirs();
		 //serialize the Object
		LOG.info("serialize: {}", path);
	    try (
	      OutputStream file = new FileOutputStream(path);
	      OutputStream buffer = new BufferedOutputStream(file);
	      ObjectOutput output = new ObjectOutputStream(buffer);
	    ){
	      output.writeObject(o);
	      return true;
	    }  
//	    catch(IOException ex){
//	      LOG.debug("Cannot perform output: {}", ex.getMessage());
//	      return false;
//	    }
	}
	
	/**
	 * Creates directory, if not exists
	 * 
	 * @param dir
	 */
	private static void mkdirs() {
		File d = new File(TEMP_DIR_PATH);
		if (!d.exists()) {
			d.mkdirs();
		}
	}


	
	private Object read(String path) throws FileNotFoundException, IOException{
		Object recovered = null;

		LOG.info("deserialize: {}", path);
		//deserialize the  file
		try(
			InputStream file = new AutoDeleteFileInputStream(path);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream (buffer);
		){
			recovered = input.readObject();
			return recovered;
		}
		catch(ClassNotFoundException ex){
			LOG.debug("Cannot perform input. Class not found {}", ex.getMessage());
			return recovered;
		}
//		catch(IOException ex){
//			LOG.debug("Cannot perform input {}", ex.getMessage());
//			return recovered;
//		}
		
	}
	
	private static String getPath(Long id){
		return TEMP_DIR+id+".cache";
	}

	private static String getPartialPath(Long id, Integer part){
		return TEMP_DIR+ id+ "-" + part + ".cache";
	}

	// inner class - implements autodelete after read
	public class AutoDeleteFileInputStream extends FileInputStream {
		   private File file;
		   public AutoDeleteFileInputStream(String fileName) throws FileNotFoundException{
		      this(new File(fileName));
		   }
		   public AutoDeleteFileInputStream(File file) throws FileNotFoundException{
		      super(file);
		      this.file = file;
		   }
		   
		   @Override
		   public void close() throws IOException {
		       try {
		          super.close();
		       } finally {
		          if(file != null) {
		             file.delete();
		             file = null;
		          }
		       }
		   }
	}
	
}
