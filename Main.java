import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {
	// Size of the actual window.
	private static final int FRAME_WIDTH = 240 << 2;
	private static final int FRAME_HEIGHT = 160 << 2;
	// Size of the image that we'll be displaying in the window.
	private static final int IMAGE_WIDTH = 240 << 0;
	private static final int IMAGE_HEIGHT = 160 << 0;
	// Constants to switch between view mode and model mode.
	private static final int MODEL = 1;
	private static final int VIEW = 2;
	private static int CURRENT_MODE = MODEL;
	
	// Initializing the frame and panel that we'll be displaying the image onto.
	private static JFrame frame = new JFrame("3D Graphics");
	private static JPanel panel = new JPanel();
	// Initializing the image that we'll be writing to via the pixels array.
	private static BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
	private static int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
	
	// screenBuffer is a page that we'll be writing our pixels to. After the final image has been made we'll move it over to pixels.
	private static int[] screenBuffer = new int[pixels.length];
	// depthBuffer is where I keep the z values of all of the pixels to hide the hidden primitives.
	private static double[] depthBuffer = new double[pixels.length];
	
	// model moves our primitives to the world space using the scale, translate, or rotate methods once we've switched to model mode.
	private static double[][] model = new double[4][4];
	// view moves our "camera" around the world space either using the scale, translate, or rotate methods once we've switched to view mode.
	private static double[][] view = new double[4][4];
	// projection changes our projection using either the ortho or frustum methods.
	private static double[][] projection = new double[4][4];
	// viewport changes our range of values (x, y, z) to be from (-1, -1, -1) - (1, 1, 1) to specific pixels using the viewPort method.
	private static double[][] viewport = new double[4][4];
	
	public static void main(String[] args) throws InterruptedException {
		//This section just sets up the screen, frame, and graphics.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		Graphics g = panel.getGraphics();
		
		// Initialize all of our matrices to the identity matrix.
		model = getIdentityMatrix();
		view = getIdentityMatrix();
		projection = getIdentityMatrix();
		viewport = getIdentityMatrix();
		
		// Set our projection so that we're not only limited to 1 unit in any direction.
		ortho(-100, 100, -100, 100, -100, 100);
		//frustum(-1, 1, -1, 1, .1, 100);
		
		// Set our viewport to use all of the pixels available to us in the image.
		viewPort(0, IMAGE_WIDTH, 0, IMAGE_HEIGHT);
		
		// Scale the y value to match our x value so that 1 unit in the x axis uses the same amount of pixels in the y axis.
		scale(1, (double)FRAME_WIDTH / FRAME_HEIGHT, 1);
		
		// Scaling to make the objects fit our projection.
		scale(8, 8, 8);
		scale(2, 2, 2);
		
		// Objects that have the points needed to render them.
		Cube cube = new Cube(RGB(255, 0, 0));
		Sphere sphere = new Sphere(2, 1.5);
		
		// angle is the current angle that we'll be using for our rotate methods, and theta is how much the angle will change each update.
		double angle = 0, theta = 1.5;
		
		// Some stuff to maintain approximately 60 updates per second.
		boolean running = true;
		double FPS = 60;
		double tempThing = 1 / 1000000.0;
		// This is the actual loop where we'll be running things.
		while (running) {
			// startTime is used to maintain our fps.
			long startTime = System.nanoTime();
			
			// Make sure to reset the depthBuffer between drawings so that we can accurately draw things.
			resetDepthBuffer();
			// Set's the screen to black before we draw anything.
			fillScreen(RGB(0, 0, 0));
			
			setMode(VIEW);
			rotate(angle, 0, 1, 0);
			setMode(MODEL);
			
			translate(-5, 0, 0);
			rotate(angle, 1, 0, 0);
			rotate(angle, 0, 1, 0);
			
			drawPolygon(sphere.getPoints(), sphere.getColors());
			
			rotate(-angle, 0, 1, 0);
			rotate(-angle, 1, 0, 0);
			translate(5, 0, 0);
			
			rotate(angle, 1, 0, 0);
			rotate(angle, 0, 0, 1);
			rotate(angle, 0, 1, 0);
			
			drawPolygon(cube.getPoints(), cube.getColors());
			
			rotate(-angle, 0, 1, 0);
			rotate(-angle, 0, 0, 1);
			rotate(-angle, 1, 0, 0);
			
			setMode(VIEW);
			rotate(-angle, 0, 1, 0);
			setMode(MODEL);
			
			// Render moves everything from screenBuffer to pixels to actually "draw" everything.
			render();
			
			// Stop's angle from overflowing by limiting it to be between 0 - 360.
			angle += theta;
			if (angle > 360) angle -= 360;
			
			// End of our loop to maintain our 60 fps and draw the image to our frame.
			long endTime = System.nanoTime();
			long programTime = (endTime - startTime);
			double sleepTime = ((1.0/FPS * 1000000000) - programTime) * tempThing;
			if (sleepTime > 0) {
				Thread.sleep((long)sleepTime);
			} else {
				System.out.println("Lagged");
			}
			g.drawImage(image, 0, 0, FRAME_WIDTH - 15, FRAME_HEIGHT - 40, null);
		}
	}
	
	// Takes the points in the points[] and separates them into triangles with the specified colors and draws each triangle.
	private static void drawPolygon(Point[] points, int[] colors) {
		int triangles = points.length / 3;
		for (int i = 0; i < triangles; i++) {
			drawTriangle(points[i * 3], points[i * 3 + 1], points[i * 3 + 2], colors[i]);
		}
	}
	
	// Takes the three points and the RGB to draw a triangle on the screen using them.
	private static void drawTriangle(Point one, Point two, Point three, int RGB) {
		// Take all of the points and turn them into 4x1 matrices for matrix multiplication.
		double[][] positionOne = {{one.getX()}, {one.getY()}, {one.getZ()}, {one.getW()}};
		double[][] positionTwo = {{two.getX()}, {two.getY()}, {two.getZ()}, {two.getW()}};
		double[][] positionThree = {{three.getX()}, {three.getY()}, {three.getZ()}, {three.getW()}};
		
		// Transfer the positions to the world space.
		positionOne = multiplyMatrices(model, positionOne);
		positionTwo = multiplyMatrices(model, positionTwo);
		positionThree = multiplyMatrices(model, positionThree);
		
		// Transforms the "camera" around the world space.
		positionOne = multiplyMatrices(view, positionOne);
		positionTwo = multiplyMatrices(view, positionTwo);
		positionThree = multiplyMatrices(view, positionThree);
		
		// Changes our projection.
		positionOne = multiplyMatrices(projection, positionOne);
		positionTwo = multiplyMatrices(projection, positionTwo);
		positionThree = multiplyMatrices(projection, positionThree);
		
		// Change our viewport to decide which area of the image we want to draw on.
		positionOne = multiplyMatrices(viewport, positionOne);
		positionTwo = multiplyMatrices(viewport, positionTwo);
		positionThree = multiplyMatrices(viewport, positionThree);
		
		// Draw the triangle using the final positions after transforming them with our matrices.
		fillTriangle((int)positionOne[0][0], (int)positionOne[1][0], positionOne[2][0], (int)positionTwo[0][0], (int)positionTwo[1][0], positionTwo[2][0], (int)positionThree[0][0], (int)positionThree[1][0], positionThree[2][0], RGB);
	}
	
	// Given the x, y, and z coords of two points on the screen, draw a line between them.
	private static void drawLine(int x0, int y0, double z0, int x1, int y1, double z1, int RGB) {
		// Get the difference in x, y, and z values between the two points.
		int run = x1 - x0;
		int rise = y1 - y0;
		double travel = z1 - z0;
		
		// Find the max difference between points to get an accurate dx, dy, and dz.
		double steps = Math.max(Math.abs(rise), Math.abs(run));
		
		// Calculate the dx, dy, and dz.
		double dx = run / steps;
		double dy = rise / steps;
		double dz = travel / steps;
		
		// Draws the line, and checks the z values vs the depthBuffer to check if each pixel should be drawn.
		int address;
		double x = x0, y = y0, z = z0;
		for (int i = 0; i <= steps; i++) {
			if (x >= 0 && x < IMAGE_WIDTH && y >= 0 && y < IMAGE_HEIGHT) {
				address = (int)(x + y * IMAGE_WIDTH);
				if (z < depthBuffer[address] && z <= 1 && z >= -1) {
					depthBuffer[address] = z;
					screenBuffer[address] = RGB;
				}
			}
			x += dx;
			y += dy;
			z += dz;
		}
	}
	
	// Helper method for fillTriangle, this does the cases where 2 vertices have the same y values at the bottom of the triangle and draws it.
	private static void fillBottomFlatTriangle(int x0, int y0, double z0, int x1, int y1, double z1, int x2, int y2, double z2, int RGB) {
		double xSlopeOne = (x1 - x0) / (double)(y1 - y0);
		double xSlopeTwo = (x2 - x0) / (double)(y2 - y0);
		double zSlopeOne = (z1 - z0) / (double)(y1 - y0);
		double zSlopeTwo = (z2 - z0) / (double)(y2 - y0);
		double xOne = x0;
		double xTwo = x0;
		double zOne = z0;
		double zTwo = z0;
		for (int scanlineY = y0; scanlineY <= y1; scanlineY++) {
			drawLine((int)xOne, scanlineY, zOne, (int)xTwo, scanlineY, zTwo, RGB);
			xOne += xSlopeOne;
			xTwo += xSlopeTwo;
			zOne += zSlopeOne;
			zTwo += zSlopeTwo;
		}
	}
	
	// Fills the screen with the specified color.
	private static void fillScreen(int rgb) {
		for (int i = 0; i < screenBuffer.length; i++) screenBuffer[i] = rgb;
	}

	// Helper method for fillTriangle, it handles the case where 2 vertices have the same y value and it's flat on the top.
	private static void fillTopFlatTriangle(int x0, int y0, double z0, int x1, int y1, double z1, int x2, int y2, double z2, int RGB) {
		double xSlopeOne = (x2 - x0) / (double)(y2 - y0);
		double xSlopeTwo = (x2 - x1) / (double)(y2 - y1);
		double zSlopeOne = (z2 - z0) / (double)(y2 - y0);
		double zSlopeTwo = (z2 - z1) / (double)(y2 - y0);
		double xOne = x2;
		double xTwo = x2;
		double zOne = z2;
		double zTwo = z2;
		for (int scanlineY = y2; scanlineY >= y0; scanlineY--) {
			drawLine((int)xOne, scanlineY, zOne, (int)xTwo, scanlineY, zTwo, RGB);
			xOne -= xSlopeOne;
			xTwo -= xSlopeTwo;
			zOne -= zSlopeOne;
			zTwo -= zSlopeTwo;
		}
	}
	
	// Draws a triangle onto our image for us using the x and y values of the pixels on the screen.
	private static void fillTriangle(int x0, int y0, double z0, int x1, int y1, double z1, int x2, int y2, double z2, int RGB) {
		Point swap; // Get the coordinates into points to more easily swap them.
		Point one = new Point(x0, y0, z0, 1);
		Point two = new Point(x1, y1, z1, 1);
		Point three = new Point(x2, y2, z2, 1);
		
		// Arrange the points by their y values from lowest to highest.
		if (two.getY() < one.getY()) {
			swap = one;
			one = two;
			two = swap;
		}
		if (three.getY() < one.getY()) {
			swap = one;
			one = three;
			three = swap;
		}
		if (three.getY() < two.getY()) {
			swap = two;
			two = three;
			three = swap;
		}
		
		// Continue to draw the triangle if it's a simple case.
		if (two.getY() == three.getY()) fillBottomFlatTriangle((int)one.getX(), (int)one.getY(), one.getZ(), (int)two.getX(), (int)two.getY(), two.getZ(), (int)three.getX(), (int)three.getY(), three.getZ(), RGB);
		else if (one.getY() == two.getY()) fillTopFlatTriangle((int)one.getX(), (int)one.getY(), one.getZ(), (int)two.getX(), (int)two.getY(), two.getZ(), (int)three.getX(), (int)three.getY(), three.getZ(), RGB);
		else {
			// If it's not a simple case we divide the triangle into 2 triangles that have a flat top and flat bottom by finding the x and z value where they split.
			int x = (int)Math.round(one.getX() + ((two.getY() - one.getY())) / (double)(three.getY() - one.getY()) * (three.getX() - one.getX()));
			double z = (one.getZ() + ((two.getY() - one.getY()) / (double)(three.getY() - one.getY()) * (three.getZ() - one.getZ())));
			fillBottomFlatTriangle((int)one.getX(), (int)one.getY(), one.getZ(), (int)two.getX(), (int)two.getY(), two.getZ(), x, (int)two.getY(), z, RGB);
			fillTopFlatTriangle((int)two.getX(), (int)two.getY(), two.getZ(), x, (int)two.getY(), z, (int)three.getX(), (int)three.getY(), three.getZ(), RGB);
		}
	}
	
	// Sets our projection to frustum perspective.
	private static void frustum(double l, double r, double b, double t, double n, double f) {
		double[][] frustum = new double[4][4];
		frustum[0][0] = (2 * n) / (r - l);
		frustum[0][2] = (r + l) / (r - l);
		frustum[1][1] = (2 * n) / (t - b);
		frustum[1][2] = (t + b) / (t - b);
		frustum[2][2] = -(f + n) / (f - n);
		frustum[2][3] = (-2 * f * n) / (f - n);
		frustum[3][2] = -1;
		multiplyMatrices(projection, frustum);
	}

	// Returns the identity matrix for a 4x4 matrix.
	private static double[][] getIdentityMatrix() {
		double[][] matrix = new double[4][4];
		matrix[0][0] = 1;
		matrix[1][1] = 1;
		matrix[2][2] = 1;
		matrix[3][3] = 1;
		return matrix;
	}
	
	// This allows us to switch between modes so our transformations affect different matrices.
	private static void setMode(int mode) {
		CURRENT_MODE = mode;
	}
	
	// Multiplies the two matrices together and returns the result.
	private static double[][] multiplyMatrices(double[][] matrixOne, double[][] matrixTwo) {
		double[][] finalMatrix = new double[matrixOne.length][matrixTwo[0].length];
		for (int i = 0; i < finalMatrix.length; i++)
			for (int j = 0; j < finalMatrix[i].length; j++)
				for (int k = 0; k < matrixOne[0].length; k++)
					finalMatrix[i][j] += matrixOne[i][k] * matrixTwo[k][j];
		return finalMatrix;
	}
	
	// Sets our projection to orthographic perspective.
	private static void ortho(double l, double r, double b, double t, double n, double f) {
		double[][] ortho = new double[4][4];
		ortho[0][0] = 2 / (r - l);
		ortho[0][3] = -(r + l) / (r - l);
		ortho[1][1] = 2 / (t - b);
		ortho[1][3] = -(t + b) / (t - b);
		ortho[2][2] = -2 / (f - n);
		ortho[2][3] = -(f + n) / (f - n);
		ortho[3][3] = 1;
		projection = multiplyMatrices(projection, ortho);
	}
	
	// Move our image from the buffer to the actual image.
	private static void render() {
		for (int i = 0; i < pixels.length; i++) pixels[i] = screenBuffer[i];
	}
	
	// Resets the depth buffer so we can draw the scene again.
	private static void resetDepthBuffer() {
		for (int i = 0; i < depthBuffer.length; i++) depthBuffer[i] = Double.POSITIVE_INFINITY;
	}
	
	// Returns an int in RGB format.
	private static int RGB(int r, int g, int b) {
		return r << 16 | g << 8 | b;
	}
	
	// Rotates the matrix by angle around the x, y, or z axis.
	private static void rotate(double angle, double x, double y, double z) {
		double[][] rotateMatrix = new double[4][4];
		
		angle *= Math.PI / 180;
		double s = Math.sin(angle), c = Math.cos(angle), t = 1 - c;
		double tx = t * x, ty = t * y, tz = t * z;
		double sx = s * x, sy = s * y, sz = s * z;
		
		rotateMatrix[0][0] = tx * x + c;
		rotateMatrix[0][1] = tx * y + sz;
		rotateMatrix[0][2] = tx * z - sy;
		rotateMatrix[1][0] = tx * y - sz;
		rotateMatrix[1][1] = ty * y + c;
		rotateMatrix[1][2] = ty * z + sx;
		rotateMatrix[2][0] = tx * z + sy;
		rotateMatrix[2][1] = ty * z - sx;
		rotateMatrix[2][2] = tz * z + c;
		rotateMatrix[3][3] = 1;
		
		if (CURRENT_MODE == MODEL) model = multiplyMatrices(model, rotateMatrix);
		else if (CURRENT_MODE == VIEW) view = multiplyMatrices(view, rotateMatrix);
	}
	
	// Scales our matrix by x, y, and z.
	private static void scale(double x, double y, double z) {
		double[][] scaleMatrix = new double[4][4];
		scaleMatrix[0][0] = x;
		scaleMatrix[1][1] = y;
		scaleMatrix[2][2] = z;
		scaleMatrix[3][3] = 1;
		
		if (CURRENT_MODE == MODEL) model = multiplyMatrices(model, scaleMatrix);
		else if (CURRENT_MODE == VIEW) view = multiplyMatrices(view, scaleMatrix);
	}
	
	// Translates our matrix by x, y, and z.
	private static void translate(double x, double y, double z) {
		double[][] translateMatrix = getIdentityMatrix();
		translateMatrix[0][3] = x;
		translateMatrix[1][3] = y;
		translateMatrix[2][3] = z;
		
		if (CURRENT_MODE == MODEL) model = multiplyMatrices(model, translateMatrix);
		else if (CURRENT_MODE == VIEW) view = multiplyMatrices(view, translateMatrix);
	}
	
	// Changes our viewport so we can determine where on the screen we'll actually be drawing.
	private static void viewPort(int l, int r, int t, int b) {
		double[][] viewportMatrix = getIdentityMatrix();
		viewportMatrix[0][0] = (r - l) / 2.0;
		viewportMatrix[0][3] = (r + l) / 2.0;
		viewportMatrix[1][1] = (t - b) / 2.0;
		viewportMatrix[1][3] = (t + b) / 2.0;
		viewportMatrix[2][2] = .5;
		viewportMatrix[2][3] = .5;
		viewport = multiplyMatrices(viewport, viewportMatrix);
	}
}