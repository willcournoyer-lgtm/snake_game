import java.awt.*;
import javax.swing.*;

public class SnakeStarter extends JPanel {
    private final int gridSize = 20;
    private final int cellSize = 30;
    private final Point[] snake = {
        new Point(10, 10),
        new Point(9, 10),
        new Point(8, 10)
    };

    public SnakeStarter() {
        setPreferredSize(new Dimension(gridSize * cellSize, gridSize * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(22, 28, 39));
        g.fillRect(0, 0, gridSize * cellSize, gridSize * cellSize);

        g.setColor(Color.GREEN);
        for (Point segment : snake) {
            g.fillRect(segment.x * cellSize, segment.y * cellSize, cellSize, cellSize);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake Starter");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new SnakeStarter());
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}