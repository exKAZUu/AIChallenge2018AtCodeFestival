# FAQ

以下の質問に該当しない内容は、[お問い合わせ](../contact/)からご連絡ください。

## コンテストに関する質問

[参加概要](../guide/)もご覧ください。

### チームでの参加は可能ですか。

複数人でチームを組んでの参加は禁止いたします。
必ず個人で参加してください。

### オープンソースの利用は可能ですか。

各自の責任でライセンス上の問題が無いことを確認した上で、自由に利用して構いません。

### コンテスト終了後に自分の提出や方針をブログや GitHub 上に公開してよいですか。

問題ありません。是非公開してコミュニティの活性化に繋げられればと思います。

## コンテストシステム・提出に関する質問

[オンライン対戦システム上のヘルプ](https://aichallenge.exkazuu.net/help)もご覧ください。

### プログラムを複数ファイルに分割して提出は可能ですか。

必ず zip, tar, tar.bz2, tar.gz のうちのいずれかのアーカイブ形式で投稿してください。
アーカイブ内には複数のファイルが存在していて構いません。

### 提出ファイルサイズに上限はありますか。

1MB 未満の上記アーカイブ形式のファイルのみ受け付けます。

### コンパイル時間やコンパイル時の使用可能メモリに上限はありますか。

コンパイルは 2 分以内に完了しなければなりませんが、それ以外の制約はありません。

### プログラムが実行される環境を教えてください。

仮想 6Core の CPU、8GB のメモリ、200GB の SSD の仮想マシン上の[Docker コンテナ](https://github.com/exKAZUu/ai-container)上で実行しています。

## ルールに関する質問

[ゲームルール](../rules/)もご覧ください。

### アタックフェーズにおいて、相手の盤面に空きが無い場合どうすればいいですか

相手の盤面に空きがない、ということは、直前の相手のターンで、動かしたのに盤面に変化がなかったことになります。
これは敗北条件にマッチしています。
すなわち、相手の盤面には必ず 1 つ以上の空きがあることになるので、質問のような状況は発生しません。