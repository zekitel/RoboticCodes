package Final_Project;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTLightSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;
import lejos.utility.PilotProps;

public class final_Robot {

	// FINAL VARIABLES
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;

	// CONNECTION
	public static ServerSocket serverSocket;
	public static Socket client;
	public static OutputStream outputStream;
	public static DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

	// MOTORS
	public static EV3MediumRegulatedMotor mediumRegulatedMotor = new EV3MediumRegulatedMotor(MotorPort.C);
	public static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	public static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	public static MovePilot pilot;

	// SENSORS
	public static NXTLightSensor sen_nxt = new NXTLightSensor(SensorPort.S1);
	private static SampleProvider gyroSampleProvider = sen_nxt.getRedMode();
	private static float[] sample = new float[gyroSampleProvider.sampleSize()];

	public static EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S3);
	public static SampleProvider sampleDistance = ultrasonicSensor.getDistanceMode();

	public static EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S2);
	public static ColorAdapter colorAdapter = new ColorAdapter(colorSensor);

	public static NXTUltrasonicSensor ultra_2 = new NXTUltrasonicSensor(SensorPort.S4);
	public static SampleProvider sampleDistance_2 = ultra_2.getDistanceMode();

	// DATA STRUCTURES
	public static int DIRECTION = NORTH;
	public static int locationX = 0;
	public static int locationY = 0;

	public static ArrayList<Cell> locVisited = new ArrayList<Cell>();
	public static Stack<Integer> locTotalMovement = new Stack<Integer>();
	public static int locDIRECTION = NORTH;
	public static Stack<AvailableWay> locStack = new Stack<AvailableWay>();
	public static int locPosX = 0;
	public static int locPosY = 0;

	public static int ballColor = Color.RED;

	public static int pressed = -1;
	public static boolean supressed = false;
	public static boolean mapping_flag = false;
	public static boolean task_flag = false;

	public static ArrayList<Cell> blackGrids = new ArrayList<Cell>();

	public static ArrayList<Sample> samples = new ArrayList<Sample>();
	public static ArrayList<Cell> visited = new ArrayList<Cell>();
	public static Stack<AvailableWay> stack = new Stack<AvailableWay>();
	public static Stack<Integer> totalMovement = new Stack<Integer>();

	public static void main(String[] args) throws IOException {
		setMotors();

		
		calib();
		pilot.rotate(90);
		pilot.travel(25);
		grap();
		release();
		
		
//		pilot.rotate(90);
//		pilot.rotate(90);
//		pilot.rotate(90);
//		pilot.rotate(90);
//		
//		pilot.rotate(-90);
//		pilot.rotate(-90);
//		pilot.rotate(-90);
//		pilot.rotate(-90);
		
		
		Thread press = new Thread(new Runnable() {
			public void run() {
				while (true) {
					pressed = Button.waitForAnyPress();
				}
			}
		});
		press.start();

		serverSocket = new ServerSocket(1234);
		Sound.beepSequenceUp();
		client = serverSocket.accept();
		outputStream = client.getOutputStream();
		dataOutputStream = new DataOutputStream(outputStream);

		Behavior enterButton_idle = new Behavior() {
			public boolean takeControl() {
				return pressed == Button.ID_ENTER;

			}

			public void suppress() {
			}

			public void action() {
				supressed = false;
				pressed = -1;
			}
		};

		Behavior escapeButton_reset = new Behavior() {
			public boolean takeControl() {
				return pressed == Button.ID_ESCAPE;
			}

			public void suppress() {
			}

			public void action() {
				supressed = false;
				pressed = -1;
			}
		};

		Behavior upButton_mapping = new Behavior() {
			public boolean takeControl() {
				return pressed == Button.ID_UP || mapping_flag;

			}

			public void suppress() {
				supressed = true;
				if(pressed == Button.ID_ESCAPE )
					mapping_flag = true;
			}

			public void action() {
				if(mapping_flag)
					mapping_flag = false;
				mapping();
				pressed = -1;
			}

		};

		Behavior downButton_taskexec = new Behavior() {
			public boolean takeControl() {
				return pressed == Button.ID_DOWN || task_flag;
			}

			public void suppress() {
				supressed = true;
				if(pressed == Button.ID_ESCAPE )
					task_flag = true;
				
			}

			public void action() {
				if(task_flag)
					task_flag = false;
				execTask();
				
				pressed = -1;

			}
		};

		Behavior[] behaviors = { upButton_mapping, downButton_taskexec, escapeButton_reset, enterButton_idle };
		(new Arbitrator(behaviors)).go();

	}
	
	public static void execTask() {
		try {

			File f = new File("map.txt");
			if (!f.exists()) {
				Sound.beepSequence();
				Sound.beepSequence();
				return;
			}

			readMap();

			createSamples();
			boolean done = findMe();

			if (!done)
				return;

			doneLoc();
			
			findPathTo(Color.GREEN);
			goToBasket(Color.GREEN);
			if(supressed)
				return;
			grap();

			pilot.travel(-25);

			if (DIRECTION == NORTH) {
				locationY = locationY - 1;
			} else if (DIRECTION == SOUTH) {
				locationY = locationY + 1;
			} else if (DIRECTION == EAST) {
				locationX = locationX - 1;
			} else if (DIRECTION == WEST) {
				locationX = locationX + 1;
			}

			dataOutputStream.writeInt(1);
			dataOutputStream.flush();

			dataOutputStream.writeInt(locationX);
			dataOutputStream.flush();

			dataOutputStream.writeInt(locationY);
			dataOutputStream.flush();

			for (int i = 0; i < 4; i++) {
				locTurnRight();
			}

			findPathTo(ballColor);
			goToBasket(ballColor);

			release();
			if(supressed)
				return;
			pilot.travel(-15);

			if (DIRECTION == NORTH) {
				locationY = locationY - 1;
			} else if (DIRECTION == SOUTH) {
				locationY = locationY + 1;
			} else if (DIRECTION == EAST) {
				locationX = locationX - 1;
			} else if (DIRECTION == WEST) {
				locationX = locationX + 1;
			}

			dataOutputStream.writeInt(1);
			dataOutputStream.flush();

			dataOutputStream.writeInt(locationX);
			dataOutputStream.flush();

			dataOutputStream.writeInt(locationY);
			dataOutputStream.flush();

			for (int i = 0; i < 4; i++) {
				locTurnRight();
			}

			
			findPathTo(Color.GREEN);
			goToBasket(Color.GREEN);
			if(supressed)
				return;
			grap();

			pilot.travel(-25);

			if (DIRECTION == NORTH) {
				locationY = locationY - 1;
			} else if (DIRECTION == SOUTH) {
				locationY = locationY + 1;
			} else if (DIRECTION == EAST) {
				locationX = locationX - 1;
			} else if (DIRECTION == WEST) {
				locationX = locationX + 1;
			}

			dataOutputStream.writeInt(1);
			dataOutputStream.flush();

			dataOutputStream.writeInt(locationX);
			dataOutputStream.flush();

			dataOutputStream.writeInt(locationY);
			dataOutputStream.flush();

			for (int i = 0; i < 4; i++) {
				locTurnRight();
			}

			findPathTo(ballColor);
			goToBasket(ballColor);

			release();
			if(supressed)
				return;

			pilot.travel(-5);

			if (DIRECTION == NORTH) {
				locationY = locationY - 1;
			} else if (DIRECTION == SOUTH) {
				locationY = locationY + 1;
			} else if (DIRECTION == EAST) {
				locationX = locationX - 1;
			} else if (DIRECTION == WEST) {
				locationX = locationX + 1;
			}

			dataOutputStream.writeInt(1);
			dataOutputStream.flush();

			dataOutputStream.writeInt(locationX);
			dataOutputStream.flush();

			dataOutputStream.writeInt(locationY);
			dataOutputStream.flush();

			for (int i = 0; i < 4; i++) {
				locTurnRight();
			}

			
			
			findPathTo(Color.GREEN);
			goToBasket(Color.GREEN);

			if(supressed)
				return;
			pilot.travel(8);
			for (int i = 0; i < 4; i++) {
				locTurnRight();
			}

			Sound.beepSequence();
			Sound.beepSequence();
			Sound.beepSequence();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void mapping() {
		try {
			traverse();
			if(supressed)
				return;
			writeMapToFile();
			Sound.beepSequence();
			Sound.beepSequence();
			Sound.beepSequence();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readMap() throws IOException {
		dataOutputStream.writeInt(13);
		dataOutputStream.flush();

		Scanner file = new Scanner(new File("map.txt"));
		visited = new ArrayList<Cell>();
		int count = file.nextInt();

		for (int i = 0; i < count; i++) {
			Cell a = new Cell();
			a.relativeX = file.nextInt();
			a.relativeY = file.nextInt();
			a.color = file.nextInt();
			a.visited = file.nextBoolean();
			a.northWall = file.nextBoolean();
			a.eastWall = file.nextBoolean();
			a.southWall = file.nextBoolean();
			a.westWall = file.nextBoolean();

			visited.add(a);

			dataOutputStream.writeInt(12);
			dataOutputStream.flush();

			dataOutputStream.writeInt(a.relativeX);
			dataOutputStream.flush();

			dataOutputStream.writeInt(a.relativeY);
			dataOutputStream.flush();

			dataOutputStream.writeInt(a.color);
			dataOutputStream.flush();

			dataOutputStream.writeBoolean(a.visited);
			dataOutputStream.flush();

			dataOutputStream.writeBoolean(a.northWall);
			dataOutputStream.flush();

			dataOutputStream.writeBoolean(a.eastWall);
			dataOutputStream.flush();

			dataOutputStream.writeBoolean(a.southWall);
			dataOutputStream.flush();

			dataOutputStream.writeBoolean(a.westWall);
			dataOutputStream.flush();

		}
	}

	public static void writeMapToFile() throws IOException {
		File f = new File("map.txt");
		if (f.exists())
			f.delete();
		else
			f.createNewFile();

		PrintWriter writer = new PrintWriter("map.txt", "UTF-8");

//		int relativeX;
//		int relativeY;
//		int color;
//		boolean visited;
//		boolean eastWall;
//		boolean westWall;
//		boolean northWall;
//		boolean southWall;

		writer.println(visited.size());
		for (int i = 0; i < visited.size(); i++) {
			writer.println(visited.get(i).relativeX);
			writer.println(visited.get(i).relativeY);
			writer.println(visited.get(i).color);
			writer.println(visited.get(i).visited);
			writer.println(visited.get(i).northWall);
			writer.println(visited.get(i).eastWall);
			writer.println(visited.get(i).southWall);
			writer.println(visited.get(i).westWall);
		}
		writer.close();

	}

	public static void goToBasket(int cellColor) throws IOException {
		Cell me = returnCell(locationX, locationY);
		Cell next = me.ancestor;
		while (next.color != cellColor) {
			if(supressed)
				return;
			int movingWay = nextMotion(me, next);
			me = next;
			next = me.ancestor;
			moveNow(movingWay);
		}

		int movingWay = nextMotion(me, next);
		int rotationCount = ((movingWay - DIRECTION) + 4) % 4;
		rotate(rotationCount);
		DIRECTION = movingWay;

		dataOutputStream.writeInt(2);
		dataOutputStream.flush();
		dataOutputStream.writeInt(DIRECTION);
		dataOutputStream.flush();

		if (cellColor == Color.GREEN)
			pilot.travel(25);
		else
			pilot.travel(20);

		if (DIRECTION == NORTH) {
			locationY = locationY + 1;
		} else if (DIRECTION == SOUTH) {
			locationY = locationY - 1;
		} else if (DIRECTION == EAST) {
			locationX = locationX + 1;
		} else if (DIRECTION == WEST) {
			locationX = locationX - 1;
		}

		dataOutputStream.writeInt(1);
		dataOutputStream.flush();

		dataOutputStream.writeInt(locationX);
		dataOutputStream.flush();

		dataOutputStream.writeInt(locationY);
		dataOutputStream.flush();
	}

	public static void moveNow(int movement) throws IOException {
		int rotationCount = ((movement - DIRECTION) + 4) % 4;
		rotate(rotationCount);
		DIRECTION = movement;

		dataOutputStream.writeInt(2);
		dataOutputStream.flush();
		dataOutputStream.writeInt(DIRECTION);
		dataOutputStream.flush();

		pilot.travel(33);

		if (DIRECTION == NORTH) {
			locationY = locationY + 1;
		} else if (DIRECTION == SOUTH) {
			locationY = locationY - 1;
		} else if (DIRECTION == EAST) {
			locationX = locationX + 1;
		} else if (DIRECTION == WEST) {
			locationX = locationX - 1;
		}

		dataOutputStream.writeInt(1);
		dataOutputStream.flush();

		dataOutputStream.writeInt(locationX);
		dataOutputStream.flush();

		dataOutputStream.writeInt(locationY);
		dataOutputStream.flush();

		for (int i = 0; i < 4; i++) {
			locTurnRight();
		}
	}

	public static int nextMotion(Cell now, Cell next) {
		if (now.relativeX - next.relativeX == 1) {
			return 3;
		} else if (now.relativeX - next.relativeX == -1) {
			return 1;
		} else if (now.relativeY - next.relativeY == 1) {
			return 2;
		} else if (now.relativeY - next.relativeY == -1) {
			return 0;
		}
		return 0;
	}

	public static void findPathTo(int cellColor) {
		ArrayList<Cell> passedWays = new ArrayList<Cell>();
		Queue<Cell> queue = new LinkedList<Cell>();
		Cell rootNode = returnColorCell(cellColor);
		queue.add(rootNode);

		while (!queue.isEmpty()) {
			boolean passable = false;

			Cell temp = queue.poll();

			if (cellColor == Color.GREEN)
				passable = temp.color == Color.BLACK || temp.color == Color.RED || temp.color == Color.BLUE;
			if (cellColor == Color.BLUE)
				passable = temp.color == Color.BLACK || temp.color == Color.RED || temp.color == Color.GREEN;
			if (cellColor == Color.RED)
				passable = temp.color == Color.BLACK || temp.color == Color.GREEN || temp.color == Color.BLUE;
			if (passable) {
				passedWays.add(temp);
			} else if (!isVisited(passedWays, temp)) {
				passedWays.add(temp);
				if (temp.eastWall) {
					Cell t = returnCell(temp.relativeX + 1, temp.relativeY);
					if (!isVisited(passedWays, t)) {
						queue.add(t);
						t.ancestor = temp;
					}

				}

				if (temp.westWall) {
					Cell t = returnCell(temp.relativeX - 1, temp.relativeY);
					if (!isVisited(passedWays, t)) {
						queue.add(t);
						t.ancestor = temp;
					}
				}

				if (temp.northWall) {
					Cell t = returnCell(temp.relativeX, temp.relativeY + 1);
					if (!isVisited(passedWays, t)) {
						queue.add(t);
						t.ancestor = temp;
					}
				}

				if (temp.southWall) {
					Cell t = returnCell(temp.relativeX, temp.relativeY - 1);
					if (!isVisited(passedWays, t)) {
						queue.add(t);
						t.ancestor = temp;
					}
				}
			}
		}
	}

	public static boolean isVisited(ArrayList<Cell> vis, Cell cell) {
		for (int i = 0; i < vis.size(); i++) {
			if (vis.get(i).relativeX == cell.relativeX && vis.get(i).relativeY == cell.relativeY) {
				return true;
			}
		}
		return false;
	}

	public static Cell returnColorCell(int color) {
		for (int i = 0; i < visited.size(); i++) {
			if (visited.get(i).color == color)
				return visited.get(i);
		}
		return null;
	}

	public static Cell returnCell(int x, int y) {
		for (int i = 0; i < visited.size(); i++) {
			if (visited.get(i).relativeX == x && visited.get(i).relativeY == y)
				return visited.get(i);
		}
		return null;
	}

	public static void calib() {

		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);

		Delay.msDelay(100);

		long current = System.currentTimeMillis();
		while (true) {
			
			float distance = 0;
			while (distance == 0) {
				float[] sample = new float[sampleDistance.sampleSize()];
				sampleDistance.fetchSample(sample, 0);
				distance = sample[0] * 100;
			}

			float distance2 = 0;
			while (distance2 == 0) {
				float[] sample = new float[sampleDistance_2.sampleSize()];
				sampleDistance_2.fetchSample(sample, 0);
				distance2 = sample[0] * 100;
			}

			if (distance > 25 || distance2 > 25)
				break;

			float leftPower = distance2 - 16;
			float rightPower = distance - 13;

			leftMotor.setSpeed(Math.abs(leftPower * 20));
			rightMotor.setSpeed(Math.abs(rightPower * 20));

			if (leftPower > 0)
				leftMotor.forward();
			else
				leftMotor.backward();

			if (rightPower > 0)
				rightMotor.forward();
			else
				rightMotor.backward();
			Delay.msDelay(100);
			if (Math.abs(leftPower * 20) < 2 && Math.abs(rightPower * 20) < 2)
				break;
			if (System.currentTimeMillis() - current > 10000)
				break;
		}

		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);
	}

	public static void getdist() throws IOException {
		float distance = 0;
		if (sampleDistance_2.sampleSize() > 0) {
			float[] sample = new float[sampleDistance_2.sampleSize()];
			sampleDistance_2.fetchSample(sample, 0);
			distance = sample[0] * 100;
		}
		int f = (int) distance;

		dataOutputStream.writeInt(f);
		dataOutputStream.flush();
	}

	public static int getColorRGB(Color k) throws IOException {
		int a = k.getRed();
		int b = k.getGreen();
		int c = k.getBlue();
		if (a < 4 && b < 4 && c < 4)
			return Color.BLACK;
		else if (a < 10 && b > 10 && c < 10)
			return Color.GREEN;
		else if (a > 10 && b < 10 && c < 10)
			return Color.RED;
		else if (a < 10 && b > 9 && c > 10)
			return Color.BLUE;
		else // (a > 17 && b > 17 && c > 17)
			return Color.WHITE;
	}

	// @TODO
	public static boolean findMe() throws IOException {
		for (int i = 0; i < visited.size(); i++) {
			if (visited.get(i).color == Color.BLACK)
				blackGrids.add(visited.get(i));
//			visited.get(i).northWall = !visited.get(i).northWall;
//			visited.get(i).eastWall = !visited.get(i).eastWall;
//			visited.get(i).southWall = !visited.get(i).southWall;
//			visited.get(i).westWall = !visited.get(i).westWall;
		}
		locExploreCell();
		while (!locStack.isEmpty() && samples.size() != 0 && samples.size() != 1) {
			AvailableWay pos = locStack.pop();

			int t = (pos.direction - locDIRECTION + 4) % 4;

			// if (!locAlreadyVisited(pos) && safeMode(t)) {
			if (!locAlreadyVisited(pos)) {
				locGoToPosition(pos);
				locExploreCell();
			}
		}

		dataOutputStream.writeInt(11);
		dataOutputStream.flush();

		dataOutputStream.writeInt(samples.size());
		dataOutputStream.flush();

		if (samples.size() != 1)
			return false;
		return true;

	}

	public static void doneLoc() {
		if (samples.size() == 1) {
			DIRECTION = samples.get(0).direction;
			locationX = samples.get(0).gridPosX;
			locationY = samples.get(0).gridPosY;
		} else {
			DIRECTION = 0;
			locationX = 0;
			locationY = 0;
		}

	}

	public static void initLoc() {
		samples = new ArrayList<Sample>();
		locVisited = new ArrayList<Cell>();
		locTotalMovement = new Stack<Integer>();
		locDIRECTION = NORTH;
		locStack = new Stack<AvailableWay>();
		locPosX = 0;
		locPosY = 0;
		blackGrids = new ArrayList<Cell>();
	}

	public static void locMoveThatWay(int movement, boolean ccc) throws IOException {
		int rotationCount = ((movement - locDIRECTION) + 4) % 4;
		rotate(rotationCount); //
		locDIRECTION = movement;

		// Can wait
		turnSamples(rotationCount);
		dataOutputStream.writeInt(6);
		dataOutputStream.flush();

		dataOutputStream.writeInt(rotationCount);
		dataOutputStream.flush();

//		Delay.msDelay(5000);

		pilot.travel(33);

//		centralizeInGrid();

		if (locDIRECTION == NORTH) {
			locPosY = locPosY + 1;
		} else if (locDIRECTION == SOUTH) {
			locPosY = locPosY - 1;
		} else if (locDIRECTION == EAST) {
			locPosX = locPosX + 1;
		} else if (locDIRECTION == WEST) {
			locPosX = locPosX - 1;
		}

		// Can wait
		forwardSamples();
		dataOutputStream.writeInt(5);
		dataOutputStream.flush();
//		Delay.msDelay(5000);

		if (ccc) {
			for (int i = 0; i < 4; i++) {
				locTurnRight();
			}
		}
	}

	public static boolean locAlreadyVisited(AvailableWay pos) {
		for (int i = 0; i < locVisited.size(); i++) {
			if (locVisited.get(i).relativeX == pos.nextX && locVisited.get(i).relativeY == pos.nextY)
				return true;
		}
		return false;
	}

	public static void locGoToPosition(AvailableWay pos) throws IOException {
		while (pos.currentX != locPosX || pos.currentY != locPosY) {
			int movement = locTotalMovement.pop(); //
			movement = (movement + 2) % 4;
			locMoveThatWay(movement, true);
		}

		locMoveThatWay(pos.direction, false);
		locTotalMovement.push(pos.direction);
	}

	public static void locTurnRight() throws IOException {
//		if(wall)
		calib();

		pilot.rotate(90);

		calib();
		locDIRECTION = (locDIRECTION + 1) % 4;

	}

	public static boolean isBlackGrid(int x, int y) {
		for (int i = 0; i < blackGrids.size(); i++) {
			if (blackGrids.get(i).relativeX == x && blackGrids.get(i).relativeY == y)
				return true;
		}
		return false;
	}

//	public static boolean safeZone(int dir) {
//		for (int i = 0; i < samples.size(); i++) {
//			Sample temp = samples.get(i);
//			dir = (dir + samples.get(i).direction) % 4;
//			if (dir == NORTH) {
//				return !isBlackGrid(samples.get(i).gridPosX, samples.get(i).gridPosY + 1);
//			} else if (dir == EAST) {
//				return !isBlackGrid(samples.get(i).gridPosX + 1, samples.get(i).gridPosY);
//			} else if (dir == SOUTH) {
//				return !isBlackGrid(samples.get(i).gridPosX, samples.get(i).gridPosY - 1);
//			} else if (dir == WEST) {
//				return !isBlackGrid(samples.get(i).gridPosX - 1, samples.get(i).gridPosY);
//			}
//		}
//
//		return true;
//	}

	public static boolean safeZone(int dir) {
		int p;

		for (int i = 0; i < samples.size(); i++) {
			Sample temp = samples.get(i);
			p = (dir + samples.get(i).direction) % 4;
			if (p == NORTH) {
				if (isBlackGrid(samples.get(i).gridPosX, samples.get(i).gridPosY + 1))
					return false;
			} else if (p == EAST) {
				if (isBlackGrid(samples.get(i).gridPosX + 1, samples.get(i).gridPosY))
					return false;
			} else if (p == SOUTH) {
				if (isBlackGrid(samples.get(i).gridPosX, samples.get(i).gridPosY - 1))
					return false;
			} else if (p == WEST) {
				if (isBlackGrid(samples.get(i).gridPosX - 1, samples.get(i).gridPosY))
					return false;
			}
		}

		return true;
	}

	public static void locExploreCell() throws IOException {

		Cell temp = new Cell();

		temp.visited = true;

		temp.relativeX = locPosX;
		temp.relativeY = locPosY;

		int gridColor = getColorRGB(colorAdapter.getColor());

		boolean walls[] = new boolean[4];

		float distance = 0;
		float distance2 = 0;
		for (int i = 0; i < 4; i++) {

//			Delay.msDelay(2000);

			boolean wall = false;
			for (int j = 0; j < 4; j++) {
				if (sampleDistance.sampleSize() > 0) {
					float[] sample = new float[sampleDistance.sampleSize()];
					sampleDistance.fetchSample(sample, 0);
					distance = sample[0] * 100;
				}
				if (distance < 25 && distance != 0) {
					wall = true;
					break;
				}
				Delay.msDelay(60);
			}

			walls[i] = wall;

			dataOutputStream.writeInt(4);
			dataOutputStream.flush();

			dataOutputStream.writeInt(i);
			dataOutputStream.flush();

			dataOutputStream.writeBoolean(wall);
			dataOutputStream.flush();

			dataOutputStream.writeInt(gridColor);
			dataOutputStream.flush();

//			dataOutputStream.writeBoolean(safeZone(i));
//			dataOutputStream.flush();

			eliminateSample(i, wall, gridColor);

			locTurnRight();
		}

//		for (int i = 0; i < 4; i++) {
//			if (!walls[i] && !safeZone(i)) {
//				if ((locDIRECTION+i)%4 == NORTH) {
//					locStack.push(new AvailableWay(locPosX, locPosY, locPosX, locPosY + 1, NORTH));
//				} else if ((locDIRECTION+i)%4 == EAST) {
//					locStack.push(new AvailableWay(locPosX, locPosY, locPosX + 1, locPosY, EAST));
//				} else if ((locDIRECTION+i)%4 == SOUTH) {
//					locStack.push(new AvailableWay(locPosX, locPosY, locPosX , locPosY - 1, SOUTH));
//				} else if ((locDIRECTION+i)%4 == WEST) {
//					locStack.push(new AvailableWay(locPosX, locPosY, locPosX -1, locPosY, WEST));
//				}
//			}
//		}
		for (int j = 0; j < 4; j++) {
			if (!walls[j] && safeZone(j)) {
				if ((locDIRECTION + j) % 4 == NORTH) {
					locStack.push(new AvailableWay(locPosX, locPosY, locPosX, locPosY + 1, NORTH));
				} else if ((locDIRECTION + j) % 4 == EAST) {
					locStack.push(new AvailableWay(locPosX, locPosY, locPosX + 1, locPosY, EAST));
				} else if ((locDIRECTION + j) % 4 == SOUTH) {
					locStack.push(new AvailableWay(locPosX, locPosY, locPosX, locPosY - 1, SOUTH));
				} else if ((locDIRECTION + j) % 4 == WEST) {
					locStack.push(new AvailableWay(locPosX, locPosY, locPosX - 1, locPosY, WEST));
				}
			}
		}

		locVisited.add(temp);

	}

	public static void turnSamples(int turn) {
		for (int i = 0; i < samples.size(); i++) {
			samples.get(i).direction = (samples.get(i).direction + turn) % 4;
		}
	}

	public static void eliminateSample(int dir, boolean exist, int gridColor) {
		for (int i = 0; i < samples.size(); i++) {
			int posX = samples.get(i).gridPosX;
			int posY = samples.get(i).gridPosY;
			int sampleDir = samples.get(i).direction;
			boolean delete = false;

			int num = -1;
			for (int j = 0; j < visited.size(); j++) {
				if (visited.get(j).relativeX == posX && visited.get(j).relativeY == posY) {
					num = j;
					break;
				}
			}
			sampleDir = (sampleDir + dir) % 4;

			if (sampleDir == NORTH) {
				if (visited.get(num).northWall == exist) {
					delete = true;
				}
			} else if (sampleDir == EAST) {
				if (visited.get(num).eastWall == exist) {
					delete = true;
				}

			} else if (sampleDir == SOUTH) {
				if (visited.get(num).southWall == exist) {
					delete = true;
				}
			} else if (sampleDir == WEST) {
				if (visited.get(num).westWall == exist) {
					delete = true;
				}
			}

			if (gridColor != visited.get(num).color)
				delete = true;

			if (delete) {
				samples.remove(i);
				i--;
			}
		}
	}

	public static void createSamples() throws IOException {

		dataOutputStream.writeInt(10);
		dataOutputStream.flush();

		initLoc();

		for (int i = 0; i < visited.size(); i++) {
			for (int j = 0; j < 4; j++) {
				samples.add(new Sample(visited.get(i).relativeX, visited.get(i).relativeY, j));
			}
		}

		dataOutputStream.writeInt(3);
		dataOutputStream.flush();
	}

	public static void traverse() throws IOException {

		dataOutputStream.writeInt(8);
		dataOutputStream.flush();

		resetVars();

		exploreCell();
		while (!stack.isEmpty()) {
			if(supressed)
				return;
			AvailableWay pos = stack.pop();
			if (!alreadyVisited(pos)) {
				goToPosition(pos);
				exploreCell();
			}
		}

		dataOutputStream.writeInt(9);
		dataOutputStream.flush();

	}

	public static void resetVars() {
		DIRECTION = NORTH;
		locationX = 0;
		locationY = 0;

		locVisited = new ArrayList<Cell>();
		locTotalMovement = new Stack<Integer>();
		locDIRECTION = NORTH;
		locStack = new Stack<AvailableWay>();
		locPosX = 0;
		locPosY = 0;

		blackGrids = new ArrayList<Cell>();

		samples = new ArrayList<Sample>();
		visited = new ArrayList<Cell>();
		stack = new Stack<AvailableWay>();
		totalMovement = new Stack<Integer>();
	}

	public static boolean alreadyVisited(AvailableWay pos) {
		for (int i = 0; i < visited.size(); i++) {
			if (visited.get(i).relativeX == pos.nextX && visited.get(i).relativeY == pos.nextY)
				return true;
		}
		return false;
	}

	public static void goToPosition(AvailableWay pos) throws IOException {

		while (pos.currentX != locationX || pos.currentY != locationY) {
			int movement = totalMovement.pop();
			movement = (movement + 2) % 4;
			moveThatWay(movement, true);
			if(supressed)
				return;
		}

		moveThatWay(pos.direction, false);
		totalMovement.push(pos.direction);

	}

	public static void moveThatWay(int movement, boolean ccc) throws IOException {
		int rotationCount = ((movement - DIRECTION) + 4) % 4;
		rotate(rotationCount);
		DIRECTION = movement;

		
		dataOutputStream.writeInt(2);
		dataOutputStream.flush();
		dataOutputStream.writeInt(DIRECTION);
		dataOutputStream.flush();

		// Can wait
		turnSamples(rotationCount);
		Delay.msDelay(100);
		dataOutputStream.writeInt(6);
		dataOutputStream.flush();
		dataOutputStream.writeInt(rotationCount);
		dataOutputStream.flush();

		pilot.travel(33);
		

//		centralizeInGrid();

		if (DIRECTION == NORTH) {
			locationY = locationY + 1;
		} else if (DIRECTION == SOUTH) {
			locationY = locationY - 1;
		} else if (DIRECTION == EAST) {
			locationX = locationX + 1;
		} else if (DIRECTION == WEST) {
			locationX = locationX - 1;
		}

		dataOutputStream.writeInt(1);
		dataOutputStream.flush();

		dataOutputStream.writeInt(locationX);
		dataOutputStream.flush();

		dataOutputStream.writeInt(locationY);
		dataOutputStream.flush();

		// Can wait
		forwardSamples();
		Delay.msDelay(100);
		dataOutputStream.writeInt(5);
		dataOutputStream.flush();

		if (ccc) {
			for (int i = 0; i < 4; i++) {
				turnRight();
			}
		}
	}

	public static void forwardSamples() {
		for (int i = 0; i < samples.size(); i++) {

			int posX = samples.get(i).gridPosX;
			int posY = samples.get(i).gridPosY;
			int sampleDir = samples.get(i).direction;
			boolean delete = false;

			if (sampleDir == 0) {
				samples.get(i).gridPosY++;
			} else if (sampleDir == 1) {
				samples.get(i).gridPosX++;
			} else if (sampleDir == 2) {
				samples.get(i).gridPosY--;
			} else if (sampleDir == 3) {
				samples.get(i).gridPosX--;
			}

			int num = -1;
			for (int j = 0; j < visited.size(); j++) {
				if (visited.get(j).relativeX == samples.get(i).gridPosX
						&& visited.get(j).relativeY == samples.get(i).gridPosY) {
					num = j;
					break;
				}
			}

			if (num == -1) {
				delete = true;
			} else if (sampleDir == 0) {
				if (!visited.get(num).southWall)
					delete = true;
			} else if (sampleDir == 1) {
				if (!visited.get(num).westWall)
					delete = true;
			} else if (sampleDir == 2) {
				if (!visited.get(num).northWall)
					delete = true;
			} else if (sampleDir == 3) {
				if (!visited.get(num).eastWall)
					delete = true;
			}

			if (delete) {
				samples.remove(i);
				i--;
			}
		}
	}

	public static void rotate(int rotation) {
		if (rotation == 0) {

		} else if (rotation == 1) {
			pilot.rotate(90);
			calib();
			pilot.stop();
		} else if (rotation == 2) {
			pilot.rotate(90);
			calib();
			pilot.rotate(90);
			calib();
			pilot.stop();
		} else if (rotation == 3) {
			pilot.rotate(-90);
			calib();
			pilot.stop();
		}

	}

	// Keep an information of already visited grids
	public static Cell exploreCell() throws IOException {
		Cell temp = new Cell();

		temp.visited = true;
		temp.color = getColorRGB(colorAdapter.getColor());

		temp.relativeX = locationX;
		temp.relativeY = locationY;

		boolean isBlack = (temp.color == Color.BLACK);

		boolean wall = false;

		if (isBlack) {
			temp.northWall = true;
			temp.eastWall = true;
			temp.southWall = true;
			temp.westWall = true;
		}

		if (!isBlack) {
			float distance = 0;
			float distance2 = 0;
			for (int i = 0; i < 4; i++) {
				wall = false;
//			Delay.msDelay(2000);

				for (int j = 0; j < 4; j++) {
					if (sampleDistance.sampleSize() > 0) {
						float[] sample = new float[sampleDistance.sampleSize()];
						sampleDistance.fetchSample(sample, 0);
						distance = sample[0] * 100;
					}

					if (distance < 25 && distance != 0) {
						wall = true;
						break;
					}
					Delay.msDelay(60);
				}

				if (distance < 25 && distance != 0) {
					wall = true;
				} else if (DIRECTION == NORTH) {
//					if (!isBlack)
					stack.push(new AvailableWay(locationX, locationY, locationX, locationY + 1, NORTH));
					temp.northWall = true;
				} else if (DIRECTION == EAST) {
//					if (!isBlack)
					stack.push(new AvailableWay(locationX, locationY, locationX + 1, locationY, EAST));
					temp.eastWall = true;
				} else if (DIRECTION == WEST) {
//					if (!isBlack)
					stack.push(new AvailableWay(locationX, locationY, locationX - 1, locationY, WEST));
					temp.westWall = true;
				} else if (DIRECTION == SOUTH) {
//					if (!isBlack)
					stack.push(new AvailableWay(locationX, locationY, locationX, locationY - 1, SOUTH));
					temp.southWall = true;
				}

				turnRight();
			}
		}
		visited.add(temp);
		sendCellInfo(temp);

		if (isBlack) {
			pilot.travel(-33);
			totalMovement.pop();

			if (DIRECTION == NORTH) {
				locationY = locationY - 1;
			} else if (DIRECTION == SOUTH) {
				locationY = locationY + 1;
			} else if (DIRECTION == EAST) {
				locationX = locationX - 1;
			} else if (DIRECTION == WEST) {
				locationX = locationX + 1;
			}

			dataOutputStream.writeInt(1);
			dataOutputStream.flush();

			dataOutputStream.writeInt(locationX);
			dataOutputStream.flush();

			dataOutputStream.writeInt(locationY);
			dataOutputStream.flush();

		}

		return temp;
	}

	public static void sendCellInfo(Cell temp) throws IOException {
		dataOutputStream.writeInt(0);
		dataOutputStream.flush();

		dataOutputStream.writeInt(temp.color);
		dataOutputStream.flush();

		dataOutputStream.writeInt(temp.relativeX);
		dataOutputStream.flush();

		dataOutputStream.writeInt(temp.relativeY);
		dataOutputStream.flush();

		dataOutputStream.writeBoolean(!temp.northWall);
		dataOutputStream.flush();

		dataOutputStream.writeBoolean(!temp.eastWall);
		dataOutputStream.flush();

		dataOutputStream.writeBoolean(!temp.southWall);
		dataOutputStream.flush();

		dataOutputStream.writeBoolean(!temp.westWall);
		dataOutputStream.flush();

	}

	public static void turnRight() throws IOException {
//		if(wall)
		calib();

		pilot.rotate(90);
		calib();

		DIRECTION = (DIRECTION + 1) % 4;

		dataOutputStream.writeInt(2);
		dataOutputStream.flush();

		dataOutputStream.writeInt(DIRECTION);
		dataOutputStream.flush();

	}

	public static void grap() {
		sen_nxt.fetchSample(sample, 0);
		double t_value_nxt = sample[0];
		mediumRegulatedMotor.resetTachoCount();
		while (mediumRegulatedMotor.getTachoCount() < 1100) {
			mediumRegulatedMotor.forward();
		}
		mediumRegulatedMotor.stop();

		sen_nxt.fetchSample(sample, 0);
		t_value_nxt = sample[0];
		if (t_value_nxt > 0.40)
			ballColor = Color.RED;
		else
			ballColor = Color.BLUE;
	}

	public static void release() {
		mediumRegulatedMotor.resetTachoCount();
		while (mediumRegulatedMotor.getTachoCount() > -1100) {
			mediumRegulatedMotor.backward();
		}
		mediumRegulatedMotor.stop();
	}

	public static void setMotors() throws IOException {
		mediumRegulatedMotor.setSpeed(400);

		PilotProps pilotProps = new PilotProps();
		pilotProps.setProperty(PilotProps.KEY_WHEELDIAMETER, "5.4");
		pilotProps.setProperty(PilotProps.KEY_TRACKWIDTH, "10.8");
		pilotProps.setProperty(PilotProps.KEY_LEFTMOTOR, "A");
		pilotProps.setProperty(PilotProps.KEY_RIGHTMOTOR, "D");
		pilotProps.setProperty(PilotProps.KEY_REVERSE, "false");
		pilotProps.storePersistentValues();
		pilotProps.loadPersistentValues();

		float wheelDiameter = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_WHEELDIAMETER, "5.4"));
		float trackWidth = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_TRACKWIDTH, "10.8"));
		boolean reverse = Boolean.parseBoolean(pilotProps.getProperty(PilotProps.KEY_REVERSE, "false"));

		Chassis chassis = new WheeledChassis(
				new Wheel[] {
						WheeledChassis.modelWheel(leftMotor, wheelDiameter).offset(-trackWidth / 2).invert(reverse),
						WheeledChassis.modelWheel(rightMotor, wheelDiameter).offset(trackWidth / 2).invert(reverse) },
				WheeledChassis.TYPE_DIFFERENTIAL);

		pilot = new MovePilot(chassis);
		pilot.setLinearSpeed(10);
//		pilot.setLinearAcceleration(30);
		pilot.setAngularSpeed(30);
		pilot.stop();
	}

}
