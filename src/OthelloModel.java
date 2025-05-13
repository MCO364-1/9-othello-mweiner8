public class OthelloModel {
    public static final int BOARD_SIZE = 8;
    public static final int BLACK = 1;
    public static final int WHITE = -1;
    public static final int EMPTY = 0;

    private final int[][] board;
    private int currentPlayer;

    public OthelloModel() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        currentPlayer = BLACK; // Black always starts
    }

    private void initializeBoard() {
        // Clear the board
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }

        // Set up initial pieces
        int mid = BOARD_SIZE / 2;
        board[mid-1][mid-1] = WHITE;
        board[mid-1][mid] = BLACK;
        board[mid][mid-1] = BLACK;
        board[mid][mid] = WHITE;
    }

    public boolean isValidMove(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE || board[row][col] != EMPTY) {
            return false;
        }

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                if (wouldFlip(row, col, dr, dc)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean wouldFlip(int row, int col, int dr, int dc) {
        int r = row + dr;
        int c = col + dc;
        boolean foundOpponent = false;

        while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE) {
            if (board[r][c] == EMPTY) return false;
            if (board[r][c] == currentPlayer) {
                return foundOpponent;
            }
            foundOpponent = true;
            r += dr;
            c += dc;
        }
        return false;
    }

    public void makeMove(int row, int col) {
        if (!isValidMove(row, col)) {
            throw new IllegalArgumentException("Invalid move");
        }

        board[row][col] = currentPlayer;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                flipPieces(row, col, dr, dc);
            }
        }

        currentPlayer = -currentPlayer;

        // If next player has no valid moves, switch back
        if (!hasValidMoves()) {
            currentPlayer = -currentPlayer;
            // If this player also has no valid moves, game is over
            if (!hasValidMoves()) {
                currentPlayer = EMPTY; // Game over
            }
        }
    }

    private void flipPieces(int row, int col, int dr, int dc) {
        if (!wouldFlip(row, col, dr, dc)) return;

        int r = row + dr;
        int c = col + dc;

        while (board[r][c] != currentPlayer) {
            board[r][c] = currentPlayer;
            r += dr;
            c += dc;
        }
    }

    public boolean hasValidMoves() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (isValidMove(i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int[][] getBoard() {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, BOARD_SIZE);
        }
        return copy;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return currentPlayer == EMPTY;
    }

    public int[] getScore() {
        int blackCount = 0;
        int whiteCount = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == BLACK) blackCount++;
                else if (board[i][j] == WHITE) whiteCount++;
            }
        }
        return new int[]{blackCount, whiteCount};
    }

    public int[] computeGreedyMove() {
        int bestRow = -1;
        int bestCol = -1;
        int maxFlips = -1;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (isValidMove(i, j)) {
                    int flips = countFlips(i, j);
                    if (flips > maxFlips) {
                        maxFlips = flips;
                        bestRow = i;
                        bestCol = j;
                    }
                }
            }
        }

        return new int[]{bestRow, bestCol};
    }

    public int countFlips(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                count += countDirectionalFlips(row, col, dr, dc);
            }
        }
        return count;
    }

    private int countDirectionalFlips(int row, int col, int dr, int dc) {
        if (!wouldFlip(row, col, dr, dc)) return 0;

        int count = 0;
        int r = row + dr;
        int c = col + dc;

        while (board[r][c] != currentPlayer) {
            count++;
            r += dr;
            c += dc;
        }

        return count;
    }
}