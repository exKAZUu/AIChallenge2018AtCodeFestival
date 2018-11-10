import java.util.Scanner;

public class GreedyAI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean hasFirstTurn = scanner.nextInt() == 0;

        int initialRow = 1;
        int initialCol = 1;
        System.out.println(initialRow + " " + initialCol);
        System.out.flush();

        while (true) {
            int currentTurn = scanner.nextInt();
            int timeLeft = scanner.nextInt();
            int myScore = scanner.nextInt();
            int opponentScore = scanner.nextInt();
            Board myBoard = AIUtils.parseBoard(scanner);
            Board opponentBoard = AIUtils.parseBoard(scanner);

            // go through all directions and give the one that will yield the most merges
            Direction greedyDir = Direction.DOWN;
            int maxNumberMerges = 0;
            for (Direction dir : Direction.values()) {
                if (myBoard.isMovable(dir)) {
                    int nbMerges = myBoard.move(dir).nbMerges;
                    if (nbMerges >= maxNumberMerges) {
                        greedyDir = dir;
                        maxNumberMerges = nbMerges;
                    }
                }
            }
            int minEnemyMerges = Integer.MAX_VALUE;
            int enemyRow = -1;
            int enemyCol = -1;
            for (int i = 0; i < Board.NB_ROWS; i++) {
                for (int j = 0; j < Board.NB_COLS; j++) {
                    if (opponentBoard.board[i][j] == 0) {
                        int totalPossibleMerges = 0;
                        opponentBoard.board[i][j] = maxNumberMerges + 1;
                        for (Direction dir : Direction.values()) {
                            if (opponentBoard.isMovable(dir)) {
                                totalPossibleMerges += opponentBoard.move(dir).nbMerges;
                            }
                        }
                        if (minEnemyMerges > totalPossibleMerges) {
                            minEnemyMerges = totalPossibleMerges;
                            enemyRow = i;
                            enemyCol = j;
                        }
                        opponentBoard.board[i][j] = 0;
                    }
                }
            }
            System.out.println(AIUtils.directionToCommand(greedyDir) + " " + 1 + " " + (maxNumberMerges + 1) + " " + (enemyRow + 1) + " " + (enemyCol + 1));
        }
    }

}
