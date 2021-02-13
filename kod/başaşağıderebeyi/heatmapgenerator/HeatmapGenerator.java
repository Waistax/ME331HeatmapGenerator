/**
 * başaşağıderebeyi.heatmapgenerator.HeatmapGenerator.java
 * 0.3 / 2 Şub 2021 / 19:27:44
 * Cem GEÇGEL (BaşAşağıDerebeyi)
 */
package başaşağıderebeyi.heatmapgenerator;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

/** Generates a heatmap from a file. */
public class HeatmapGenerator implements KeyListener, MouseListener, MouseWheelListener, MouseMotionListener {
	private static final int[] midColor = new int[] { 0, 100, 0 };
	private static final int[] minColor = new int[] { 0, 0, 0 };
	private static final int[] maxColor = new int[] { 255, 0, 0 };
	
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
	
	private int rowCount;
	private int dataCount;
	private float[] temperatures;
	private final Color[] colors;
	private float xPos = 10.0F;
	private float yPos = 10.0F;
	private float scale = 15.0F;
	private float ratio;
	private boolean dragged;
	private int cursorX;
	private int cursorY;
	
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
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		bufferStrategy = canvas.getBufferStrategy();
		graphics = (Graphics2D)bufferStrategy.getDrawGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		readData();
		colors = new Color[temperatures.length];
		generateColors();
	}
	
	/** Starts the engine. */
	private void start() {
		float previousTime = System.nanoTime();
		float secondsCounter = 0.0F;
		int ticks = 0;
		running = true;
		while (running) {
			float elapsedTime = System.nanoTime() - previousTime;
			previousTime += elapsedTime;
			elapsedTime /= 1000000000.0F;
			if ((secondsCounter += elapsedTime) >= 1.0F) {
				System.out.println("Tick Rate: " + ticks);
				ticks = 0;
				secondsCounter--;
			}
			try {
				Thread.sleep(1);
			} catch (final InterruptedException e1) {
				e1.printStackTrace();
			}
			ticks++;
			update();
			bufferStrategy.show();
			graphics.clearRect(0, 0, 1280, 720);
		}
		System.exit(0);
	}
	
	/** Stops the engine. */
	private void stop() {
		running = false;
	}
	
	/** Updates the engine. */
	private void update() {
		final int x = Math.round(xPos);
		final int y = Math.round(yPos);
		float pixelScale = (float)Math.pow(1.2, scale);
		final int sx = Math.round(pixelScale);
		final int sy = Math.round(pixelScale * ratio);
		graphics.setFont(new Font("Consolas", Font.PLAIN, sx / 8));
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < dataCount; j++) {
				final int index = j + i * dataCount;
				final int xpos = x + i * sx;
				final int ypos = y + (dataCount-j-1) * sy;
				graphics.setColor(colors[index]);
				graphics.fillRect(xpos, ypos, sx, sy);
				graphics.setColor(Color.WHITE);
				graphics.drawString(""+temperatures[index], xpos + 1, ypos + sy - 1);
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
	
	private void generateColors() {
		float total = 0.0F;
		float min = 0.0F;
		float max = 0.0F;
		for (final float temp : temperatures) {
			total += temp;
			min = Math.min(temp, min);
			max = Math.max(temp, max);
		}
		final float mid = total / temperatures.length;
		float l = max - min;
		final int[] rgb = new int[3];
		final float upper = max - mid;
		final float lower = mid - min;
		for (int i = 0; i < temperatures.length; i++) {
			final float temp = temperatures[i];
			final float avg = (temp-min) /l;
			final float inv = 1 - avg;
			for (int j = 0; j < 3; j++) {
				rgb[j] = (int)Math.max(Math.min(
						Math.round(HeatmapGenerator.minColor[j] * inv + HeatmapGenerator.maxColor[j] * avg), 255),
						0);
			}
			colors[i] = new Color(rgb[0], rgb[1], rgb[2]);
		}
	}
	
	private void readData() {
		List<String> lines = new ArrayList<>();
		final File file = new File("output.txt");
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		float rowLength = Float.parseFloat(lines.get(0).substring("Row Length: ".length()));
		float stepSize = Float.parseFloat(lines.get(1).substring("Step Size: ".length()));
		float rowWidth = Float.parseFloat(lines.get(2).substring("Row Width: ".length()));
		rowCount = Integer.parseInt(lines.get(3).substring("Row Count: ".length()));
		dataCount = (int)Math.floor(rowLength / stepSize);
		ratio = stepSize / rowWidth;
		System.out.println("Row Length: " + rowLength);
		System.out.println("Step Size: " + stepSize);
		System.out.println("Data Count: " + dataCount);
		System.out.println("Row Width: " + rowWidth);
		temperatures = new float[rowCount*dataCount];
		for (int i = 0; i < temperatures.length; i++) {
			temperatures[i] = Float.parseFloat(lines.get(5+i).substring("Row: 0 | Position: 0.00 | Temperature: ".length()));
		}
	}
	
	@Override
	public void keyTyped(final KeyEvent e) {
	}
	
	@Override
	public void keyPressed(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_Y:
			final Random random = new Random();
			for (int i = 0; i < temperatures.length; i++) {
				temperatures[i] = random.nextFloat();
			}
			generateColors();
			return;
		}
	}
	
	@Override
	public void keyReleased(final KeyEvent e) {
	}
	
	@Override
	public void mouseClicked(final MouseEvent e) {
	}
	
	@Override
	public void mousePressed(final MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON2:
			dragged = true;
			return;
		}
	}
	
	@Override
	public void mouseReleased(final MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON2:
			dragged = false;
			return;
		}
	}
	
	@Override
	public void mouseEntered(final MouseEvent e) {
	}
	
	@Override
	public void mouseExited(final MouseEvent e) {
	}
	
	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		scale += (float)e.getPreciseWheelRotation();
	}
	
	@Override
	public void mouseDragged(final MouseEvent e) {
		if (dragged) {
			xPos += e.getX() - cursorX;
			yPos += e.getY() - cursorY;
		}
		cursorX = e.getX();
		cursorY = e.getY();
	}
	
	@Override
	public void mouseMoved(final MouseEvent e) {
		cursorX = e.getX();
		cursorY = e.getY();
	}
}
