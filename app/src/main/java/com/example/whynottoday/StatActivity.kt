package com.example.whynottoday

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*

import com.github.mikephil.charting.utils.ColorTemplate


class StatActivity : AppCompatActivity() {

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

    // 핑계 랭킹 영역(동적으로 추가)
    private lateinit var layoutExcuseImportantRank: LinearLayout
    private lateinit var layoutExcuseNormalRank: LinearLayout

    // 바텀 네비 버튼
    private lateinit var listMenuImageButton: ImageButton
    private lateinit var homeMenuImageButton: ImageButton
    private lateinit var statMenuImageButton: ImageButton

    // 현재 선택된 연/월 (일단 2025년 1월부터 시작)
    private var currentYear = 2025
    private var currentMonth = 1
    private lateinit var chartTotal: PieChart
    private lateinit var chartWorstTime: LineChart


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        val commonUIHandler = CommonUIHandler()
        commonUIHandler.setupListener(this)

        initViews()
//        setupBottomNavigation()
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
        chartWorstTime = findViewById(R.id.chartWorstTime)

        listMenuImageButton = findViewById(R.id.listMenuImageButton)
        homeMenuImageButton = findViewById(R.id.homeMenuImageButton)
        statMenuImageButton = findViewById(R.id.statMenuImageButton)

        // 처음 월 텍스트 설정
        updateMonthTitle()
    }

    /** 하단 네비게이션 버튼 동작 */
//    private fun setupBottomNavigation() {
//        // 리스트 화면으로
//        listMenuImageButton.setOnClickListener {
//            val intent = Intent(this, ListActivity::class.java)
//            startActivity(intent)
//            overridePendingTransition(0, 0)
//        }
//
//        // 홈 화면으로
//        homeMenuImageButton.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            overridePendingTransition(0, 0)
//        }
//
//        // 통계 화면(현재 화면) - 선택된 느낌 내고 싶으면 tint만 바꿔주기
//        statMenuImageButton.isEnabled = false   // 현재 탭이니까 비활성화 느낌
//        statMenuImageButton.imageTintList =
//            getColorStateList(android.R.color.holo_blue_dark)
//    }

    /** 월 이동 버튼 동작 */
    private fun setupMonthSelector() {
        btnPrevMonth.setOnClickListener {
            moveMonth(-1)
        }
        btnNextMonth.setOnClickListener {
            moveMonth(1)
        }
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

        // 실제로는 여기서 DB / SharedPreference 에서
        // currentYear, currentMonth에 해당하는 통계 데이터를 불러오면 됨.
        renderStatistics()
    }

    private fun updateMonthTitle() {
        tvMonthTitle.text = "${currentYear}년 ${currentMonth}월"
    }

    /** 통계 값 채워 넣기 (지금은 더미 데이터) */
    private fun renderStatistics() {
        // TODO: 나중에 실제 통계 데이터로 교체하기

        // 1) 전체 성공률
        tvTotalSuccessPercent.text = "55%"  // 예시

        // 2) 중요한 일 / 일반 일 성공 / 실패
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(55f, "성공"))
            add(PieEntry(45f, "실패"))
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                0xFF7280FF.toInt(), // 성공
                0xFFCCCCCC.toInt()  // 실패
            )
            sliceSpace = 2f
            valueTextSize = 0f   // 도넛 안 숫자는 우리가 TextView로 쓰니까 숨김
        }

        chartTotal.data = PieData(dataSet)
        chartTotal.description.isEnabled = false
        chartTotal.legend.isEnabled = false
        chartTotal.setUsePercentValues(false)
        chartTotal.setDrawEntryLabels(false)
        chartTotal.setDrawCenterText(false)
        chartTotal.setHoleColor(android.graphics.Color.TRANSPARENT)
        chartTotal.holeRadius = 70f
        chartTotal.setTouchEnabled(false)
        chartTotal.invalidate()

        // --- 2) 선 그래프 (못 한 시간대) ---
        val lineEntries = ArrayList<Entry>().apply {
            // x: 시간, y: 실패 개수 (예시 데이터)
            add(Entry(9f, 3f))
            add(Entry(11f, 5f))
            add(Entry(13f, 2f))
            add(Entry(15f, 4f))
            add(Entry(17f, 1f))
            add(Entry(19f, 2f))
        }

        val lineDataSet = LineDataSet(lineEntries, "").apply {
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
            color = 0xFF7280FF.toInt()
            setCircleColor(0xFF7280FF.toInt())
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

        // 3) 핑계 랭킹
        val importantExcuses = listOf(
            ExcuseItem("귀찮았어요", 57),
            ExcuseItem("다른 일이 있었어요", 21),
            ExcuseItem("시간이 모자랐어요", 9),
            ExcuseItem("두루뭉실 이유였어요", 5)
        )

        val normalExcuses = listOf(
            ExcuseItem("시간이 없었어요", 67),
            ExcuseItem("내일 해도 된다고 생각했어요", 23),
            ExcuseItem("눈앞의 일이 더 급했어요", 11)
        )

        renderExcuseList(layoutExcuseImportantRank, "오늘 해야 할 중요한 일", importantExcuses, true)
        renderExcuseList(layoutExcuseNormalRank, "해야 할 일", normalExcuses, false)
    }

    /** 핑계 랭킹을 LinearLayout 안에 동적으로 추가하는 함수 */
    private fun renderExcuseList(
        parent: LinearLayout,
        title: String,
        excuses: List<ExcuseItem>,
        isImportant: Boolean
    ) {
        parent.removeAllViews()

        // 제목 (★ 오늘 해야 할 중요한 일 이런 거)
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

    // 핑계 정보용 간단한 데이터 클래스
    data class ExcuseItem(
        val reason: String,
        val percent: Int
    )
}
