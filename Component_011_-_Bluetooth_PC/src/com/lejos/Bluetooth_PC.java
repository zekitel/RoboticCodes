package com.lejos;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;

public class Bluetooth_PC {
	
	public static void main(String[] args) throws Exception	{
		
		String ip = "10.0.1.1";
		
		Socket socket = new Socket(ip, 1234);
		System.out.println("Connected!");
		
		InputStream inputStream = socket.getInputStream();
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		
		while( socket.isConnected() ){
			float distance = dataInputStream.readFloat()*100;
			System.out.println(distance);
		}
		
		socket.close();
	}
}

