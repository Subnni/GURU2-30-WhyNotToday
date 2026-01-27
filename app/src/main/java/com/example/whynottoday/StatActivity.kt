package com.example.whynottoday

import android.os.Bundle
import android.view.ViewConfiguration
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*

class StatActivity : AppCompatActivity() {

    // 색상 상수
    private val COLOR_PRIMARY = 0xFF7280FF.toInt()
    private val COLOR_FAIL = 0xFFCCCCCC.toInt()

    // 월 타이틀 / 버튼
    private lateinit var tvMonthTitle: TextView
    private lateinit var btnPrevMonth: TextView
    private lateinit var btnNextMonth: TextView

    // 상단 통계 퍼센트
    private lateinit var tvTotalSuccessPercent: TextView
    private lateinit var tvImportantSuccessPercent: TextView
    private lateinit var tvImportantFailPercent: TextView
    private lateinit var tvNormalSuccessPercent: TextView
    private lateinit var tvNormalFailPercent: TextView

    // 차트
    private lateinit var chartTotal: PieChart
    private lateinit var chartImportant: PieChart
    private lateinit var chartNormal: PieChart
    private lateinit var chartWorstTime: LineChart

    // 핑계 랭킹 영역(동적으로 추가)
    private lateinit var layoutExcuseImportantRank: LinearLayout
    private lateinit var layoutExcuseNormalRank: LinearLayout

    // 바텀 네비 버튼 (CommonUIHandler에서 씀)
    private lateinit var listMenuImageButton: ImageButton
    private lateinit var homeMenuImageButton: ImageButton
    private lateinit var statMenuImageButton: ImageButton

