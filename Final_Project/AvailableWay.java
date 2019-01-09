package Final_Project;

public class AvailableWay {
	int currentX;
	int currentY;
	int nextX;
	int nextY;
	int direction;
	
	public AvailableWay(int x, int y, int nx, int ny, int dir) {
		this.currentX = x;
		this.currentY = y;
		this.nextX = nx;
		this.nextY = ny;
		this.direction = dir;
	}
	
}
