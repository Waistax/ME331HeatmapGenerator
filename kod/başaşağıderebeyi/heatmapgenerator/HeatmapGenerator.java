/**
 * başaşağıderebeyi.heatmapgenerator.HeatmapGenerator.java
 * 0.1 / 2 Şub 2021 / 19:27:44
 * Cem GEÇGEL (BaşAşağıDerebeyi)
 */
package başaşağıderebeyi.heatmapgenerator;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

/** Generates a heatmap from a file. */
public class HeatmapGenerator implements KeyListener, MouseListener, MouseWheelListener, MouseMotionListener {
	private static final int[] midColor = new int[] { 255, 255, 0 };
	private static final int[] minColor = new int[] { 0, 0, 200 };
	private static final int[] maxColor = new int[] { 200, 0, 0 };
	
	/** Start of the program. */
	public static void main(final String[] args) {
		final HeatmapGenerator engine = new HeatmapGenerator();
		engine.start();
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
	
	private final int rowCount;
	private final int dataCount;
	private final float[] temperatures;
	private final Color[] colors;
	private float xPos = 10.0F;
	private float yPos = 10.0F;
	private float scale = 30.0F;
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
		final Random random = new Random();
		rowCount = 10;
		dataCount = 10;
		temperatures = new float[rowCount * dataCount];
		for (int i = 0; i < temperatures.length; i++) {
			temperatures[i] = random.nextFloat();
		}
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
		final int s = Math.round(scale);
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < dataCount; j++) {
				final int index = j + i * dataCount;
				graphics.setColor(colors[index]);
				graphics.fillRect(x + i * s, y + j * s, s, s);
			}
		}
		graphics.setColor(Color.WHITE);
		for (int i = 0; i <= rowCount; i++) {
			graphics.drawLine(x + i * s, y, x + i * s, y + dataCount * s);
		}
		for (int j = 0; j <= dataCount; j++) {
			graphics.drawLine(x, y + j * s, x + rowCount * s, y + j * s);
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
		final int[] rgb = new int[3];
		final float upper = max - mid;
		final float lower = mid - min;
		for (int i = 0; i < temperatures.length; i++) {
			final float temp = temperatures[i];
			final float d = temp - mid;
			if (d < 0.0F) {
				final float avg = (float)Math.sqrt(-d / lower);
				final float inv = 1 - avg;
				for (int j = 0; j < 3; j++) {
					rgb[j] = Math.max(Math.min(
							Math.round(HeatmapGenerator.midColor[j] * avg + HeatmapGenerator.minColor[j] * inv), 255),
							0);
				}
			} else {
				final float avg = (float)Math.sqrt(d / upper);
				final float inv = 1 - avg;
				for (int j = 0; j < 3; j++) {
					rgb[j] = Math.max(Math.min(
							Math.round(HeatmapGenerator.midColor[j] * avg + HeatmapGenerator.maxColor[j] * inv), 255),
							0);
				}
			}
			colors[i] = new Color(rgb[0], rgb[1], rgb[2]);
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
