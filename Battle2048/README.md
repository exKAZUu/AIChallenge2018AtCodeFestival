# Battle2048

本ドキュメントは、[Releases](https://github.com/exKAZUu/AIChallenge2018AtCodeFestival/releases)で公開中の zip ファイルに同梱することを前提に、
AI Challenge 2018 @ CODE FESTIVAL 用のゲームソフトウェア `Battle2048` の実行方法を説明します。

## 実行方法

実行には Java8 以降が必要です。

本ドキュメントが存在するディレクトリでコンソールを開き、以下のコマンドで Battle2048 を実行すると、デフォルト AI プログラム同士の対戦が行われ、対戦結果が標準出力へ出力されます。
同時に、`game.log` ファイルが生成され、このファイルを[ビジュアライザ](https://www.exkazuu.net/GameViewerForAIChallenge2018AtCodeFestival)に入力することで、実行した AI の対戦を見ることができます。

    java -jar Battle2048.jar

自分の AI を対戦させるには、以下のように-a オプションで AI プログラムの実行コマンドを指定します。-w オプションでワーキングディレクトリも指定できます。
対戦相手にはデフォルト AI が使用されます。

    cd SampleAI
    g++ random.cpp -o a.out -O2 --std=c++17
    cd ..
    java -jar Battle2048.jar -a "./SampleAI/a.out"
    # または
    java -jar Battle2048.jar -a "./a.out" -w "./SampleAI"

AI プログラムおよびワーキングディレクトリはそれぞれ 2 つまで指定できます。

    java -jar Battle2048.jar -a "./a.out" -w "./SampleAI" -a "./a.out" -w "./SampleAI"

対戦中のプログラムの標準エラー出力の内容は、 `-l` のフラグを有効にしてプログラムを実行することで、確認できます。
また、対戦中のすべての入出力を含めた詳細なログは、 `-ll` をしてプログラムを実行することで、確認できます。
どちらの場合も、標準出力とログファイル（`log.txt`）の両方に結果が出力されます。

その他のオプションについては以下のコマンドで表示されるヘルプをご参照ください。

    java -jar Battle2048.jar -h

## 実行結果

プログラムを実行すると次の様な結果が表示されます。

```json
{ "winner": 1, "reasonOfOpponentDefeat": "PRESENTATION_ERROR_AT_MAIN_OR_ATTACK" }
```

`winner` は勝利したプレイヤーの番号（一人目なら`0`、二人目なら`1`）、`reasonOfOpponentDefeat`は勝利した理由（相手が敗北した理由）です。

| reasonOfOpponentDefeat               | 意味                                                                                                     |
| ------------------------------------ | -------------------------------------------------------------------------------------------------------- |
| PRESENTATION_ERROR_AT_INIT           | 「ゲーム開始前の出力」の出力フォーマットが正しくない                                                     |
| INVALID_POSITION_AT_INIT             | 「ゲーム開始前の出力」でボードの外を指定した                                                             |
| PRESENTATION_ERROR_AT_MAIN_OR_ATTACK | 「各ターンの出力」の出力フォーマットが正しくない                                                         |
| CANNOT_MOVE_AT_MAIN                  | 「各ターンの出力」でスライドできない方向を指定した、もしくは、ターン開始時にスライドできる方向がなかった |
| INVALID_M_AND_V_AT_ATTACK            | 「各ターンの出力」のマージされたマスの数と相手のボードに置く数が一致していない                           |
| INVALID_NUMBER_OF_R_AND_C_AT_ATTACK  | 「各ターンの出力」の M と r<sub>i</sub>,c<sub>i</sub>の数が一致していない                                |
| INVALID_POSITION_AT_ATTACK           | 「各ターンの出力」でボードの外を指定した                                                                 |
| ALREADY_OCCUPIED_AT_ATTACK           | 「各ターンの出力」で既に数字があるマスを指定した                                                         |
| LESS_SCORE_AT_END                    | ゲーム終了時、自分のスコアが相手のスコアを上回った                                                       |
| FIRST_BUT_SAME_SCORE_AT_END          | ゲーム終了時、自分のスコアと相手のスコアと同じで、自分が後攻だった                                       |

## サンプルプログラム

SampleAI ディレクトリにサンプルプログラムが入っています。AI を作成する際の参考にしてください
AI の入出力形式については[ゲームルール](https://www.exkazuu.net/AIChallenge2018AtCodeFestival/rules/)をご参照ください。
