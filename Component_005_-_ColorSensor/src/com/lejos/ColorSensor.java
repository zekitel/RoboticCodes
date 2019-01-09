package com.lejos;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.utility.Delay;

public class ColorSensor {
	
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	
	static EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S4);
	static ColorAdapter colorAdapter = new ColorAdapter(colorSensor);
	
	public static void main(String[] args) {
		
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Color Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		Button.waitForAnyPress();
		
		while (Button.readButtons() != Button.ID_ESCAPE) {
			Color color = colorAdapter.getColor();
			
			graphicsLCD.clear();
			graphicsLCD.drawString("R : " + color.getRed(), 10, 20 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT);
			graphicsLCD.drawString("G : " + color.getGreen(), 10, 40 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT);
			graphicsLCD.drawString("B : " + color.getBlue(), 10, 60 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT);
			
			Delay.msDelay(100);
			
			Thread.yield();
		}		
	}
}
