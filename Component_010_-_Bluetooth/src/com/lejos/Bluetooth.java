package com.lejos;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Bluetooth {
	
	static EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S3);
	
	public static void main(String[] args) throws Exception {		
		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		ServerSocket serverSocket = new ServerSocket(1234);
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Bluetooth", graphicsLCD.getWidth()/2, 0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Waiting", graphicsLCD.getWidth()/2, 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
		
		Socket client = serverSocket.accept();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Bluetooth", graphicsLCD.getWidth()/2, 0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Connected", graphicsLCD.getWidth()/2, 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Bluetooth", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		OutputStream outputStream = client.getOutputStream();
		
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		
		Button.waitForAnyPress();
	    
	    SampleProvider sampleProvider = ultrasonicSensor.getDistanceMode();
	    
	    while (Button.readButtons() != Button.ID_ESCAPE) {
	    	if(sampleProvider.sampleSize() > 0) {
	    		float [] sample = new float[sampleProvider.sampleSize()];
		    	sampleProvider.fetchSample(sample, 0);
		    	float distance = sample[0];
		    	
		    	dataOutputStream.writeFloat(distance);
				dataOutputStream.flush();
		    	
		    	Delay.msDelay(10);
	    	}
	    	
	    	Thread.yield();
		}
	    
	    dataOutputStream.close();
		serverSocket.close();
	}

}
