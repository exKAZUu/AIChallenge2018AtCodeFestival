import java.util.Scanner;

public class AIUtils {
    public static Board parseBoard(Scanner scanner) {
        int board[][] = new int[Board.NB_ROWS][Board.NB_COLS];
        for (int i = 0; i < Board.NB_ROWS; i++) {
            for (int j = 0; j < Board.NB_COLS; j++) {
                board[i][j] = scanner.nextInt();
            }
        }
        return new Board(board, 0);
    }

    public static char directionToCommand(Direction dir) {
        switch (dir) {
            case UP:
                return 'U';
            case RIGHT:
                return 'R';
            case DOWN:
                return 'D';
            case LEFT:
                return 'L';
        }
        return 'X';
    }
}
