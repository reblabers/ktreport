package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"os"
	"sort"
	"strings"
)

// KtTestReport はKotlinテスト結果のJSON形式
type KtTestReport struct {
	TestResults []KtTestResult `json:"testResults"`
	TotalTests  int            `json:"totalTests"`
	Passed      int            `json:"passed"`
	Failed      int            `json:"failed"`
	Skipped     int            `json:"skipped"`
	StartTime   int64          `json:"startTime"`
	EndTime     int64          `json:"endTime"`
	TotalDurationMs int        `json:"totalDurationMs"`
}

// KtTestResult は個々のテスト結果
type KtTestResult struct {
	Identifier KtTestIdentifier `json:"identifier"`
	UniqueId       string           `json:"uniqueId"`
	SpecId         string           `json:"specId"`
	Status         string           `json:"status"`
	StartTime      int64            `json:"startTime"`
	EndTime        int64            `json:"endTime"`
	DurationMs     int              `json:"durationMs"`
	Stdout         string           `json:"stdout,omitempty"`
	Stderr         string           `json:"stderr,omitempty"`
	Throwable      string           `json:"throwable,omitempty"`
}

// KtTestIdentifier はテスト識別子
type KtTestIdentifier struct {
	DisplayName    string   `json:"displayName"`
	Type           string   `json:"type"`
	Tags           []string `json:"tags"`
	TestSourceName string   `json:"testSourceName,omitempty"`
	TestSourceFull string   `json:"testSourceFull,omitempty"`
}

// SpecResult はspecIdごとの集計結果
type SpecResult struct {
	SpecId    string
	TestCount int
	PassCount int
	FailCount int
	Time      float64
	Results   []string // "." または "F" の配列
}

// ANSIカラーコード
const (
	colorReset  = "\033[0m"
	colorRed    = "\033[31m"
	colorGreen  = "\033[32m"
	colorBold   = "\033[1m"
)

func main() {
	// スタックトレースの行数のオプション定義
	stackTraceLines := flag.Int("stack", 7, "スタックトレースの表示行数")
	flag.IntVar(stackTraceLines, "s", 7, "スタックトレースの表示行数 (短縮形)")
	
	// コマンドライン引数を解析
	flag.Parse()

	// JSONファイルを処理
	report, err := processJsonTestResults()
	if err != nil {
		fmt.Printf("エラー: %v\n", err)
		return
	}
	
	// テスト結果をUniqueIdでソート
	sortTestResultsByUniqueId(report)
	
	// ヘッダーを表示
	fmt.Printf("= ktreport ===============================================================================================\n\n")
	
	// specIdごとに結果を集計して表示
	displayResultsBySpecId(report)
	
	// 失敗したテストの詳細を表示
	failedResults := getFailedResults(report.TestResults)
	if len(failedResults) > 0 {
		fmt.Printf("\n= failures ===============================================================================================\n")
		displayFailedResults(failedResults, *stackTraceLines)
	}
	
	// サマリー情報を表示
	fmt.Printf("\n= short test summary info =================================================================================\n")
	displaySummary(report.TotalTests, report.Failed, float64(report.TotalDurationMs)/1000.0)
	fmt.Printf("===========================================================================================================\n")
}

// JSONファイルを読み込んでKtTestReportを返す関数
func processJsonTestResults() (*KtTestReport, error) {
	// JSONファイルを検索
	jsonFile := "build/test-results/ktreport.json"

	// JSONファイルを読み込む
	data, err := os.ReadFile(jsonFile)
	if err != nil {
		return nil, fmt.Errorf("JSONファイル '%s' の読み込み中にエラーが発生しました: %w", jsonFile, err)
	}

	// JSONをパース
	var report KtTestReport
	if err := json.Unmarshal(data, &report); err != nil {
		return nil, fmt.Errorf("JSONファイル '%s' のパース中にエラーが発生しました: %w", jsonFile, err)
	}

	return &report, nil
}

// テスト結果をUniqueIdでソートする関数
func sortTestResultsByUniqueId(report *KtTestReport) {
	sort.Slice(report.TestResults, func(i, j int) bool {
		return report.TestResults[i].UniqueId < report.TestResults[j].UniqueId
	})
}

