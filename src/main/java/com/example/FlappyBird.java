package com.example;

import com.example.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class FlappyBird extends Canvas implements ActionListener {

	private static final long serialVersionUID = 1L;


	public static FlappyBird flappyBirdGame;

	public final int WIDTH = 800;
	public final int HEIGHT = 800;
	public final Color BROWN = new Color(0xFF964B00);
	final Font DIALOG_50 = new Font(Font.DIALOG, Font.PLAIN, 50);
	final Font DIALOG_100 = new Font(Font.DIALOG, Font.PLAIN, 100);
	final String BIRD_IMAGE = "bird.png";
	final String BACKGROUND_IMAGE = "bg-3.jpg";
	public int ticks;
	public int yMotion;
	public int speed = 15;
	public int score;
	public int highScore;
	public boolean gameOver;
	public boolean started;
	public Renderer renderer;
	public Rectangle bird;
	public Image birdImage;
	public Random random;
	private final File gameDataFile;
	private final Properties gameProperties;
	public List<Rectangle> columns;
	Font DIALOG_25 = new Font(Font.DIALOG, Font.PLAIN, 25);
	String dataFolder = System.getProperty("user.home");
	String gameDataFolderName = dataFolder + "\\FlappyBird_Java";
	String gameDataFileName = gameDataFolderName + "\\game.properties";
	String startText = "Click to Start!";
	MouseAdapter mouseAdapter = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			jump();
		}
	};

	KeyAdapter keyAdapter = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				jump();
			}
		}
	};

	public FlappyBird() throws IOException {
		JFrame jFrame = new JFrame();
		Timer timer = new Timer(20, this);
		renderer = new Renderer();
		random = new Random();

		// window setup
		jFrame.setTitle("Flappy Bird");
		jFrame.setResizable(false);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setSize(WIDTH, HEIGHT);
		jFrame.setVisible(true);
		jFrame.add(renderer);
		jFrame.addMouseListener(mouseAdapter);
		jFrame.addKeyListener(keyAdapter);
		jFrame.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// items setup
		bird = new Rectangle(WIDTH / 2 - 20, HEIGHT / 2 - 20, 30, 30);

		columns = new ArrayList<>();
		addColumn(true);
		addColumn(true);
		addColumn(true);
		addColumn(true);

		// game data save
		System.out.println(dataFolder);
		System.out.println(gameDataFolderName);
		System.out.println(gameDataFileName);

		gameDataFile = new File(gameDataFileName);
		if (!gameDataFile.exists() && !gameDataFile.isDirectory()) {
			if (new File(gameDataFolderName).mkdirs() && gameDataFile.createNewFile()) {
				System.out.println("game file created");

			} else {
				throw new IOException("Unable to create file or folder");
			}
		}
		FileReader reader = new FileReader(gameDataFile);
		gameProperties = new Properties();
		gameProperties.load(reader);
		if (gameProperties.isEmpty()) {
			gameProperties.put("highScore", 0 + "");
			gameProperties.store(new FileWriter(gameDataFile), "Game Data for Flappy Bird Game");
		}
		highScore = Integer.parseInt(gameProperties.get("highScore").toString());

		timer.start();
	}

	public static void main(String[] args) throws IOException {
		flappyBirdGame = new FlappyBird();
	}



	private void writeGameData(int highScore) throws Exception {
		gameProperties.put("highScore", highScore + "");
		gameProperties.store(new FileWriter(gameDataFile), "Game Data for Flappy Bird Game");
	}

	public void addColumn(boolean start) {
		int space = 300;
		int width = 100;
		int height = 50 + random.nextInt(300);

		if (start) {
			columns.add(new Rectangle(WIDTH + width + columns.size() * 300, HEIGHT - height - 120, width, height));
			columns.add(new Rectangle(WIDTH + width + (columns.size() - 1) * 300, 0, width, HEIGHT - height - space));
		} else {
			columns.add(new Rectangle(columns.get(columns.size() - 1).x + 600, HEIGHT - height - 120, width, height));
			columns.add(new Rectangle(columns.get(columns.size() - 1).x, 0, width, HEIGHT - height - space));
		}
	}

	public void paintColumn(Graphics g, Rectangle column) {
		g.setColor(Color.GREEN.darker());
		if (column.y != 0) {
			g.fillRect(column.x, column.y + 30, column.width, column.height);
			g.setColor(Color.GREEN.darker().darker());
			g.fillRect(column.x, column.y + 30, column.width, 5);
			g.setColor(Color.GREEN.darker());
			g.fillRect(column.x - 5, column.y, column.width + 10, 30);
		} else {
			int baseHeight = column.height - 30;
			g.fillRect(column.x, column.y, column.width, baseHeight);
			g.setColor(Color.GREEN.darker().darker());
			g.fillRect(column.x, baseHeight - 5, column.width, 5);
			g.setColor(Color.GREEN.darker());
			g.fillRect(column.x - 5, column.y + baseHeight, column.width + 10, 30);
		}
	}

	/**
	 * @param e the event to be processed
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		ticks++;

		if (started) {

			columns.forEach(column -> column.x -= speed);

			if (ticks % 2 == 0 && yMotion < 15) {
				yMotion += 2;
			}

			for (int i = 0; i < columns.size(); i++) {
				Rectangle column = columns.get(i);
				if (column.x + column.width < 0) {
					columns.remove(column);
					// addColumn is adding 2 pipes so add only when both of them are removed
					if (column.y == 0) {
						addColumn(false);
					}
				}
			}

			bird.y += yMotion;

			columns.forEach(column -> {
				if (column.y == 0 && bird.x + bird.width / 2 > column.x + column.width / 2 - 10
						&& bird.x + bird.width / 2 < column.x + column.width / 2 + 10) {
					score++;
				}

				if (column.intersects(bird)) {
					gameOver = true;
					if (bird.x <= column.x) {
						bird.x = column.x - bird.width;
					} else if (column.y != 0) {
						bird.y = column.y - bird.height;
					} else if (bird.y < column.height) {
						bird.y = column.height;
					}
				}
			});

			if (bird.y > HEIGHT - 120 || bird.y < 0) {
				gameOver = true;

			}
			if (bird.y + yMotion >= HEIGHT - 120) {
				bird.y = HEIGHT - 120 - bird.height;
			}

		}
		highScore = Integer.max(score, highScore);
		if (gameOver) {
			score = 0;
			try {
				writeGameData(highScore);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		renderer.repaint();
	}


	public void repaint(Graphics g) {

		FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
		Toolkit t = Toolkit.getDefaultToolkit();

		// background
		g.setColor(Color.CYAN);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		Image bgImage = t.getImage(FileUtils.getResourceFile(this, BACKGROUND_IMAGE));
		g.drawImage(bgImage, 0, 0, this);

		// bottom
		g.setColor(BROWN);
		g.fillRect(0, HEIGHT - 120, WIDTH, 120);

		// bird
		if (started) {
			t = Toolkit.getDefaultToolkit();
			birdImage = t.getImage(FileUtils.getResourceFile(this, BIRD_IMAGE)).getScaledInstance(bird.width, bird.height, Image.SCALE_FAST);
			ImageIcon ii = new ImageIcon(birdImage);
			g.drawImage(ii.getImage(), bird.x, bird.y, this);
		}

		// pipe
		columns.forEach((col) -> paintColumn(g, col));
		Color textColor = Color.WHITE;

		if (ticks % 20 == 0) {
			textColor = new Color(0xFF48b8fa);
			DIALOG_25 = new Font(Font.DIALOG, Font.BOLD, 25);
		} else if (ticks % 3 == 0) {
			textColor = new Color(0xFFFFFFFF);
			DIALOG_25 = new Font(Font.DIALOG, Font.PLAIN, 25);
		}
		g.setColor(textColor);

		g.setFont(DIALOG_100);
		if (!started) {
			int textwidth = (int) (DIALOG_100.getStringBounds(startText, frc).getWidth());
			g.drawString(startText, (WIDTH - textwidth) / 2, HEIGHT / 2 - 25);
		}


		g.setColor(Color.WHITE);

		if (gameOver) {
			g.drawString("Game Over!!", 100, HEIGHT / 2 - 50);
			g.setFont(DIALOG_25);
			g.setColor(Color.RED);
			int textwidth = (int) (DIALOG_25.getStringBounds(startText, frc).getWidth());
			g.drawString(startText, (WIDTH - textwidth) / 2, HEIGHT / 2);
		}

		g.setFont(DIALOG_50);
		if (started && !gameOver) {
			g.drawString("Score : " + score, 20, 50);
			g.drawString("High Score : " + highScore, WIDTH - 400, 50);

		}


	}

	public void jump() {
		if (gameOver) {
			// items setup
			bird = new Rectangle(WIDTH / 2 - 20, HEIGHT / 2 - 20, 30, 30);

			columns.clear();
			addColumn(true);
			addColumn(true);
			addColumn(true);
			addColumn(true);

			gameOver = false;
		}

		if (!started) {
			started = true;
		} else {
			yMotion = Math.min(yMotion, 0);
			yMotion -= 10;
		}
	}
}
