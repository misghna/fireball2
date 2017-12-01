package com.sesnu.service;

import java.util.List;

import org.hibernate.Query;

import org.hibernate.Session;
import org.hibernate.cfg.AnnotationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sesnu.model.Trade;

public class DAOService {

		private static final Logger mainL = LoggerFactory.getLogger("MainLog");
		private Session session;
		
	 	public DAOService(){
	 		session = getSession();
	 	}
	 	
	    private synchronized Session getSession() {
	    	if(session!=null && session.isConnected()){
	    		return session;
	    	}
	    	
	        try {
	            // Create the SessionFactory from hibernate.cfg.xml
	            return new AnnotationConfiguration().configure().buildSessionFactory().openSession();

	        }
	        catch (Throwable ex) {
	            // Make sure you log the exception, as it might be swallowed
	            mainL.error("Initial SessionFactory creation failed.",ex);
	            throw new ExceptionInInitializerError(ex);
	        }
	    }

	    public void shutdown() {
	    	// Close caches and connection pools
	    	session.close();
	    }

	    
	    public synchronized void save(Object obj){
	    	try{
		    	Session s = getSession();
		    	s.beginTransaction();
		    	getSession().save(obj);
		    	s.getTransaction().commit();
	    	}catch(Exception e){
	    		mainL.error("Error saving db",e);
	    	}
	    }
	    
	    public synchronized void update(int orderId,double avgPrice){
	    	try{
		    	Session s = getSession();
		    	s.beginTransaction();
		    	Query query = s.createQuery("update Trade set avgPrice = :avgPrice "
		    						+ "where orderId = :orderId and time > :time and mode = :mode");
		    	query.setDouble("avgPrice",avgPrice);
		    	query.setInteger("orderId",orderId);
		    	query.setLong("time", Util.getDayStartTime());
		    	query.setString("mode", Util.getMode());
		    	query.executeUpdate();
		    	s.getTransaction().commit();
	    	}catch(Exception e){
	    		mainL.error("Error updating db",e);
	    	}
	    }
	    
	    public synchronized List<Trade> get(int orderId){
	    	Session s = getSession();
	    	s.beginTransaction();
	    	Query query = s.createQuery("from Trade where orderId = :orderId and ticker = :ticker");
	    	List<Trade> list = query.list();
	    	return list;
	    }
	    
	    public synchronized List<Trade> getAll(){
	    	Session s = getSession();
	    	s.beginTransaction();
	    	Query query = s.createQuery("from Trade where time > :time");
	    	query.setLong("time", Util.getDayStartTime());
	    	List<Trade> list = query.list();
	    	return list;
	    }
	    
	    
}
