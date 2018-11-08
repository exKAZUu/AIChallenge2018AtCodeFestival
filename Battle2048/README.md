# Battle2048

本ドキュメントは、AI CHALLENGE 2018 @ CODE FESTIVAL 用のゲームソフトウェア `Battle2048` の実行方法について説明しております。

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

## サンプルプログラム

SampleAI ディレクトリにサンプルプログラムが入っています。AI を作成する際の参考にしてください
AI の入出力形式については[ゲームルール](https://www.exkazuu.net/AIChallenge2018AtCodeFestival/rules/)をご参照ください。
