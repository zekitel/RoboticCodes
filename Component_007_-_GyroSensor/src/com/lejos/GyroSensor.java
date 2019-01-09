package com.lejos;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class GyroSensor {
	
	static RegulatedMotor leftMotor = Motor.A;
	static RegulatedMotor rightMotor = Motor.D;
	
	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);
	
	public static void main(String[] args) {
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		
		leftMotor.rotateTo(0);
	    rightMotor.rotateTo(0);
	    leftMotor.setAcceleration(800);
	    rightMotor.setAcceleration(800);
	    
	    leftMotor.setSpeed(10);
		rightMotor.setSpeed(10);
		
		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Gyro Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		Button.waitForAnyPress();
	    
	    SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
	    
	    Button.waitForAnyPress();
	    
	    while (Button.readButtons() != Button.ID_ESCAPE) {
	    	if(sampleProvider.sampleSize() > 0) {
	    		float [] sample = new float[sampleProvider.sampleSize()];
		    	sampleProvider.fetchSample(sample, 0);
		    	float angle = sample[0];
		    	
		    	graphicsLCD.clear();
				graphicsLCD.drawString("Gyro Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
				graphicsLCD.drawString(""+angle, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		    	
		    	if(angle > 91) {
		    		leftMotor.forward();
		    		rightMotor.backward();
		    	}
		    	else if(angle < 89) {
		    		leftMotor.backward();
		    		rightMotor.forward();
		    	}
		    	else {
		    		leftMotor.stop(true);
		    		rightMotor.stop(true);
		    	}
		    	
		    	Delay.msDelay(10);
	    	}
	    	
	    	Thread.yield();
		}
	}

}
