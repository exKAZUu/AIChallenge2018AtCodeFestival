import java.util.Random;
import java.util.Scanner;

public class RandomAI {
    public static void main(String[] args) {
        Random rnd;
        if (args.length > 0) {
            rnd = new Random(Long.parseLong(args[0]));
        } else {
            rnd = new Random();
        }

        Scanner scanner = new Scanner(System.in);

        Board myBoard;
        Board opponentBoard;
        boolean hasFirstTurn = scanner.nextInt() == 0;

        // random placement of tile in opponent's board
        int randomRow = rnd.nextInt(Board.NB_ROWS) + 1;
        int randomCol = rnd.nextInt(Board.NB_COLS) + 1;
        System.out.println(randomRow + " " + randomCol);

        while (true) {
            int currentTurn = scanner.nextInt();
            int timeLeft = scanner.nextInt();
            int myScore = scanner.nextInt();
            int opponentScore = scanner.nextInt();
            myBoard = AIUtils.parseBoard(scanner);
            opponentBoard = AIUtils.parseBoard(scanner);

            boolean wrote = false;
            for (Direction dir : Direction.values()) {
                if (myBoard.isMovable(dir)) {
                    int nbMerges = myBoard.move(dir).nbMerges;
                    int pRow, pCol;
                    do {
                        pRow = rnd.nextInt(Board.NB_ROWS);
                        pCol = rnd.nextInt(Board.NB_COLS);
                    } while (opponentBoard.board[pRow][pCol] != 0);
                    System.out.println(AIUtils.directionToCommand(dir) + " " + 1 + " " + (nbMerges + 1) + " " + (pRow + 1) + " " + (pCol + 1));
                    wrote = true;
                    break;
                }
            }
            if (!wrote) {
                System.out.println("die.\n");
                System.out.flush();
            }
        }
    }
}
