package com.sesnu.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sesnu.model.CandidateTicker;


public class Test {


	    public static void main( String[] args ){

//	    		Util.sendMail("Hello","4083077700@mms.att.net","test");
	    		
	    		CandidateTicker ct = new CandidateTicker("TEST2",Util.getDateStr(System.currentTimeMillis()),0.5);
	    		DAOService daoService = new DAOService();
	    		daoService.save(ct);
	    			

	        
	    }



}
