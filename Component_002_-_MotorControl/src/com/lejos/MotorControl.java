package com.lejos;
import java.util.Random;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.Motor;
import lejos.utility.Delay;

public class MotorControl {
	
	public static void main(String[] args) {
		Motor.A.resetTachoCount();
		Motor.D.resetTachoCount();
		
		Motor.A.rotateTo(0);
	    Motor.D.rotateTo(0);
	    Motor.A.setSpeed(400);
	    Motor.D.setSpeed(400);
	    Motor.A.setAcceleration(800);
	    Motor.D.setAcceleration(800);
	    
	    Motor.A.setSpeed(200);
		Motor.D.setSpeed(200);

		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Motor Control", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		Button.waitForAnyPress();
		
		while (Button.readButtons() != Button.ID_ESCAPE) {
			
			Random random = new Random();
			int currentState = (random.nextInt() % 10 + 10) % 10;
			
			graphicsLCD.clear();
			graphicsLCD.drawString("Motor Control", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString(""+currentState, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			
			if(currentState == 0) {				
				Motor.A.forward();
				Motor.D.forward();
			}
			else if(currentState == 1) {
				Motor.A.backward();
				Motor.D.backward();
			}
			else if(currentState == 2) {
				Motor.A.forward();
				Motor.D.backward();
			}
			else if(currentState == 3) {
				Motor.A.backward();
				Motor.D.forward();
			}
			else if(currentState == 4) {
				Motor.A.rotate(90,true);
				Motor.D.stop();
			}
			else if(currentState == 5) {
				Motor.A.stop();
				Motor.D.rotate(90,true);
			}
			else if(currentState == 6) {
				Motor.A.rotate(90,true);
				Motor.D.stop();
			}
			else if(currentState == 7) {
				Motor.A.stop();
				Motor.D.rotate(90,true);
			}
			else if(currentState == 8) {
				Motor.A.stop();
			}
			else if(currentState == 9) {
				Motor.D.stop();
			}
			
			Delay.msDelay(2000);
	    	
	    	Thread.yield();
		}
	}

}
