package io.pivotal.pa.appautoscaler.helper;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.codec.Base64;

public class Connection {
	
	public static HttpHeaders getAuthorizationHeader(String clientId, String clientSecret, String accessToken, String host) {
		String creds = String.format("%s:%s", clientId, clientSecret);
		try {
			
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.add("Authorization", "Basic " + new String(Base64.encode(creds.getBytes("UTF-8"))));
			headers.add("Authorization", "bearer " + accessToken);
			headers.add("Host", host);
			
			return headers;
			
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Could not convert String");
		}
	}
	
	public static HttpHeaders getAuthorizationHeader(String accessToken, String host) {		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", "bearer " + accessToken);
		headers.add("Host", host);
		
		return headers;
	}
	
	public static HttpHeaders getAuthorizationHeader(String clientId, String clientSecret, String host) {
		String creds = String.format("%s:%s", clientId, clientSecret);
		try {
			
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.add("Authorization", "Basic " + new String(Base64.encode(creds.getBytes("UTF-8"))));
			headers.add("Host", host);
			
			return headers;
			
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Could not convert String");
		}
	}
	
	public static HttpHeaders getBasicAuthorizationHeader(String clientId, String clientSecret) {
		String creds = String.format("%s:%s", clientId, clientSecret);
		try {
			
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Basic " + new String(Base64.encode(creds.getBytes("UTF-8"))));
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			
			return headers;
			
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Could not convert String");
		}
	}
}
