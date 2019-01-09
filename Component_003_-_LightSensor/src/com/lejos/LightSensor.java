package com.lejos;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.LightDetectorAdaptor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class LightSensor {
	
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	
	static EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S4);
	static LightDetectorAdaptor lightDetectorAdaptor = new LightDetectorAdaptor((SampleProvider)lightSensor);
	
	static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	
	public static void main(String[] args) {
		
		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Light Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		Button.waitForAnyPress();
		
		while (Button.readButtons() != Button.ID_ESCAPE) {
			long start = System.currentTimeMillis();
			float value = lightDetectorAdaptor.getLightValue();
			long finish = System.currentTimeMillis() - start;
			
			graphicsLCD.clear();
			graphicsLCD.drawString("Light Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString(""+value, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString(""+finish, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 40, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			
			if(value > 0.10) {
				leftMotor.forward();
				rightMotor.forward();
			}
			else {
				leftMotor.stop();
				rightMotor.stop();
			}
			
			Delay.msDelay(100);
			
			Thread.yield();
		}		
	}
}
