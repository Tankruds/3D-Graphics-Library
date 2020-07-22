public class Sphere {
	private static Point[] points;
	private static int[] colors;
	
	private static int circleTriangle = 0;
	
	public Sphere(int depth, double radius) {
		points = new Point[2 * 3 * 4 * (int)(Math.pow(4, depth))];
		colors = new int[2 * 4 * (int)(Math.pow(4, depth))];
		calcSphere(depth, radius);
	}
	
	private static double calcDistance(double x, double y, double z) {
		return Math.sqrt((x * x) + (y * y) + (z * z));
	}
	
	private static void calcSphere(int depth, double radius) {
		Point POINT00 = new Point(1, 0, 0, 1);
		Point POINT01 = new Point(0, 1, 0, 1);
		Point POINT02 = new Point(0, 0, 1, 1);
		Point POINT10 = new Point(-1, 0, 0, 1);
		Point POINT11 = new Point(0, -1, 0, 1);
		Point POINT12 = new Point(0, 0, -1, 1);
		
		subdivideSphere(radius, POINT00, POINT01, POINT02, depth);
		subdivideSphere(radius, POINT00, POINT02, POINT11, depth);
		subdivideSphere(radius, POINT00, POINT11, POINT12, depth);
		subdivideSphere(radius, POINT00, POINT12, POINT01, depth);
		
		subdivideSphere(radius, POINT10, POINT01, POINT12, depth);
		subdivideSphere(radius, POINT10, POINT12, POINT11, depth);
		subdivideSphere(radius, POINT10, POINT11, POINT02, depth);
		subdivideSphere(radius, POINT10, POINT02, POINT01, depth);
	}
	
	public Point[] getPoints() {
		return points;
	}
	
	public int[] getColors() {
		return colors;
	}
	
	private static void normalize(double[] p) {
		double normalizer = 1 / (calcDistance(p[0], p[1], p[2]));
		for (int i = 0; i < 3; i++) p[i] *= normalizer;
	}

	private static void subdivideSphere(double radius, Point p1, Point p2, Point p3, int depth) {
		if (depth == 0) {
			points[(circleTriangle * 3) + 0] = new Point(p1.getX() * radius, p1.getY() * radius, p1.getZ() * radius, 1);
			points[(circleTriangle * 3) + 1] = new Point(p2.getX() * radius, p2.getY() * radius, p2.getZ() * radius, 1);
			points[(circleTriangle * 3) + 2] = new Point(p3.getX() * radius, p3.getY() * radius, p3.getZ() * radius, 1);
			colors[circleTriangle] = (int)(Math.random() * 255) << 16 | (int)(Math.random() * 255) << 8 | (int)(Math.random() * 255);
			circleTriangle++;
		} else {
			double[] betweenP12 = new double[3];
			double[] betweenP23 = new double[3];
			double[] betweenP31 = new double[3];
			
			betweenP12[0] = p1.getX() + p2.getX();
			betweenP12[1] = p1.getY() + p2.getY();
			betweenP12[2] = p1.getZ() + p2.getZ();
			
			betweenP23[0] = p2.getX() + p3.getX();
			betweenP23[1] = p2.getY() + p3.getY();
			betweenP23[2] = p2.getZ() + p3.getZ();
			
			betweenP31[0] = p3.getX() + p1.getX();
			betweenP31[1] = p3.getY() + p1.getY();
			betweenP31[2] = p3.getZ() + p1.getZ();
			
			normalize(betweenP12);
			normalize(betweenP23);
			normalize(betweenP31);
			
			Point newP12 = new Point(betweenP12[0], betweenP12[1], betweenP12[2], 1);
			Point newP23 = new Point(betweenP23[0], betweenP23[1], betweenP23[2], 1);
			Point newP31 = new Point(betweenP31[0], betweenP31[1], betweenP31[2], 1);
			
			subdivideSphere(radius, p1, newP12, newP31, depth - 1);
			subdivideSphere(radius, p2, newP23, newP12, depth - 1);
			subdivideSphere(radius, p3, newP31, newP23, depth - 1);
			subdivideSphere(radius, newP12, newP23, newP31, depth - 1);
		}
	}
}