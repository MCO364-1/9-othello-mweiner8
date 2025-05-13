import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class OthelloGUI extends JFrame {
    private static final int CELL_SIZE = 60;
    private static final Color BOARD_COLOR = new Color(0, 100, 0);
    private static final Color GRID_COLOR = Color.BLACK;

    private final OthelloModel model;
    private JPanel boardPanel;
    private JLabel statusLabel;
    private boolean isComputerThinking = false;

    public OthelloGUI() {
        model = new OthelloModel();
        setupGUI();
    }

    private void setupGUI() {
        setTitle("Othello Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the main game board panel
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }
        };
        boardPanel.setPreferredSize(new Dimension(CELL_SIZE * OthelloModel.BOARD_SIZE,
                CELL_SIZE * OthelloModel.BOARD_SIZE));
        boardPanel.setBackground(BOARD_COLOR);

        // Add mouse listener for handling moves
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isComputerThinking && model.getCurrentPlayer() == OthelloModel.BLACK) {
                    int row = e.getY() / CELL_SIZE;
                    int col = e.getX() / CELL_SIZE;
                    handlePlayerMove(row, col);
                }
            }
        });

        // Create status label
        statusLabel = new JLabel("Black's turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Layout
        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void drawBoard(Graphics g) {
        // Draw grid lines
        g.setColor(GRID_COLOR);
        for (int i = 0; i <= OthelloModel.BOARD_SIZE; i++) {
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, OthelloModel.BOARD_SIZE * CELL_SIZE);
            g.drawLine(0, i * CELL_SIZE, OthelloModel.BOARD_SIZE * CELL_SIZE, i * CELL_SIZE);
        }

        // Draw pieces
        int[][] board = model.getBoard();
        for (int row = 0; row < OthelloModel.BOARD_SIZE; row++) {
            for (int col = 0; col < OthelloModel.BOARD_SIZE; col++) {
                if (board[row][col] != OthelloModel.EMPTY) {
                    drawPiece(g, row, col, board[row][col]);
                }

                // Show valid moves for human player
                if (model.getCurrentPlayer() == OthelloModel.BLACK &&
                        model.isValidMove(row, col)) {
                    drawValidMove(g, row, col);
                }
            }
        }
    }

    private void drawPiece(Graphics g, int row, int col, int player) {
        int x = col * CELL_SIZE + CELL_SIZE/10;
        int y = row * CELL_SIZE + CELL_SIZE/10;
        int diameter = CELL_SIZE - CELL_SIZE/5;

        g.setColor(player == OthelloModel.BLACK ? Color.BLACK : Color.WHITE);
        g.fillOval(x, y, diameter, diameter);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, diameter, diameter);
    }

    private void drawValidMove(Graphics g, int row, int col) {
        int x = col * CELL_SIZE + CELL_SIZE/3;
        int y = row * CELL_SIZE + CELL_SIZE/3;
        int size = CELL_SIZE/3;

        g.setColor(new Color(0, 255, 0, 100));
        g.fillRect(x, y, size, size);
    }

    private void handlePlayerMove(int row, int col) {
        if (model.isValidMove(row, col)) {
            makeMove(row, col);

            if (!model.isGameOver() && model.getCurrentPlayer() == OthelloModel.WHITE) {
                // Computer's turn
                isComputerThinking = true;
                updateStatus();
                boardPanel.repaint();

                // Use SwingWorker to prevent GUI freezing
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            Thread.sleep(500); // Add small delay for better UX
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        makeComputerMove();
                        isComputerThinking = false;
                        updateStatus();
                        boardPanel.repaint();
                    }
                };
                worker.execute();
            }
        }
    }

    private void makeMove(int row, int col) {
        model.makeMove(row, col);
        updateStatus();
        boardPanel.repaint();
    }

    private void makeComputerMove() {
        int[] move = model.computeGreedyMove();
        if (move[0] != -1 && move[1] != -1) {
            makeMove(move[0], move[1]);
        }
    }

    private void updateStatus() {
        if (model.isGameOver()) {
            int[] score = model.getScore();
            String winner = score[0] > score[1] ? "Black" : score[0] < score[1] ? "White" : "Draw";
            statusLabel.setText(String.format("Game Over! %s wins! Score: Black %d - White %d",
                    winner, score[0], score[1]));
        } else {
            String player = model.getCurrentPlayer() == OthelloModel.BLACK ? "Black" : "White";
            String thinking = isComputerThinking ? " (thinking...)" : "";
            statusLabel.setText(player + "'s turn" + thinking);
        }
    }
} 