    // 현재 선택된 연/월 (일단 2025년 1월부터 시작)
    private var currentYear = 2026
    private var currentMonth = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)
        
        val commonUIHandler = CommonUIHandler()
        commonUIHandler.setupListener(this)

        initViews()
        setupMonthSelector()

        // 더미 데이터 한 번 그려주기
        renderStatistics()
    }

    private fun initViews() {
        tvMonthTitle = findViewById(R.id.tvMonthTitle)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)

        tvTotalSuccessPercent = findViewById(R.id.tvTotalSuccessPercent)
        tvImportantSuccessPercent = findViewById(R.id.tvImportantSuccessPercent)
        tvImportantFailPercent = findViewById(R.id.tvImportantFailPercent)
        tvNormalSuccessPercent = findViewById(R.id.tvNormalSuccessPercent)
        tvNormalFailPercent = findViewById(R.id.tvNormalFailPercent)

        layoutExcuseImportantRank = findViewById(R.id.layoutExcuseImportantRank)
        layoutExcuseNormalRank = findViewById(R.id.layoutExcuseNormalRank)

        chartTotal = findViewById(R.id.chartTotal)
        chartImportant = findViewById(R.id.chartImportant)
        chartNormal = findViewById(R.id.chartNormal)
        chartWorstTime = findViewById(R.id.chartWorstTime)

        listMenuImageButton = findViewById(R.id.listMenuImageButton)
        homeMenuImageButton = findViewById(R.id.homeMenuImageButton)
        statMenuImageButton = findViewById(R.id.statMenuImageButton)

        // 처음 월 텍스트 설정
        updateMonthTitle()
    }

    /** 월 이동 버튼 동작 */
    private fun setupMonthSelector() {
        btnPrevMonth.setOnClickListener { moveMonth(-1) }
        btnNextMonth.setOnClickListener { moveMonth(1) }
    }

    private fun moveMonth(diff: Int) {
        currentMonth += diff
        if (currentMonth <= 0) {
            currentMonth = 12
            currentYear -= 1
        } else if (currentMonth >= 13) {
            currentMonth = 1
            currentYear += 1
        }
        updateMonthTitle()

        // 여기서 선택된 연/월에 맞는 통계 재로딩
        renderStatistics()
    }

    private fun updateMonthTitle() {
        tvMonthTitle.text = "${currentYear}년 ${currentMonth}월"
    }

    /** 통계 값 채워 넣기 */
    private fun renderStatistics() {
        // ✅ 나중에 DB 연결할 때는 이 함수만 수정하면 됨
        val stats = loadMonthlyStats(currentYear, currentMonth)

        // 1) 퍼센트 텍스트
        tvTotalSuccessPercent.text = "${stats.totalSuccessPercent}%"

        tvImportantSuccessPercent.text = "${stats.importantSuccessPercent}%"
        tvImportantFailPercent.text = "${100 - stats.importantSuccessPercent}%"

        tvNormalSuccessPercent.text = "${stats.normalSuccessPercent}%"
        tvNormalFailPercent.text = "${100 - stats.normalSuccessPercent}%"

        // 2) 도넛 차트들
        setupPieChart(
            chartTotal,
            stats.totalSuccessPercent
        )
        setupPieChart(
            chartImportant,
            stats.importantSuccessPercent
        )
        setupPieChart(
            chartNormal,
            stats.normalSuccessPercent
        )

        // 3) 시간대 라인 차트
        setupLineChart(stats.worstTimePoints)

        // 4) 핑계 랭킹
        renderExcuseList(
            layoutExcuseImportantRank,
            "오늘 해야 할 중요한 일",
            stats.importantExcuses,
            isImportant = true
        )
        renderExcuseList(
            layoutExcuseNormalRank,
            "해야 할 일",
            stats.normalExcuses,
            isImportant = false
        )
    }

    /** 공통 도넛 차트 세팅 */
    private fun setupPieChart(chart: PieChart, successPercent: Int) {
        val failPercent = 100 - successPercent

        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(successPercent.toFloat(), "성공"))
            add(PieEntry(failPercent.toFloat(), "실패"))
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(COLOR_PRIMARY, COLOR_FAIL)
            sliceSpace = 2f
            valueTextSize = 0f    // 값은 TextView로 보여줄 거라 숨김
        }

        chart.data = PieData(dataSet)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setUsePercentValues(false)
        chart.setDrawEntryLabels(false)
        chart.setDrawCenterText(false)
        chart.setHoleColor(android.graphics.Color.TRANSPARENT)
        chart.holeRadius = 70f
        chart.setTouchEnabled(false)
        chart.invalidate()
    }

    /** 시간대 라인 차트 세팅 */
    private fun setupLineChart(points: List<Pair<Int, Int>>) {
        val entries = ArrayList<Entry>().apply {
            points.forEach { (hour, failCount) ->
                add(Entry(hour.toFloat(), failCount.toFloat()))
            }
        }

        val lineDataSet = LineDataSet(entries, "").apply {
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
            color = COLOR_PRIMARY
            setCircleColor(COLOR_PRIMARY)
        }

        chartWorstTime.data = LineData(lineDataSet)
        chartWorstTime.description.isEnabled = false
        chartWorstTime.legend.isEnabled = false
        chartWorstTime.axisRight.isEnabled = false
        chartWorstTime.xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textSize = 10f
            granularity = 1f
        }
        chartWorstTime.axisLeft.apply {
            setDrawGridLines(true)
            setDrawAxisLine(false)
            textSize = 10f
            granularity = 1f
        }
        chartWorstTime.setTouchEnabled(false)
        chartWorstTime.invalidate()
    }

    /** 핑계 랭킹을 LinearLayout 안에 동적으로 추가하는 함수 */
    private fun renderExcuseList(
        parent: LinearLayout,
        title: String,
        excuses: List<ExcuseItem>,
        isImportant: Boolean
    ) {
        parent.removeAllViews()

        // 제목
        val titleView = TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(0xFF3A3A3A.toInt())
        }
        parent.addView(titleView)

        // 각 핑계 한 줄씩 (★ 귀찮았어요 (57%))
        excuses.forEach { excuse ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = dp(4)
                layoutParams = lp
            }

            val starView = TextView(this).apply {
                text = "★ "
                textSize = 13f
                setTextColor(
                    if (isImportant) 0xFF4200FF.toInt() else 0xFF999999.toInt()
                )
            }

            val textView = TextView(this).apply {
                text = "${excuse.reason} (${excuse.percent}%)"
                textSize = 13f
                setTextColor(0xFF3A3A3A.toInt())
            }

            row.addView(starView)
            row.addView(textView)
            parent.addView(row)
        }
    }

    // dp를 px로 변환하는 간단한 함수
    private fun dp(value: Int): Int {
        val scale = resources.displayMetrics.density
        return (value * scale + 0.5f).toInt()
    }

    // ====== 여기부터는 데이터 구조 & 더미 데이터 (DB 연결 시 여기만 고치면 됨) ======

    data class ExcuseItem(
        val reason: String,
        val percent: Int
    )

    data class MonthlyStats(
        val totalSuccessPercent: Int,
        val importantSuccessPercent: Int,
        val normalSuccessPercent: Int,
        val worstTimePoints: List<Pair<Int, Int>>, // (시간, 실패 횟수)
        val importantExcuses: List<ExcuseItem>,
        val normalExcuses: List<ExcuseItem>
    )

    /** 나중에 DB에서 월별 통계 불러오도록 바꿀 함수 */
    private fun loadMonthlyStats(year: Int, month: Int): MonthlyStats {

        val db = DBManager(this, "WhyNotTodayDB.db", null, 1).readableDatabase

        // 월 필터: yyyy-MM
        val monthKey = String.format("%04d-%02d", year, month)   // 예: 2025-01

        // 1. 전체 성공/실패 카운트
        val cursorAll = db.rawQuery("""
        SELECT 
            SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END) AS successCnt,
            SUM(CASE WHEN is_done = 0 THEN 1 ELSE 0 END) AS failCnt
        FROM todoTBL
        WHERE date_time LIKE '$monthKey%'
    """.trimIndent(), null)

        var totalSuccess = 0
        var totalFail = 0
        if (cursorAll.moveToFirst()) {
            totalSuccess = cursorAll.getInt(0)
            totalFail = cursorAll.getInt(1)
        }
        cursorAll.close()

        val totalPercent = if (totalSuccess + totalFail == 0)
            0 else (totalSuccess * 100 / (totalSuccess + totalFail))

        // 2. 중요한 일 성공률
        val cursorImportant = db.rawQuery("""
        SELECT 
            SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END) AS successCnt,
            SUM(CASE WHEN is_done = 0 THEN 1 ELSE 0 END) AS failCnt
        FROM todoTBL
        WHERE date_time LIKE '$monthKey%'
        AND is_important = 1
    """.trimIndent(), null)

        var importantSuccess = 0
        var importantFail = 0
        if (cursorImportant.moveToFirst()) {
            importantSuccess = cursorImportant.getInt(0)
            importantFail = cursorImportant.getInt(1)
        }
        cursorImportant.close()

        val importantPercent = if (importantSuccess + importantFail == 0)
            0 else (importantSuccess * 100 / (importantSuccess + importantFail))

        // 3. 일반(중요하지 않은) 일 성공률
        val cursorNormal = db.rawQuery("""
        SELECT 
            SUM(CASE WHEN is_done = 1 THEN 1 ELSE 0 END) AS successCnt,
            SUM(CASE WHEN is_done = 0 THEN 1 ELSE 0 END) AS failCnt
        FROM todoTBL
        WHERE date_time LIKE '$monthKey%'
        AND is_important = 0
    """.trimIndent(), null)

        var normalSuccess = 0
        var normalFail = 0
        if (cursorNormal.moveToFirst()) {
            normalSuccess = cursorNormal.getInt(0)
            normalFail = cursorNormal.getInt(1)
        }
        cursorNormal.close()

        val normalPercent = if (normalSuccess + normalFail == 0)
            0 else (normalSuccess * 100 / (normalSuccess + normalFail))

        // 4. 시간대별 실패 통계 (ex: 09시 → 실패 2개)
        val cursorTime = db.rawQuery("""
        SELECT 
            SUBSTR(date_time, 12, 2) AS hour,   -- HH 가져옴
            COUNT(*) 
        FROM todoTBL
        WHERE date_time LIKE '$monthKey%'
        AND is_done = 0
        GROUP BY hour
        ORDER BY hour ASC
    """.trimIndent(), null)

        val timeList = mutableListOf<Pair<Int, Int>>()
        while (cursorTime.moveToNext()) {
            val hour = cursorTime.getString(0).toInt()     // "09" → 9
            val count = cursorTime.getInt(1)
            timeList.add(hour to count)
        }
        cursorTime.close()

        // 5. 핑계 랭킹 (중요한 일만)
        val cursorExcuseImportant = db.rawQuery("""
        SELECT excuse_reason, COUNT(*) 
        FROM excuseTBL e
        JOIN todoTBL t ON e.todo_id = t.todo_id
        WHERE date_time LIKE '$monthKey%'
        AND t.is_important = 1
        GROUP BY excuse_reason
        ORDER BY COUNT(*) DESC
    """.trimIndent(), null)

        val importantExcuses = mutableListOf<ExcuseItem>()
        while (cursorExcuseImportant.moveToNext()) {
            importantExcuses.add(
                ExcuseItem(
                    cursorExcuseImportant.getString(0),
                    cursorExcuseImportant.getInt(1)
                )
            )
        }
        cursorExcuseImportant.close()

        // 6. 핑계 랭킹 (일반 일)
        val cursorExcuseNormal = db.rawQuery("""
        SELECT excuse_reason, COUNT(*) 
        FROM excuseTBL e
        JOIN todoTBL t ON e.todo_id = t.todo_id
        WHERE date_time LIKE '$monthKey%'
        AND t.is_important = 0
        GROUP BY excuse_reason
        ORDER BY COUNT(*) DESC
    """.trimIndent(), null)

        val normalExcuses = mutableListOf<ExcuseItem>()
        while (cursorExcuseNormal.moveToNext()) {
            normalExcuses.add(
                ExcuseItem(
                    cursorExcuseNormal.getString(0),
                    cursorExcuseNormal.getInt(1)
                )
            )
        }
        cursorExcuseNormal.close()

        db.close()

        return MonthlyStats(
            totalSuccessPercent = totalPercent,
            importantSuccessPercent = importantPercent,
            normalSuccessPercent = normalPercent,
            worstTimePoints = timeList,
            importantExcuses = importantExcuses,
            normalExcuses = normalExcuses
        )
    }
}
