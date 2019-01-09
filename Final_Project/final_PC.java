package Final_Project;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JFrame;

public class final_PC extends JFrame {

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;

	public static InputStream inputStream;
	public static DataInputStream dataInputStream;

	public static String ip = "10.0.1.1";

	public static ArrayList<Cell> cells = new ArrayList<Cell>();

	public static int color = 0;
	public static int posX = 0;
	public static int posY = 0;
	public static boolean northWall = false;
	public static boolean eastWall = false;
	public static boolean southWall = false;
	public static boolean westWall = false;

	public static int direction = NORTH;
	public static int carLocX = 0;
	public static int carLocY = 0;

	public static ArrayList<Sample> samples = new ArrayList<Sample>();
	public static boolean samplesON = false;

	public static Color sampleColor = Color.YELLOW;
	public static Color wallColor = Color.ORANGE;

	public final_PC() {
		super("Final Project");
		setSize(1000, 1000);
		setVisible(true);

	}

	public static void main(String[] args) throws UnknownHostException, IOException {

		final_PC monitor = new final_PC();
		monitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Socket socket = new Socket(ip, 1234);
		System.out.println("Connected!");

		monitor.repaint();

		inputStream = socket.getInputStream();
		dataInputStream = new DataInputStream(inputStream);

		while (true) {
			int spec = dataInputStream.readInt();
			System.out.println(spec);
			if (spec == 0) {
				color = dataInputStream.readInt();
				posX = dataInputStream.readInt();
				posY = dataInputStream.readInt();
				carLocX = posX;
				carLocY = posY;
				northWall = dataInputStream.readBoolean();
				eastWall = dataInputStream.readBoolean();
				southWall = dataInputStream.readBoolean();
				westWall = dataInputStream.readBoolean();
				cells.add(new Cell(color, posX, posY, northWall, eastWall, southWall, westWall));
			} else if (spec == 1) {
				carLocX = dataInputStream.readInt();
				carLocY = dataInputStream.readInt();
			} else if (spec == 2) {
				direction = dataInputStream.readInt();
			} else if (spec == 3) { // SAMPLES <--> Localization
				sampleRects();
				samplesON = true;
			} else if (spec == 4) { // SAMPLES elimination <--> Localization
				int wallDir = dataInputStream.readInt();
				boolean wallExist = dataInputStream.readBoolean();
				int gridColor = dataInputStream.readInt();
				eliminateSamples(wallDir, wallExist, gridColor);
			} else if (spec == 5) {
				forwardSamples();
			} else if (spec == 6) {
				int turn = dataInputStream.readInt();
				turnSamples(turn);
			} else if (spec == 7) {
				carLocX = dataInputStream.readInt();
				carLocY = dataInputStream.readInt();
				direction = dataInputStream.readInt();
			} else if (spec == 8) { // Mapping Init
				initMap();
			} else if (spec == 9) { // Mapping Done
			}

			else if (spec == 10) { // Localization Init
				initLoc();
			} else if (spec == 11) { // Localization Done
				doneLoc(dataInputStream.readInt());
			} else if (spec == 12) { // READ MAP
				Cell temp = new Cell();
				temp.relativeX = dataInputStream.readInt();
				temp.relativeY = dataInputStream.readInt();
				temp.color = dataInputStream.readInt();
				temp.visited = dataInputStream.readBoolean();
				temp.northWall = !dataInputStream.readBoolean();
				temp.eastWall = !dataInputStream.readBoolean();
				temp.southWall = !dataInputStream.readBoolean();
				temp.westWall = !dataInputStream.readBoolean();
				cells.add(temp);
			} else if (spec == 13) { // DELETE MAP
				cells = new ArrayList<Cell>();
			}
			monitor.repaint();

		}

	}

	public static void doneLoc(int f) {
		System.out.println("SAMPLE SİZE --->" + f);
		if (f == 1) {
			samplesON = false;

			direction = samples.get(0).direction;
			carLocX = samples.get(0).gridPosX;
			carLocY = samples.get(0).gridPosY;

			samples = new ArrayList<Sample>();

		}
	}

	public static void initLoc() {
		samples = new ArrayList<Sample>();
		samplesON = true;
	}

	public static void initMap() {
		cells = new ArrayList<Cell>();

		color = 0;
		posX = 0;
		posY = 0;
		northWall = false;
		eastWall = false;
		southWall = false;
		westWall = false;

		direction = NORTH;
		carLocX = 0;
		carLocY = 0;

		samples = new ArrayList<Sample>();
		samplesON = false;
	}

	public static void turnSamples(int turn) {
		for (int i = 0; i < samples.size(); i++) {
			samples.get(i).direction = (samples.get(i).direction + turn) % 4;
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
			for (int j = 0; j < cells.size(); j++) {
				if (cells.get(j).relativeX == samples.get(i).gridPosX
						&& cells.get(j).relativeY == samples.get(i).gridPosY) {
					num = j;
					break;
				}
			}

			if (num == -1)
				delete = true;
			else if (sampleDir == 0) {
				if (cells.get(num).southWall)
					delete = true;
			} else if (sampleDir == 1) {
				if (cells.get(num).westWall)
					delete = true;
			} else if (sampleDir == 2) {
				if (cells.get(num).northWall)
					delete = true;
			} else if (sampleDir == 3) {
				if (cells.get(num).eastWall)
					delete = true;
			}

			if (delete) {
				samples.remove(i);
				i--;
			}
		}
	}

