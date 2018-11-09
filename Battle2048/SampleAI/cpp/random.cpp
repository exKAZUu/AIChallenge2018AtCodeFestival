// 動かせる方向にランダムに動かすだけのコード。

#include <algorithm>
#include <cstring>
#include <cstdio>
#include <iostream>
#include <map>
#include <string>
#include <vector>

using namespace std;

constexpr int H = 5;
constexpr int W = 5;

constexpr int dx[] = {0,-1,0,1};
constexpr int dy[] = {-1,0,1,0};
const string i2c = "LURD";
const map<char,int> c2i = {{'L', 0}, {'U', 1}, {'R', 2}, {'D', 3}};

class Xorshift128 {
private:
  static constexpr unsigned MASK = 0x7FFFFFFF;
  unsigned x = 123456789, y = 987654321, z = 1000000007, w;
public:
  unsigned rnd(){
    unsigned t = x ^ (x << 11);
    x = y; y = z; z = w;
    w = (w ^ (w >> 19)) ^ (t ^ (t >> 8));
    return w & MASK;
  }
  Xorshift128(const unsigned seed) : w(seed) {}
};

class Board {
public:
    int board[H][W];
    Board(){}

    void reload() {
        for (int i=0; i<H; i++) {
            for (int j=0;j<W; j++) cin >> board[i][j];
        }
    }

    inline bool IN(const int x, const int y) const {
        return 0<=x && x<H && 0<=y && y<W;
    }

    bool movable(const int dir) const {
        for (int i=0; i<H; i++) {
            for (int j=0; j<W; j++) if (board[i][j] > 0) {
                int ci = i + dx[dir], cj = j + dy[dir];
                if (IN(ci,cj) && board[ci][cj] == board[i][j]) return true;
                while (IN(ci, cj)) {
                    if (board[ci][cj] == 0) return true;
                    ci += dx[dir];
                    cj += dy[dir];
                }
            }
        }
        return false;
    }

    int move(const int dir) {
        bool merged[H][W];
        Board new_board;

        for (int i=0; i<H; i++) {
            for (int j=0; j<W; j++) {
                merged[i][j] = false;
                new_board.board[i][j] = 0;
            }
        }

        int vi[H], vj[W];
        generate(begin(vi), end(vi), [n = 0] () mutable { return n++; });
        generate(begin(vj), end(vj), [n = 0] () mutable { return n++; });

        if (dx[dir] == 1) reverse(begin(vi), end(vi));
        if (dy[dir] == 1) reverse(begin(vj), end(vj));

        int num_merge = 0;
        for (int i : vi) {
            for (int j : vj) if(board[i][j] > 0) {
                for(int ci = i, cj = j; ; ci += dx[dir], cj += dy[dir]) {
                    if (IN(ci, cj) && new_board.board[ci][cj] == board[i][j] && !merged[ci][cj]) {
                        num_merge++;
                        new_board.board[ci][cj]++;
                        merged[ci][cj] = true;
                        break;
                    }
                    else if (!IN(ci, cj) || new_board.board[ci][cj] != 0) {
                        ci -= dx[dir];
                        cj -= dy[dir];
                        new_board.board[ci][cj] = board[i][j];
                        break;
                    }
                }
            }
        }

        // you can use new_board here
        memcpy(board, new_board.board, H * W * (sizeof(int)));

        return num_merge;
    }
};

int main(int argc, char* argv[]) {
    Xorshift128 prng(12345);

    if (argc > 1) {
        prng = Xorshift128(atoi(argv[1]));
    }

    {
        int turn;
        scanf("%d", &turn);
        printf("%d %d\n", prng.rnd() % H + 1, prng.rnd() % W + 1);
        fflush(stdout);
    }

    Board me, op;
    while (true) {
        int turn, timeleft, myscore, opscore;
        scanf("%d %d %d %d", &turn, &timeleft, &myscore, &opscore);
        me.reload();
        op.reload();

        int s = prng.rnd() % 4;
        bool wrote = false;
        for (int i = 0; i< 4; i++) {
            int dir = (s + i) % 4;
            if(me.movable(dir)) {
                int place = me.move(dir);
                int px, py;
                do {
                    px = prng.rnd() % H;
                    py = prng.rnd() % W;
                } while(op.board[px][py] != 0);
                printf("%c 1 %d %d %d\n", i2c[dir], place+1, px+1, py+1);
                fflush(stdout);
                wrote = true;
                break;
            }
        }
        if (!wrote) {
            printf("die.\n");
            fflush(stdout);
        }
    }

    return 0;
}
