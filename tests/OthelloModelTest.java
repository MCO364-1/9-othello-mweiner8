import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class OthelloModelTest {
    private OthelloModel model;

    @BeforeEach
    void setUp() {
        model = new OthelloModel();
    }

    @Test
    void testInitialBoard() {
        int[][] board = model.getBoard();
        int mid = OthelloModel.BOARD_SIZE / 2;

        // Check initial piece placement
        assertEquals(OthelloModel.WHITE, board[mid-1][mid-1]);
        assertEquals(OthelloModel.BLACK, board[mid-1][mid]);
        assertEquals(OthelloModel.BLACK, board[mid][mid-1]);
        assertEquals(OthelloModel.WHITE, board[mid][mid]);

        // Check initial score
        int[] score = model.getScore();
        assertEquals(2, score[0]); // Black pieces
        assertEquals(2, score[1]); // White pieces

        // Check initial player
        assertEquals(OthelloModel.BLACK, model.getCurrentPlayer());
    }

    @Test
    void testValidMoves() {
        int mid = OthelloModel.BOARD_SIZE / 2;

        // Test valid moves for initial position
        // In Othello, valid moves for Black in the starting position are:
        // (2,3), (3,2), (4,5), (5,4)
        assertTrue(model.isValidMove(mid-1, mid-2)); // (3,2)
        assertTrue(model.isValidMove(mid-2, mid-1)); // (2,3)
        assertTrue(model.isValidMove(mid+1, mid));   // (5,4)
        assertTrue(model.isValidMove(mid, mid+1));   // (4,5)
    }

    @Test
    void testInvalidMoves() {
        int mid = OthelloModel.BOARD_SIZE / 2;

        // Test out-of-bounds moves
        assertFalse(model.isValidMove(-1, mid), "Move above board should be invalid");
        assertFalse(model.isValidMove(mid, OthelloModel.BOARD_SIZE), "Move right of board should be invalid");

        // Test occupied spaces
        assertFalse(model.isValidMove(mid, mid), "Already occupied space should be invalid");

        // Test moves that don't flip pieces
        assertFalse(model.isValidMove(0, 0), "Corner is invalid at start");
        assertFalse(model.isValidMove(mid-1, mid), "Adjacent to pieces but doesn't flip should be invalid");

        // Test that making invalid moves throws exception
        assertThrows(IllegalArgumentException.class, () -> model.makeMove(0, 0));

        assertThrows(IllegalArgumentException.class, () -> model.makeMove(mid, mid));
    }

    @Test
    void testMakeMove() {
        int mid = OthelloModel.BOARD_SIZE / 2;

        // Make a valid move at (3,2) - this is one of the valid starting moves
        model.makeMove(mid-1, mid-2);

        // Check if the piece was placed
        int[][] board = model.getBoard();
        assertEquals(OthelloModel.BLACK, board[mid-1][mid-2]);

        // Check if the correct piece was flipped
        assertEquals(OthelloModel.BLACK, board[mid-1][mid-1]);

        // Check if player switched
        assertEquals(OthelloModel.WHITE, model.getCurrentPlayer());

        // Check updated score
        int[] score = model.getScore();
        assertEquals(4, score[0]); // Black should now have 4 pieces
        assertEquals(1, score[1]); // White should now have 1 piece
    }

    @Test
    void testGameOver() {
        assertFalse(model.isGameOver());

        // Make a series of valid moves that lead to a game over situation
        int mid = OthelloModel.BOARD_SIZE / 2;

        // Make some valid moves first
        model.makeMove(mid-1, mid-2); // Valid first move for Black
        if (model.isValidMove(mid-2, mid-2)) {
            model.makeMove(mid-2, mid-2); // Valid move for White
        }

        // Continue with more moves if needed
        while (!model.isGameOver() && model.hasValidMoves()) {
            int[] move = model.computeGreedyMove();
            if (move[0] != -1 && move[1] != -1) {
                model.makeMove(move[0], move[1]);
            } else {
                break;
            }
        }

        // After no valid moves are available for both players
        // either the game should be over or there should be no valid moves
        assertTrue(model.isGameOver() || !model.hasValidMoves());
    }

    @Test
    void testGreedyMove() {
        int[] move = model.computeGreedyMove();

        // Check that the computed move is valid
        assertTrue(move[0] >= 0 && move[0] < OthelloModel.BOARD_SIZE);
        assertTrue(move[1] >= 0 && move[1] < OthelloModel.BOARD_SIZE);
        assertTrue(model.isValidMove(move[0], move[1]));

        // Make the move and verify it flips at least one piece
        int[] scoreBefore = model.getScore();
        model.makeMove(move[0], move[1]);
        int[] scoreAfter = model.getScore();

        // The score for the current player should increase
        assertTrue(scoreAfter[0] > scoreBefore[0]); // Black's score should increase
        assertTrue(scoreAfter[1] < scoreBefore[1]); // White's score should decrease
        assertEquals(scoreAfter[0] + scoreAfter[1], scoreBefore[0] + scoreBefore[1] + 1); // Total pieces should increase by 1
    }

    @Test
    void testGreedyMove_SimpleChoice() {
        // A realistic early-game position where Black has two options
        setupCustomBoard(new int[][] {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, -1, 0, 0, 0},
                {0, 0, 0, -1, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0}
        }, OthelloModel.BLACK);

        int[] move = model.computeGreedyMove();
        int flipsForChosenMove = model.countFlips(move[0], move[1]);

        assertTrue(isOptimalMove(move[0], move[1]));
        assertTrue(flipsForChosenMove >= 1, "Should choose a move that flips at least 1 piece");
    }

    @Test
    void testGreedyMove_MultipleDirections() {
        // Mid-game position where pieces have naturally spread from the center
        setupCustomBoard(new int[][] {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, -1, 0, 0, 0},
                {0, 0, 0, 1, -1, 0, 0, 0},
                {0, 0, 0, -1, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0}
        }, OthelloModel.BLACK);

        int[] move = model.computeGreedyMove();
        int flipsForChosenMove = model.countFlips(move[0], move[1]);

        assertTrue(isOptimalMove(move[0], move[1]));
        assertTrue(flipsForChosenMove >= 2, "Should choose a move that flips at least 2 pieces");
    }

    @Test
    void testGreedyMove_EdgeOpportunity() {
        // Mid-game position with edge opportunity
        setupCustomBoard(new int[][] {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, -1, 0, 0, 0, 0},
                {0, 0, -1, 1, -1, 0, 0, 0},
                {0, 0, 0, -1, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0}
        }, OthelloModel.BLACK);

        int[] move = model.computeGreedyMove();
        int flipsForChosenMove = model.countFlips(move[0], move[1]);

        assertTrue(isOptimalMove(move[0], move[1]));
        assertTrue(flipsForChosenMove >= 2, "Should choose a move that flips at least 2 pieces");
    }

    @Test
    void testGreedyMove_MidGame() {
        // Mid-game position with multiple capture opportunities
        setupCustomBoard(new int[][] {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, -1, 0, 0, 0},
                {0, 0, -1, 1, -1, 0, 0, 0},
                {0, 0, 0, -1, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0}
        }, OthelloModel.BLACK);

        int[] move = model.computeGreedyMove();
        int flipsForChosenMove = model.countFlips(move[0], move[1]);

        assertTrue(isOptimalMove(move[0], move[1]));
        assertTrue(flipsForChosenMove >= 2, "Should choose a move that flips at least 2 pieces");
    }

    // Helper method to verify if a move is optimal (no other move flips more pieces)
    private boolean isOptimalMove(int row, int col) {
        int maxFlips = model.countFlips(row, col);

        // Check all possible moves
        for (int i = 0; i < OthelloModel.BOARD_SIZE; i++) {
            for (int j = 0; j < OthelloModel.BOARD_SIZE; j++) {
                if (model.isValidMove(i, j)) {
                    int flips = model.countFlips(i, j);
                    if (flips > maxFlips) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Helper method to set up custom board configurations for testing
    private void setupCustomBoard(int[][] boardConfig, int currentPlayer) {
        try {
            java.lang.reflect.Field boardField = OthelloModel.class.getDeclaredField("board");
            boardField.setAccessible(true);
            boardField.set(model, boardConfig);

            java.lang.reflect.Field playerField = OthelloModel.class.getDeclaredField("currentPlayer");
            playerField.setAccessible(true);
            playerField.set(model, currentPlayer);
        } catch (Exception e) {
            fail("Failed to set up custom board: " + e.getMessage());
        }
    }
}