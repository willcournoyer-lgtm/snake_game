import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

class Segment {
    int x, y;
    Color color;

    Segment(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }
}

public class SnakeGame {
    private static final int CELL_SIZE = 30;
    private static final int GRID_SIZE = 20;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new GamePanel());
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static class GamePanel extends JPanel {
        private final LinkedList<Segment> snake = new LinkedList<>();
        private int dx = 1;
        private int dy = 0;
        private Point food;
        private int score = 0;
        private boolean gameOver = false;
        private Timer timer;
        private int currentDelay = 150;
        private BufferedImage gameOverImage;
        private long gameOverTime = -1;
        private static final int GAME_OVER_IMAGE_DURATION = 3000;

        private boolean growSnake = false;
        private final Random rand = new Random();

        public GamePanel() {
            setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
            setFocusable(true);
            initializeGame();
            gameOverImage = createGameOverImage();

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (gameOver) {
                        if (e.getKeyCode() == KeyEvent.VK_R) {
                            resetGame();
                        }
                        return;
                    }
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT -> setDirection(-1, 0);
                        case KeyEvent.VK_RIGHT -> setDirection(1, 0);
                        case KeyEvent.VK_UP -> setDirection(0, -1);
                        case KeyEvent.VK_DOWN -> setDirection(0, 1);
                    }
                }
            });

            timer = new Timer(currentDelay, e -> {
                if (!gameOver) {
                    moveSnake();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        public void addNotify() {
            super.addNotify();
            requestFocusInWindow();
        }

        private void initializeGame() {
            initializeSnake();
            spawnFood();
            score = 0;
            gameOver = false;
            gameOverTime = -1;
            growSnake = false;
        }

        private Color randomColor() {
            return new Color(
                rand.nextInt(206) + 50,
                rand.nextInt(206) + 50,
                rand.nextInt(206) + 50
            );
        }

        private void initializeSnake() {
            snake.clear();

            // Start with green snake
            snake.add(new Segment(8, 10, Color.GREEN));
            snake.add(new Segment(9, 10, Color.GREEN));
            snake.add(new Segment(10, 10, Color.GREEN));
        }

        private void spawnFood() {
            boolean valid;
            do {
                valid = true;
                int x = (int) (Math.random() * GRID_SIZE);
                int y = (int) (Math.random() * GRID_SIZE);

                for (Segment s : snake) {
                    if (s.x == x && s.y == y) {
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    food = new Point(x, y);
                }
            } while (!valid);
        }

        private void resetGame() {
            initializeGame();
            dx = 1;
            dy = 0;
            currentDelay = 150;
            timer.setDelay(currentDelay);
            timer.start();
            repaint();
        }

        private void setDirection(int newDx, int newDy) {
            if (dx + newDx == 0 && dy + newDy == 0) {
                return;
            }
            dx = newDx;
            dy = newDy;
        }

        private void triggerGameOver() {
            gameOver = true;
            gameOverTime = System.currentTimeMillis();
            playGameOverSound();
        }

        private BufferedImage createGameOverImage() {
            int width = 240;
            int height = 140;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(0, 0, 0, 220));
            g.fillRoundRect(0, 0, width, height, 30, 30);
            g.setColor(Color.RED);
            g.fillOval(30, 20, 80, 80);
            g.setColor(Color.WHITE);
            g.fillOval(45, 35, 15, 15);
            g.fillOval(80, 35, 15, 15);
            g.setColor(Color.BLACK);
            g.fillOval(55, 65, 50, 15);
            g.setColor(Color.WHITE);
            g.drawString("Game Over", 130, 50);
            g.drawString("Try Again", 130, 80);
            g.dispose();
            return image;
        }

        private void playGameOverSound() {
            new Thread(() -> {
                try {
                    float sampleRate = 44100f;
                    int durationMs = 300;
                    int numSamples = (int) (durationMs * sampleRate / 1000);
                    byte[] buffer = new byte[numSamples];
                    double frequency = 440;
                    for (int i = 0; i < buffer.length; i++) {
                        double angle = 2.0 * Math.PI * i * frequency / sampleRate;
                        buffer[i] = (byte) (Math.sin(angle) * 127);
                    }
                    AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                    try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                        line.open(format);
                        line.start();
                        line.write(buffer, 0, buffer.length);
                        line.drain();
                    }
                } catch (Exception ignored) {}
            }).start();
        }

        private void moveSnake() {
            Segment head = snake.getLast();
            int nextX = head.x + dx;
            int nextY = head.y + dy;

            // Wall collision
            if (nextX < 0 || nextX >= GRID_SIZE || nextY < 0 || nextY >= GRID_SIZE) {
                triggerGameOver();
                return;
            }

            // Self collision
            for (Segment s : snake) {
                if (s.x == nextX && s.y == nextY) {
                    triggerGameOver();
                    return;
                }
            }

            // Add new head
            snake.addLast(new Segment(nextX, nextY, head.color));

            // Food eaten
            if (nextX == food.x && nextY == food.y) {
                score++;
                growSnake = true;

                if (score % 5 == 0) {
                    currentDelay = Math.max(50, currentDelay - 10);
                    timer.setDelay(currentDelay);
                }

                spawnFood();
            }

            // Growth logic
            if (growSnake) {
                Segment tail = snake.getFirst();
                snake.addFirst(new Segment(tail.x, tail.y, randomColor()));
                growSnake = false;
            } else {
                snake.removeFirst();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(64, 64, 64));
            for (int x = 0; x <= GRID_SIZE; x++) {
                g.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, GRID_SIZE * CELL_SIZE);
            }
            for (int y = 0; y <= GRID_SIZE; y++) {
                g.drawLine(0, y * CELL_SIZE, GRID_SIZE * CELL_SIZE, y * CELL_SIZE);
            }

            // Draw snake (each segment has its own color)
            for (Segment segment : snake) {
                g.setColor(segment.color);
                g.fillRect(segment.x * CELL_SIZE, segment.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }

            // Draw food
            if (food != null) {
                g.setColor(Color.RED);
                g.fillRect(food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }

            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, 10, 20);

            if (gameOver) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                long elapsed = System.currentTimeMillis() - gameOverTime;
                if (gameOverImage != null && elapsed >= 0 && elapsed <= GAME_OVER_IMAGE_DURATION) {
                    int imageX = (getWidth() - gameOverImage.getWidth()) / 2;
                    int imageY = (getHeight() - gameOverImage.getHeight()) / 2 - 40;
                    g.drawImage(gameOverImage, imageX, imageY, null);
                }

                g.setColor(Color.WHITE);
                g.drawString("Game Over", getWidth() / 2 - 50, getHeight() / 2 + 50);
                g.drawString("Final Score: " + score, getWidth() / 2 - 60, getHeight() / 2 + 70);
                g.drawString("Press R to restart", getWidth() / 2 - 70, getHeight() / 2 + 90);
            }
        }
    }
}