// specIdごとにテスト結果を集計して表示する関数
func displayResultsBySpecId(report *KtTestReport) {
	// specIdごとに結果を集計
	specResults := make(map[string]*SpecResult)
	
	for _, result := range report.TestResults {
		specId := result.SpecId
		if specId == "" {
			continue
		}
		
		// specIdの結果を取得または作成
		specResult, exists := specResults[specId]
		if !exists {
			specResult = &SpecResult{
				SpecId: specId,
			}
			specResults[specId] = specResult
		}
		
		// テスト結果を集計
		specResult.TestCount++
		if result.Status == "FAILED" || result.Throwable != "" {
			specResult.FailCount++
			specResult.Results = append(specResult.Results, "F")
		} else {
			specResult.PassCount++
			specResult.Results = append(specResult.Results, ".")
		}
		
		// 時間を集計（ミリ秒から秒に変換）
		specResult.Time += float64(result.DurationMs) / 1000.0
	}
	
	// 現在のspecIdを表示
	var currentSpecId string
	for _, result := range report.TestResults {
		specId := result.SpecId
		if specId == "" || specId == currentSpecId {
			continue
		}
		
		// 前のspecIdの結果を表示
		if currentSpecId != "" {
			displaySpecResult(specResults[currentSpecId])
		}
		
		currentSpecId = specId
	}
	
	// 最後のspecIdの結果を表示
	if currentSpecId != "" {
		displaySpecResult(specResults[currentSpecId])
	}
}

// specIdの結果を表示する関数
func displaySpecResult(result *SpecResult) {
	fmt.Printf("%s ", result.SpecId)
	
	// テスト結果のドットを表示
	for _, r := range result.Results {
		if r == "." {
			fmt.Printf("%s.%s", colorGreen, colorReset)
		} else {
			fmt.Printf("%sF%s", colorRed, colorReset)
		}
	}
	
	// 時間を表示
	fmt.Printf(" (%.3fs)\n", result.Time)
}

// 失敗したテスト結果を取得する関数
func getFailedResults(testResults []KtTestResult) []KtTestResult {
	var failedResults []KtTestResult
	
	for _, result := range testResults {
		// 失敗したテストかどうかを判定
		if result.Status == "FAILED" || result.Throwable != "" {
			failedResults = append(failedResults, result)
		}
	}
	
	return failedResults
}

// 失敗したテストの詳細を表示する関数
func displayFailedResults(failedResults []KtTestResult, stackTraceLines int) {
	for i, result := range failedResults {
		// 区切り線を追加（最初のテスト以外）
		if i > 0 {
			fmt.Print("\n***\n")
		}
		
		// テスト名と時間を表示
		fmt.Printf("\n%s%s%s (%.3fs)\n\n", colorBold, result.UniqueId, colorReset, float64(result.DurationMs)/1000.0)
		
		// Throwableがあれば表示（指定された行数まで）
		if result.Throwable != "" {
			stackLines := strings.Split(strings.TrimSpace(result.Throwable), "\n")
			displayLines := stackTraceLines

			stackTraceCount := 0
			omittedCount := 0
			for _, line := range stackLines {
				if !strings.HasPrefix(strings.TrimSpace(line), "at ") {
					if (0 < omittedCount) {
						fmt.Printf("... (%d lines omitted)\n", omittedCount)
					}

					fmt.Println(line)
					stackTraceCount = 0
					omittedCount = 0
				} else if (stackTraceCount < displayLines) {
					fmt.Println(line)
					stackTraceCount++
				} else {
					omittedCount++
				}
			}

			if (0 < omittedCount) {
				fmt.Printf("... (%d lines omitted)\n", omittedCount)
			}
		}
		
		// 標準出力があれば表示
		if result.Stdout != "" {
			fmt.Printf("\nSTDOUT:\n%s\n", strings.TrimRight(result.Stdout, "\r\n"))
		}
		
		// 標準エラー出力があれば表示
		if result.Stderr != "" {
			fmt.Printf("\nSTDERR:\n%s\n", strings.TrimRight(result.Stderr, "\r\n"))
		}
	}
}

// サマリー情報を表示する関数
func displaySummary(totalTests, totalFailures int, totalTime float64) {
	if totalFailures > 0 {
		fmt.Printf("%sFAILED%s ", colorRed, colorReset)
	} else {
		fmt.Printf("%sPASSED%s ", colorGreen, colorReset)
	}
	
	fmt.Printf("%d passed", totalTests-totalFailures)
	if totalFailures > 0 {
		fmt.Printf(", %d failed", totalFailures)
	}
	
	fmt.Printf(" in %.3fs\n", totalTime)
}
