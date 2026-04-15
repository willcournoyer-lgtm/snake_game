import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

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
        private final LinkedList<Point> snake = new LinkedList<>();
        private int dx = 1;
        private int dy = 0;
        private Point food;
        private int score = 0;
        private boolean gameOver = false;
        private Timer timer;

        public GamePanel() {
            setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
            setFocusable(true);
            initializeGame();
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

            timer = new Timer(150, e -> {
                if (!gameOver) {
                    moveSnake();
                    repaint();
                }
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
        }

        private void initializeSnake() {
            snake.clear();
            snake.add(new Point(8, 10));
            snake.add(new Point(9, 10));
            snake.add(new Point(10, 10));
        }

        private void spawnFood() {
            do {
                int x = (int) (Math.random() * GRID_SIZE);
                int y = (int) (Math.random() * GRID_SIZE);
                food = new Point(x, y);
            } while (snake.contains(food));
        }

        private void resetGame() {
            initializeGame();
            dx = 1;
            dy = 0;
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

        private void moveSnake() {
            Point head = snake.getLast();
            int nextX = head.x + dx;
            int nextY = head.y + dy;

            // Check wall collision
            if (nextX < 0 || nextX >= GRID_SIZE || nextY < 0 || nextY >= GRID_SIZE) {
                gameOver = true;
                timer.stop();
                return;
            }

            Point nextHead = new Point(nextX, nextY);

            // Check self collision
            if (snake.contains(nextHead)) {
                gameOver = true;
                timer.stop();
                return;
            }

            snake.addLast(nextHead);

            // Check food eating
            if (nextHead.equals(food)) {
                score++;
                spawnFood();
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

            g.setColor(Color.GREEN);
            for (Point segment : snake) {
                g.fillRect(segment.x * CELL_SIZE, segment.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }

            if (food != null) {
                g.setColor(Color.RED);
                g.fillRect(food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }

            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, 10, 20);

            if (gameOver) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.drawString("Game Over", getWidth() / 2 - 50, getHeight() / 2 - 10);
                g.drawString("Final Score: " + score, getWidth() / 2 - 60, getHeight() / 2 + 10);
                g.drawString("Press R to restart", getWidth() / 2 - 70, getHeight() / 2 + 30);
            }
        }
    }
}