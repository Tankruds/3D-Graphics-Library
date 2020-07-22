public class Cube {
	private Point[] points = new Point[36];
	private int[] colors = new int[12];
	
	public Cube(int RGB) { 
		colors[0] = 255 << 16;
		colors[1] = colors[0];
		colors[2] = 255 << 16 | 165 << 8;
		colors[3] = colors[2];
		colors[4] = 255 << 16 | 255 << 8 | 255;
		colors[5] = colors[4];
		colors[6] = 255 << 16 | 255 << 8;
		colors[7] = colors[6];
		colors[8] = 255 << 8;
		colors[9] = colors[8];
		colors[10] = 255;
		colors[11] = colors[10];
		
		 points[0] = new Point(-1, 1, -1, 1); // Red
		 points[1] = new Point(1, 1, -1, 1);
		 points[2] = new Point(-1, -1, -1, 1);
		 
		 points[3] = new Point(1, 1, -1, 1);
		 points[4] = new Point(-1, -1, -1, 1);
		 points[5] = new Point(1, -1, -1, 1);
		 
		 points[6] = new Point(-1, 1, 1, 1); // Orange
		 points[7] = new Point(1, 1, 1, 1);
		 points[8] = new Point(-1, -1, 1, 1);
		 
		 points[9] = new Point(1, 1, 1, 1);
		 points[10] = new Point(-1, -1, 1, 1);
		 points[11] = new Point(1, -1, 1, 1);
		 
		 points[12] = new Point(-1, -1, 1, 1); // White
		 points[13] = new Point(-1, -1, -1, 1);
		 points[14] = new Point(1, -1, 1, 1);
		 
		 points[15] = new Point(1, -1, 1, 1);
		 points[16] = new Point(-1, -1, -1, 1);
		 points[17] = new Point(1, -1, -1, 1);
		 
		 points[18] = new Point(-1, 1, 1, 1); // Yellow
		 points[19] = new Point(-1, 1, -1, 1);
		 points[20] = new Point(1, 1, 1, 1);
		 
		 points[21] = new Point(1, 1, 1, 1);
		 points[22] = new Point(-1, 1, -1, 1);
		 points[23] = new Point(1, 1, -1, 1);
		 
		 points[24] = new Point(1, 1, -1, 1); // Green
		 points[25] = new Point(1, 1, 1, 1);
		 points[26] = new Point(1, -1, -1, 1);
		 
		 points[27] = new Point(1, 1, 1, 1);
		 points[28] = new Point(1, -1, -1, 1);
		 points[29] = new Point(1, -1, 1, 1);
		 
		 points[30] = new Point(-1, 1, -1, 1); // Blue
		 points[31] = new Point(-1, 1, 1, 1);
		 points[32] = new Point(-1, -1, -1, 1);
		 
		 points[33] = new Point(-1, 1, 1, 1);
		 points[34] = new Point(-1, -1, -1, 1);
		 points[35] = new Point(-1, -1, 1, 1);
	}
	
	public Point[] getPoints() {
		return points;
	}
	
	public int[] getColors() {
		return colors;
	}
}