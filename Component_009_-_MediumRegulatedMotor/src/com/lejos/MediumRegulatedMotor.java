package com.lejos;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class MediumRegulatedMotor {
	
	static EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S3);
	
	static EV3MediumRegulatedMotor mediumRegulatedMotor = new EV3MediumRegulatedMotor(MotorPort.B);
	
	static boolean isGrippered = false;
	
	public static void main(String[] args) {
		mediumRegulatedMotor.resetTachoCount();
		mediumRegulatedMotor.rotateTo(0);
		
		mediumRegulatedMotor.setSpeed(200);
		
		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Medium Motor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		Button.waitForAnyPress();
	    
	    SampleProvider sampleProvider = ultrasonicSensor.getDistanceMode();
	    
	    while (Button.readButtons() != Button.ID_ESCAPE) {
	    	if(sampleProvider.sampleSize() > 0) {
	    		float [] sample = new float[sampleProvider.sampleSize()];
		    	sampleProvider.fetchSample(sample, 0);
		    	float distance = sample[0];
		    	
		    	if(distance < 0.10 && !isGrippered) {
		    		mediumRegulatedMotor.rotateTo(-120);
		    		isGrippered = true;
		    	}
		    	
		    	Delay.msDelay(10);
	    	}
	    	
	    	Thread.yield();
		}
	}

}