	public static void eliminateSamples(int dir, boolean exist, int gridColor) {
		for (int i = 0; i < samples.size(); i++) {
			int posX = samples.get(i).gridPosX;
			int posY = samples.get(i).gridPosY;
			int sampleDir = samples.get(i).direction;
			boolean delete = false;

			int num = -1;
			for (int j = 0; j < cells.size(); j++) {
				if (cells.get(j).relativeX == posX && cells.get(j).relativeY == posY) {
					num = j;
					break;
				}
			}
			sampleDir = (sampleDir + dir) % 4;

			if (sampleDir == NORTH) {
				if (cells.get(num).northWall != exist) {
					delete = true;
				}
			} else if (sampleDir == EAST) {
				if (cells.get(num).eastWall != exist) {
					delete = true;
				}

			} else if (sampleDir == SOUTH) {
				if (cells.get(num).southWall != exist) {
					delete = true;
				}
			} else if (sampleDir == WEST) {
				if (cells.get(num).westWall != exist) {
					delete = true;
				}
			}

			if (gridColor != cells.get(num).color)
				delete = true;

			if (delete) {
				samples.remove(i);
				i--;
			}
		}
	}

	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;
		for (int i = 0; i < cells.size(); i++) {
			drawCell(cells.get(i), g2);
		}

		g2.setColor(Color.MAGENTA);
		g2.setStroke(new BasicStroke(0));

		if (!samplesON) {

			int x = 500 + 60 * carLocX;
			int y = 300 - 60 * carLocY;
			if (direction == NORTH)
				g2.fillPolygon(new int[] { x + 20, x + 30, x + 40, x + 30 },
						new int[] { y + 40, y + 20, y + 40, y + 30 }, 4);
			else if (direction == EAST)
				g2.fillPolygon(new int[] { x + 20, x + 30, x + 20, x + 40 },
						new int[] { y + 20, y + 30, y + 40, y + 30 }, 4);
			else if (direction == SOUTH)
				g2.fillPolygon(new int[] { x + 20, x + 30, x + 40, x + 30 },
						new int[] { y + 20, y + 30, y + 20, y + 40 }, 4);
			else if (direction == WEST)
				g2.fillPolygon(new int[] { x + 40, x + 30, x + 40, x + 20 },
						new int[] { y + 20, y + 30, y + 40, y + 30 }, 4);
		}
		if (samplesON) {
			for (int i = 0; i < samples.size(); i++) {
				int posX = samples.get(i).gridPosX;
				int posY = samples.get(i).gridPosY;
				int dir = samples.get(i).direction;

				int x = 500 + 60 * posX;
				int y = 300 - 60 * posY;
				if (dir == NORTH)
					g2.fillPolygon(new int[] { x + 25, x + 30, x + 35 }, new int[] { y + 10, y + 5, y + 10 }, 3);
				else if (dir == EAST)
					g2.fillPolygon(new int[] { x + 50, x + 55, x + 50 }, new int[] { y + 25, y + 30, y + 35 }, 3);
				else if (dir == SOUTH)
					g2.fillPolygon(new int[] { x + 25, x + 30, x + 35 }, new int[] { y + 50, y + 55, y + 50 }, 3);
				else if (dir == WEST)
					g2.fillPolygon(new int[] { x + 10, x + 5, x + 10 }, new int[] { y + 25, y + 30, y + 35 }, 3);

			}
		}

//		g2.setStroke(new BasicStroke(5));
//		g2.setColor(wallColor);
//		g2.drawLine(500, 300, 560, 300);
//
//		g2.setColor(Color.BLACK);
//		g2.setStroke(new BasicStroke(0));
//		g2.drawRect(500, 300, 60, 60);

//		Cell c = new Cell(lejos.robotics.Color.WHITE, 0, 0, true, false, false, false);
//		drawCell(c, g2);
	}

	public void drawCell(Cell cell, Graphics2D g2) {
		int x = 500 + 60 * cell.relativeX;
		int y = 300 - 60 * cell.relativeY;

		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(0));
		g2.drawRect(x, y, 60, 60);

		if (cell.color == lejos.robotics.Color.BLACK)
			g2.setColor(Color.BLACK);
		else if (cell.color == lejos.robotics.Color.RED)
			g2.setColor(Color.RED);
		else if (cell.color == lejos.robotics.Color.GREEN)
			g2.setColor(Color.GREEN);
		else if (cell.color == lejos.robotics.Color.BLUE)
			g2.setColor(Color.BLUE);
		else
			g2.setColor(Color.WHITE);

		g2.fillRect(x, y, 60, 60);

		g2.setStroke(new BasicStroke(5));
		g2.setColor(wallColor);
		if (cell.northWall)
			g2.drawLine(x, y, x + 60, y);

		if (cell.eastWall)
			g2.drawLine(x + 60, y, x + 60, y + 60);

		if (cell.southWall)
			g2.drawLine(x, y + 60, x + 60, y + 60);

		if (cell.westWall)
			g2.drawLine(x, y, x, y + 60);

	}

	public static void sampleRects() {
		for (int i = 0; i < cells.size(); i++) {
			for (int j = 0; j < 4; j++) {
				samples.add(new Sample(cells.get(i).relativeX, cells.get(i).relativeY, j));
			}
		}
	}
}
