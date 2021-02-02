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
public class HeatmapGenerator {
	/** Start of the program. */
	public static void main(String[] args) {
		HeatmapGenerator engine = new HeatmapGenerator();
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
	
	private int rowCount;
	private int dataCount;
	private int[] temperatures;
	
	/** Creates the engine. */
	public HeatmapGenerator() {
		this.frame = new JFrame("ME331 Heatmap Generator");
		this.canvas = new Canvas();
		final Dimension dimension = new Dimension(1280, 720);
		this.canvas.setMaximumSize(dimension);
		this.canvas.setMinimumSize(dimension);
		this.canvas.setPreferredSize(dimension);
		this.frame.add(this.canvas);
		this.frame.pack();
		this.frame.setResizable(false);
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				HeatmapGenerator.this.stop();
			}
		});
		this.frame.requestFocus();
		this.canvas.requestFocus();
		this.canvas.setBackground(Color.BLACK);
		this.canvas.createBufferStrategy(2);
		this.bufferStrategy = this.canvas.getBufferStrategy();
		this.graphics = (Graphics2D)this.bufferStrategy.getDrawGraphics();
		this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		Random random = new Random();
		rowCount = random.nextInt() % 10+5;
		dataCount = random.nextInt() % 15 + 10;
		temperatures = new int[rowCount*dataCount];
		for (int i = 0; i < temperatures.length; i++)
			temperatures[i] = random.nextInt();
	}
	
	/** Starts the engine. */
	public void start() {
		float previousTime = System.nanoTime();
		float secondsCounter = 0.0F;
		int ticks = 0;
		this.running = true;
		while (this.running) {
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
			this.bufferStrategy.show();
			this.graphics.clearRect(0, 0, 1280, 720);
		}
		System.exit(0);
	}
	
	/** Stops the engine. */
	public void stop() {
		this.running = false;
	}
	
	/** Updates the engine. */
	public void update() {
		
	}
}
