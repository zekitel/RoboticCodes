package com.lejos;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTSoundSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class SoundSensor {
	
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	
	static boolean isFinish = false;
	
	static UnregulatedMotor leftMotor = new UnregulatedMotor(MotorPort.A);
	static UnregulatedMotor rightMotor = new UnregulatedMotor(MotorPort.D);
	
	static boolean isMoving = false;
	
	static NXTSoundSensor soundSensor = new NXTSoundSensor(SensorPort.S4);
	
	static float getSoundSensorValue() {
		SampleProvider sampleProvider = soundSensor.getDBMode();
		if(sampleProvider.sampleSize() > 0) {
			float [] samples = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(samples, 0);
			return samples[0];
		}
		return -1;		
	}
	
	public static void main(String[] args) throws Exception {
		
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Sound Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		leftMotor.setPower(0);
		rightMotor.setPower(0);
		
		while (Button.readButtons() != Button.ID_ENTER) {
			float value = getSoundSensorValue();
			
			graphicsLCD.clear();
			graphicsLCD.drawString("Sound Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString(""+value, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.refresh();
		}
		
		while (Button.readButtons() != Button.ID_ESCAPE) {
			float value = getSoundSensorValue();
			
			graphicsLCD.clear();
			graphicsLCD.drawString("Sound Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString(""+value, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString("Started", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 40, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.refresh();
			
			if(isMoving && value > 0.7) {
				leftMotor.setPower(0);
				rightMotor.setPower(0);
				
				leftMotor.stop();
				rightMotor.stop();
				isMoving = false;
				Delay.msDelay(500);
			}
			else if(!isMoving && value > 0.7) {
				leftMotor.setPower(100);
				rightMotor.setPower(100);
				
				leftMotor.forward();
				rightMotor.forward();
				isMoving = true;
				Delay.msDelay(500);
			}
		}
	}
}
