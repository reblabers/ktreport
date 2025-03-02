# Kotlin + Kotest サンプルプロジェクト

このプロジェクトは、ktreportツールのサンプルとして作成されたKotlin + Kotestのプロジェクトです。

## プロジェクト構成

- `src/main/kotlin/com/example/Calculator.kt` - 簡単な計算機クラス
- `src/test/kotlin/com/example/CalculatorTest.kt` - Kotestを使ったテストクラス
- `src/test/kotlin/com/example/KtreportTestListener.kt` - ktreport用のテストリスナー
- `src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener` - テストリスナーの登録

## 実行方法

以下のコマンドでテストを実行し、ktreportでテスト結果を表示できます：

```bash
./gradlew :test || ./ktreport
```

## テスト内容

このサンプルには以下のテストが含まれています：

- 加算のテスト（正の数、負の数、パラメータ化テスト）
- 減算のテスト（正の数、負の数）
- 乗算のテスト（正の数、負の数、わざと失敗するテスト）
- 除算のテスト（正の数、0による除算の例外テスト）

## Kotestのテストスタイル

このサンプルでは、Kotestの様々なテストスタイルを使用しています：

- FunSpec - 関数型のテストスタイル
- StringSpec - 文字列ベースの簡潔なテストスタイル
- DescribeSpec - BDDスタイルのテスト
- BehaviorSpec - Given-When-Thenスタイルのテスト
- ShouldSpec - Should句を使ったテストスタイル

## ktreportの仕組み

1. KtreportTestListenerがテスト実行時に呼び出され、テスト結果を`build/test-results/ktreport.json`に記録します
2. ktreportコマンドがこのJSONファイルを読み込み、整形された結果を表示します
