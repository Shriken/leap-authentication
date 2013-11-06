import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Canvas;
import java.awt.Color;

public class TestComponent extends Canvas {
	static final double AUTH_THRESHOLD = 40;
	int width, height;

	public TestComponent(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	public void displayString(String s, Color color) {
		Graphics g = getGraphics();

		g.setColor(color);
		g.fillRect(0, 0, width, height);

		int fontSize = 200;
		g.setColor(Color.BLACK);
		g.drawString(s, 0, fontSize);
	}

	public void displayScore(double score) {
		Graphics g = getGraphics();

		g.setColor(Color.GREEN);
		g.fillRect(0, 0, width, height);

		int fontSize = 20;
		g.setColor(Color.BLACK);
		g.drawString(score < AUTH_THRESHOLD ? "Success!" : "Failure",
					 0, fontSize);
		g.drawString("" + score, 0, 3*fontSize);
	}
}
