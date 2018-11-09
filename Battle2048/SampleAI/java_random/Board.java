import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Board {
    public static final int NB_ROWS = 5;
    public static final int NB_COLS = 5;

    final static int[] dx = {0, 1, 0, -1};
    final static int[] dy = {-1, 0, 1, 0};
    final static EnumMap<Direction, Integer> dirToVec = Board.initDirToVec();

    public final int[][] board = new int[NB_ROWS][NB_COLS];
    public final int nbMerges;

    public Board(int[][] board, int nbMerges) {
        for (int i = 0; i < NB_ROWS; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, NB_COLS);
        }
        this.nbMerges = nbMerges;
    }

    public Board() {
        for (int i = 0; i < NB_ROWS; i++) {
            for (int j = 0; j < NB_COLS; j++) {
                board[i][j] = 0;
            }
        }
        this.nbMerges = 0;
    }

    public boolean withinBounds(int r, int c) {
        return r >= 0 && r < NB_ROWS && c >= 0 && c < NB_COLS;
    }

    public boolean isMovable(Direction dir) {
        int vecIndex = dirToVec.get(dir);
        for (int i = 0; i < NB_ROWS; i++) {
            for (int j = 0; j < NB_COLS; j++) {
                if (board[i][j] > 0) {
                    int ci = i + dy[vecIndex];
                    int cj = j + dx[vecIndex];
                    if (withinBounds(ci, cj) && board[ci][cj] == board[i][j]) {
                        return true;
                    }
                    while (withinBounds(ci, cj)) {
                        if (board[ci][cj] == 0)  {
                            return true;
                        }
                        ci += dy[vecIndex];
                        cj += dx[vecIndex];
                    }
                }
            }
        }
        return false;
    }

    public Board move(Direction dir) {
        int vecIndex = dirToVec.get(dir);
        int[][] newBoard = new int[NB_ROWS][NB_COLS];
        boolean[][] merged = new boolean[NB_ROWS][NB_COLS];

        for (int i = 0; i < NB_ROWS; i++) {
            for (int j = 0; j < NB_COLS; j++) {
                merged[i][j] = false;
                newBoard[i][j] = board[i][j];
            }
        }

        List<Integer> rowsToTraverse = IntStream.range(0, NB_ROWS)
                .boxed().collect(Collectors.toList());
        List<Integer> colsToTraverse = IntStream.range(0, NB_COLS)
                .boxed().collect(Collectors.toList());
        if (dy[vecIndex] == 1) {
            Collections.reverse(rowsToTraverse);
        }
        if (dx[vecIndex] == 1) {
            Collections.reverse(colsToTraverse);
        }

        int nbMerges = 0;
        for (int i : rowsToTraverse) {
            for (int j : colsToTraverse) {
                int ci = i, cj = j;
                int tileToMoveValue = newBoard[i][j];
                while (withinBounds(ci, cj) && tileToMoveValue != 0) {
                    ci += dy[vecIndex];
                    cj += dx[vecIndex];

                    // encountered obstacle
                    if (!withinBounds(ci, cj) || newBoard[ci][cj] != 0) {
                        if (withinBounds(ci, cj) && !merged[ci][cj] && tileToMoveValue == newBoard[ci][cj]) {
                            nbMerges++;
                            newBoard[i][j] = 0;
                            merged[ci][cj] = true;
                            newBoard[ci][cj]++;
                            break;
                        } else {
                            boolean wasMerged = merged[i][j];
                            merged[i][j] = false;
                            newBoard[i][j] = 0;
                            merged[ci - dy[vecIndex]][cj - dx[vecIndex]] = wasMerged;
                            newBoard[ci - dy[vecIndex]][cj - dx[vecIndex]] = tileToMoveValue;
                            break;
                        }
                    }
                }
            }
        }
        return new Board(newBoard, nbMerges);
    }

    private static EnumMap<Direction, Integer> initDirToVec() {
        EnumMap<Direction, Integer> result = new EnumMap<>(Direction.class);
        result.put(Direction.UP, 0);
        result.put(Direction.RIGHT, 1);
        result.put(Direction.DOWN, 2);
        result.put(Direction.LEFT, 3);
        return result;
    }
}
