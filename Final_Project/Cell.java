package Final_Project;

public class Cell {
	Cell ancestor = null;
	int relativeX;
	int relativeY;
	int color;
	boolean visited;
	boolean eastWall;
	boolean westWall;
	boolean northWall;
	boolean southWall;

	public Cell() {
		relativeX = -1;
		relativeY = -1;
		color = 0;
		visited = false;
		eastWall = false;
		westWall = false;
		northWall = false;
		southWall = false;
	}

	public Cell( int color, int relativeX, int relativeY, boolean northWall, boolean eastWall,
		boolean southWall, boolean westWall) {
		this.relativeX = relativeX;
		this.relativeY = relativeY;
		this.color = color;
		this.eastWall = eastWall;
		this.westWall = westWall;
		this.northWall = northWall;
		this.southWall = southWall;
	}

	@Override
	public String toString() {
		return "Cell [relativeX=" + relativeX + ", relativeY=" + relativeY + ", color=" + color + ", visited=" + visited
				+ ", eastWall=" + eastWall + ", westWall=" + westWall + ", northWall=" + northWall + ", southWall="
				+ southWall + "]";
	}
	
	
	
}