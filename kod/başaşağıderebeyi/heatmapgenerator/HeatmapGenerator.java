/**
 * başaşağıderebeyi.heatmapgenerator.HeatmapGenerator.java
 * 0.5 / 2 Şub 2021 / 19:27:44
 * Cem GEÇGEL (BaşAşağıDerebeyi)
 */
package başaşağıderebeyi.heatmapgenerator;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

/** Generates a heatmap from a file. */
public class HeatmapGenerator implements MouseWheelListener {
	/** Start of the program. */
	public static void main(final String[] args) {
		try {
			final HeatmapGenerator engine = new HeatmapGenerator();
			engine.start();
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	/** The frame on the screen. */
	private final JFrame frame;
	/** The AWT object that is gettin drawn on. */
	private final Canvas canvas;
	/** The flag that is true if the engine is running. */
	private boolean running;
	/** The screen buffers to draw onto. */
	private final BufferStrategy bufferStrategy;
	/** The drawing tool. */
	private final Graphics2D graphics;
	/** The number of waterways in the field. */
	private int rowCount;
	/** The number of measurements for each waterway. */
	private int dataCount;
	/** The temperature data. */
	private float[] temperatures;
	/** The color for each temperature. */
	private final Color[] colors;
	/** The color for the text temperature value for each temperature. */
	private final Color[] textColors;
	/** The scale to draw the tiles. */
	private float scale = 26.0F;
	/** The height to width ratio of the tiles. */
	private float ratio;
	
	/** Creates the engine. */
	private HeatmapGenerator() {
		frame = new JFrame("ME331 Heatmap Generator");
		canvas = new Canvas();
		final Dimension dimension = new Dimension(1280, 720);
		canvas.setMaximumSize(dimension);
		canvas.setMinimumSize(dimension);
		canvas.setPreferredSize(dimension);
		frame.add(canvas);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				HeatmapGenerator.this.stop();
			}
		});
		frame.requestFocus();
		canvas.requestFocus();
		canvas.setBackground(Color.BLACK);
		canvas.createBufferStrategy(2);
		canvas.addMouseWheelListener(this);
		bufferStrategy = canvas.getBufferStrategy();
		graphics = (Graphics2D)bufferStrategy.getDrawGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		readData();
		colors = new Color[temperatures.length];
		textColors = new Color[temperatures.length];
		generateColors();
	}
	
	/** Starts the engine. */
	private void start() {
		float previousTime = System.nanoTime();
		float secondsCounter = 0.0F;
		running = true;
		while (running) {
			float elapsedTime = System.nanoTime() - previousTime;
			previousTime += elapsedTime;
			elapsedTime /= 1000000000.0F;
			if ((secondsCounter += elapsedTime) >= 1.0F) {
				secondsCounter--;
			}
			try {
				Thread.sleep(1);
			} catch (final InterruptedException e1) {
				e1.printStackTrace();
			}
			update();
			bufferStrategy.show();
			graphics.clearRect(0, 0, 1280, 720);
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	
	/** Stops the engine. */
	private void stop() {
		running = false;
	}
	
	/** Updates the engine. */
	private void update() {
		final float pixelScale = (float)Math.pow(1.2, scale);
		final int sx = Math.round(pixelScale);
		final int sy = Math.round(pixelScale * ratio);
		final int x = (1280 - sx * rowCount) / 2;
		final int y = (720 - sy * dataCount) / 2;
		graphics.setFont(new Font("Consolas", Font.PLAIN, sx / 8));
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < dataCount; j++) {
				final int index = j + i * dataCount;
				final int xpos = x + i * sx;
				final int ypos = y + (dataCount - j - 1) * sy;
				graphics.setColor(colors[index]);
				graphics.fillRect(xpos, ypos, sx, sy);
				graphics.setColor(textColors[index]);
				graphics.drawString("" + temperatures[index], xpos + 3, ypos + sy - 3);
			}
		}
		graphics.setColor(Color.WHITE);
		for (int i = 0; i <= rowCount; i++) {
			graphics.drawLine(x + i * sx, y, x + i * sx, y + dataCount * sy);
		}
		for (int j = 0; j <= dataCount; j++) {
			graphics.drawLine(x, y + j * sy, x + rowCount * sx, y + j * sy);
		}
	}
	
	/** Generates the colors from the temperature data. */
	private void generateColors() {
		final float[] tempRanges = new float[] { 15.0F, 25.0F, 30.0F, 40.0F };
		final ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
		final float[][] colorRanges = new float[][] { colorSpace.fromRGB(new float[] { 0.0F, 0.0F, 0.0F }),
				colorSpace.fromRGB(new float[] { 0.0F, 0.0F, 1.0F }),
				colorSpace.fromRGB(new float[] { 1.0F, 1.0F, 0.0F }),
				colorSpace.fromRGB(new float[] { 1.0F, 0.1F, 0.0F }),
				colorSpace.fromRGB(new float[] { 1.0F, 0.1F, 0.0F }),
				colorSpace.fromRGB(new float[] { 1.0F, 0.0F, 0.0F }) };
		final float[] interpolated = new float[colorSpace.getNumComponents()];
		for (int i = 0; i < temperatures.length; i++) {
			int lowerRange = 0;
			for (int j = 0; j < tempRanges.length - 1; j++) {
				if (temperatures[i] > tempRanges[j] && tempRanges[j] > tempRanges[lowerRange]) {
					lowerRange = j;
				}
			}
			final float d = Math.max(Math.min(
					(temperatures[i] - tempRanges[lowerRange]) / (tempRanges[lowerRange + 1] - tempRanges[lowerRange]),
					1.0F), 0.0F);
			final float inv = 1.0F - d;
			for (int j = 0; j < interpolated.length; j++) {
				interpolated[j] = colorRanges[lowerRange * 2][j] * inv + colorRanges[lowerRange * 2 + 1][j] * d;
			}
			colors[i] = new Color(colorSpace, interpolated, 1.0F);
			textColors[i] = colors[i].getRed() + colors[i].getGreen() + colors[i].getBlue() >= 300 ? Color.BLACK
					: Color.WHITE;
		}
	}
	
	/** Reads the data in the output.txt file. */
	private void readData() {
		final List<String> lines = new ArrayList<>();
		final File file = new File("output.txt");
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		final float rowLength = Float.parseFloat(lines.get(0).substring("Row Length: ".length()));
		final float stepSize = Float.parseFloat(lines.get(1).substring("Step Size: ".length()));
		final float rowWidth = Float.parseFloat(lines.get(2).substring("Row Width: ".length()));
		rowCount = Integer.parseInt(lines.get(3).substring("Row Count: ".length()));
		dataCount = (int)Math.floor(rowLength / stepSize);
		ratio = stepSize / rowWidth;
		System.out.println("Row Length: " + rowLength);
		System.out.println("Step Size: " + stepSize);
		System.out.println("Data Count: " + dataCount);
		System.out.println("Row Width: " + rowWidth);
		temperatures = new float[rowCount * dataCount];
		for (int i = 0; i < temperatures.length; i++) {
			int x = i / dataCount;
			int y = i % dataCount;
			int realX = x;
			int realY = x % 2 == 1 ? dataCount-1-y : y;
			temperatures[realX * dataCount + realY] = Float
					.parseFloat(lines.get(5 + i).substring("Row: 0 | Position: 0.00 | Temperature: ".length()));
		}
	}
	
	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		scale += (float)e.getPreciseWheelRotation();
	}
}
