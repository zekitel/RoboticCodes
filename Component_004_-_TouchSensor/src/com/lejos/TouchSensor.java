package com.lejos;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.TouchAdapter;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.PilotProps;

public class TouchSensor {
	
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	
	static boolean isFinish = false;

	static MovePilot pilot;
	
	static EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S2);
	static TouchAdapter touchAdapter = new TouchAdapter(touchSensor);
	
	public static void main(String[] args) throws Exception {
		PilotProps pilotProps = new PilotProps();
		pilotProps.setProperty(PilotProps.KEY_WHEELDIAMETER, "4.96");
		pilotProps.setProperty(PilotProps.KEY_TRACKWIDTH, "13.0");
		pilotProps.setProperty(PilotProps.KEY_LEFTMOTOR, "A");
		pilotProps.setProperty(PilotProps.KEY_RIGHTMOTOR, "D");
		pilotProps.setProperty(PilotProps.KEY_REVERSE, "false");
		pilotProps.storePersistentValues();
		pilotProps.loadPersistentValues();
    	
    	EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
    	
    	float wheelDiameter = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_WHEELDIAMETER, "4.96"));
    	float trackWidth = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_TRACKWIDTH, "13.0"));
    	boolean reverse = Boolean.parseBoolean(pilotProps.getProperty(PilotProps.KEY_REVERSE, "false"));
    	
    	Chassis chassis = new WheeledChassis(new Wheel[]{WheeledChassis.modelWheel(leftMotor,wheelDiameter).offset(-trackWidth/2).invert(reverse),WheeledChassis.modelWheel(rightMotor,wheelDiameter).offset(trackWidth/2).invert(reverse)}, WheeledChassis.TYPE_DIFFERENTIAL);
    	
    	pilot = new MovePilot(chassis);
    	pilot.setLinearSpeed(5);
    	pilot.setAngularSpeed(10);
    	pilot.stop();
    	
    	Behavior stop = new Behavior() {
    		
			public boolean takeControl() {
				if (touchAdapter.isPressed()) {
					return true;
				}
				else {
					return false;
				}
			}
			
			public void suppress() {}
			
			public void action() {
				pilot.stop();
				isFinish = true;
			}
		};
		
		Behavior offline = new Behavior() {
			
			boolean suppressed = false;
			
			public boolean takeControl() {
				return !isFinish;
			}
			
			public void suppress() {
				suppressed = true;
			}
			
			public void action() {
				pilot.forward();
                while(!suppressed) {
                	Thread.yield();
                }
                suppressed = false;
			}
		};
		
		Behavior[] behaviors = {offline,stop};
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		graphicsLCD.clear();
		graphicsLCD.drawString("Touch Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 , GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
        Button.waitForAnyPress();
        graphicsLCD.drawString("Started", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20 , GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
        graphicsLCD.refresh();
	    (new Arbitrator(behaviors)).go();
	}
}
