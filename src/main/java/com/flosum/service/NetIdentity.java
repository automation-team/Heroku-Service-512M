package com.flosum.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetIdentity {

    private String loopbackHost = "";
    private String host = "";

    private String loopbackIp = "";
    private List<NetWrapper> ipLi = new ArrayList<NetWrapper>();
    public NetIdentity(){

        try{
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while(interfaces.hasMoreElements()){
                NetworkInterface i = interfaces.nextElement();
                if(i != null){
                    Enumeration<InetAddress> addresses = i.getInetAddresses();
                    System.out.println(i.getDisplayName());
                    while(addresses.hasMoreElements()){
                        InetAddress address = addresses.nextElement();
                        String hostAddr = address.getHostAddress();

                        // local loopback
                        if(hostAddr.indexOf("127.") == 0 ){
                        }

                        // internal ip addresses (behind this router)
                        if( hostAddr.indexOf("192.168") == -1 || 
                                hostAddr.indexOf("10.") == -1 || 
                                hostAddr.indexOf("172.16") == -1 ){
                            this.ipLi.add(new NetWrapper (address.getHostName(),address.getHostAddress()));
                        }
                    }
                }
            }
            String ext = getIpAddr();
            if ( ext != null) {
            	this.ipLi.add(new NetWrapper("Determined by checkip.amazonaws.com",ext)); 
            }else {
            }
        }
        catch(SocketException e){
        } 
        catch (MalformedURLException e) {
		}

        try{
            InetAddress loopbackIpAddress = InetAddress.getLocalHost();
            this.loopbackIp = loopbackIpAddress.getHostName();
        }
        catch(UnknownHostException e){
        }
    }
    
    public static String getIpAddr() throws MalformedURLException{
        URL whatismyip = new URL("http://checkip.amazonaws.com");

        try ( BufferedReader in  = new BufferedReader(new InputStreamReader(whatismyip.openStream()))){
            String ip = in.readLine();
            return ip;
        } catch (Exception e) {
        }
		return null;
    }

    public List<NetWrapper> getIp(){
        return ipLi;
    }
    
    public class NetWrapper{
        private String host = "";
        private String ip = "";
        
        public NetWrapper(String host,String ip) {
        	this.host = host;
        	this.ip = ip;
        }

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}
    	
    }
}

