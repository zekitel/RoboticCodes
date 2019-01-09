package com.lejos;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class UltrasonicSensor {
	
	static UnregulatedMotor leftMotor = new UnregulatedMotor(MotorPort.A);
	static UnregulatedMotor rightMotor = new UnregulatedMotor(MotorPort.D);
	
	static EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S3);
	
	public static void main(String[] args) {
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		
		leftMotor.setPower(100);
		rightMotor.setPower(100);
		leftMotor.stop();
		rightMotor.stop();
		
		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Ultrasonic Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		Button.waitForAnyPress();
	    
	    SampleProvider sampleProvider = ultrasonicSensor.getDistanceMode();
	   
	    Button.waitForAnyPress();
		
		while (Button.readButtons() != Button.ID_ESCAPE) {
			if(sampleProvider.sampleSize() > 0) {
		    	float [] sample = new float[sampleProvider.sampleSize()];
		    	sampleProvider.fetchSample(sample, 0);
		    	
		    	float distance = sample[0];
		    	
		    	graphicsLCD.clear();
				graphicsLCD.drawString("Ultrasonic Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
				graphicsLCD.drawString(""+distance, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		    	
		    	if(distance > 0.10) {
		    		leftMotor.forward();
		    		rightMotor.forward();
		    	}
		    	else {
		    		leftMotor.stop();
		    		rightMotor.stop();
		    	}
		    	
		    	Delay.msDelay(10);
			}
	    	
	    	Thread.yield();
		}
	}

}
