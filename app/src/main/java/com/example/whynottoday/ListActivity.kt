package com.example.whynottoday

import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whynottoday.CalendarUtils.daysInWeekArray
import com.example.whynottoday.CalendarUtils.monthYearFromDate
import com.example.whynottoday.CalendarUtils.weekFromDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ListActivity : AppCompatActivity(), CalendarAdapter.OnItemListener {

    //사용 변수 - 캘린더
    private lateinit var monthYearTextView: TextView
    private lateinit var weekTextView : TextView
    private lateinit var calendarRecyclerView: RecyclerView

    //사용 변수 - 오늘 날짜 정보
    private lateinit var selectedDateTextView : TextView
    private lateinit var excuseDensityTextView : TextView
    private lateinit var excuseDensityBox : View

    //사용 변수 - 오늘 날짜 리스트
    private lateinit var dbManager: DBManager
    private lateinit var sqlitedb : SQLiteDatabase
    private lateinit var excuseLayout : LinearLayout

    //사용 변수 - 스크롤 제어
    private lateinit var listScrollView : ScrollView
    private lateinit var navigationBar : ConstraintLayout
    var scroll : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        //DB 세팅
        dbManager = DBManager(this, "WhyNotTodayDB.db", null, 1)
        sqlitedb = dbManager.readableDatabase

        //공통 UI 초기화
        val commonUIHandler = CommonUIHandler()
        commonUIHandler.setupListener(this)

        //위젯 초기화
        initWidgets()

        //달력 및 리스트 세팅
        setWeekView()

        //스크롤 방향 감지하여 하단 UI 제어
        listScrollView = findViewById<ScrollView>(R.id.listScrollView)
        navigationBar = findViewById<ConstraintLayout>(R.id.navigationBar)

        listScrollView.getViewTreeObserver().addOnScrollChangedListener {
            val scrollY = listScrollView.scrollY
            //최소 10픽셀 이상 스크롤
            if (scrollY > scroll && scrollY > 10) {
                // 스크롤 다운 시 UI 숨기기
                hideNavigation(navigationBar)
            } else if (scrollY < scroll) {
                // 스크롤 업 시 UI 보이기
                showNavigation(navigationBar)
            }
            scroll = scrollY
        }

    }

    private fun hideNavigation(view: View) {
        view.animate()
            .translationY(view.height.toFloat())
            .setDuration(300)
            .start()
    }

    private fun showNavigation(view: View) {
        view.animate()
            .translationY(0f)
            .setDuration(300)
            .start()
    }
    private fun initWidgets() {
        calendarRecyclerView = findViewById<RecyclerView?>(R.id.calendarRecyclerView)
        monthYearTextView = findViewById<TextView?>(R.id.monthYearTextView)
        weekTextView = findViewById<TextView?>(R.id.weekTextView)
        selectedDateTextView = findViewById<TextView?>(R.id.selectedDateTextView)
        excuseDensityTextView = findViewById<TextView?>(R.id.excuseDensityTextView2)
        excuseDensityBox = findViewById<View?>(R.id.excuseDensityBox)
        excuseLayout = findViewById(R.id.excuseLayout)

    }

    private fun setWeekView() {
        monthYearTextView.setText(monthYearFromDate(CalendarUtils.selectedDate))
        weekTextView.setText(weekFromDate(CalendarUtils.selectedDate))
        val days: ArrayList<LocalDate?>? = daysInWeekArray(CalendarUtils.selectedDate)

        val calendarAdapter = CalendarAdapter(days, this)
        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(getApplicationContext(), 7)
        calendarRecyclerView.setLayoutManager(layoutManager)
        calendarRecyclerView.setAdapter(calendarAdapter)

        setSelectedDateAdapter()
        setExcuseLayoutAdapter()
    }

    fun previousWeekAction(view: View?) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1).with(java.time.DayOfWeek.SATURDAY)
        setWeekView()
    }
    fun nextWeekAction(view: View?) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1)
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY))
        setWeekView()
    }

    public override fun onItemClick(position: Int, date: LocalDate?) {
        CalendarUtils.selectedDate = date
        setWeekView()
    }

    //추가, 수정, 삭제 후 목록 다시 그리기
    override fun onResume() {
        super.onResume()
        setExcuseLayoutAdapter()
    }

    //선택된 날의 날짜, 핑계 농도 출력
    private fun setSelectedDateAdapter(){
        val selectedDate = CalendarUtils.selectedDate
        val format = DateTimeFormatter.ofPattern("MM. dd (E)")
        selectedDateTextView.text = selectedDate.format(format)

        var cursor : Cursor
        var query = "SELECT count(*) FROM todoTBL " +
                "WHERE date_time LIKE '$selectedDate%' " +
                "AND is_done = 0"
        cursor = sqlitedb.rawQuery(query, null)
        val incompleteTaskCount = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()

        var query2 = "SELECT count(*) FROM todoTBL " +
                "INNER JOIN excuseTBL ON todoTBL.todo_id = excuseTBL.todo_id " +
                "WHERE todoTBL.date_time LIKE '$selectedDate%'"
        cursor = sqlitedb.rawQuery(query2, null)
        val excuseCount = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()

        val excuseRatio = if(incompleteTaskCount==0) 0 else {
            ((excuseCount.toFloat()/incompleteTaskCount.toFloat()) * 100).toInt()
        }
        excuseDensityTextView.text = "${minOf(excuseRatio, 100)}%"
        CalendarUtils.updateBoxColor(excuseDensityBox, excuseRatio)
    }
    //선택된 날의 핑계 리스트 출력
    private fun setExcuseLayoutAdapter() {

        excuseLayout.removeAllViews() //기존 뷰 제거

        val selectedDate = CalendarUtils.selectedDate
        var cursor : Cursor
        var query = "SELECT * FROM todoTBL " +
                "INNER JOIN excuseTBL ON todoTBL.todo_id = excuseTBL.todo_id " +
                "WHERE todoTBL.date_time LIKE '$selectedDate%' AND todoTBL.is_done == 0"
        cursor = sqlitedb.rawQuery(query, null)
        var num : Int = 0

        if (cursor.count == 0) {
            val noneExcuseTextView = TextView(this)
            noneExcuseTextView.text = "오늘은 핑계 없이 갓생을 사셨군요! ✨"
            noneExcuseTextView.setTextColor(Color.GRAY)
            noneExcuseTextView.gravity = Gravity.CENTER
            noneExcuseTextView.setPadding(0, 60, 0, 0)
            excuseLayout.addView(noneExcuseTextView)
        } else {
            while(cursor.moveToNext()){
                Log.d("DB_DEBUG", num.toString())
                var excuseId = cursor.getInt(cursor.getColumnIndexOrThrow("excuse_id"))
                var str_excuse = cursor.getString(cursor.getColumnIndexOrThrow("excuse_reason")).toString()
                var str_todo = cursor.getString(cursor.getColumnIndexOrThrow("todo_name")).toString()
                var isImportant = cursor.getInt(cursor.getColumnIndexOrThrow("is_important"))
                var str_time = cursor.getString(cursor.getColumnIndexOrThrow("date_time")).toString()

                var str_timeFormatted = try{
                    val time = str_time.trim().split(" ")[1]
                    val splitedTime = time.split(":")
                    val hour = splitedTime[0].toInt()
                    var minute = splitedTime[1].toInt()

                    //meridiem = am/pm 통칭
                    val meridiem = if(hour < 12) "오전" else "오후"
                    val hour2 = if(hour>12) hour - 12 else hour

                    "$meridiem $hour2:${String.format("%02d", minute)}"
                }catch (e : Exception){
                    str_time
                }

                //핑계 아이템 생성
                var excuseItem : LinearLayout = LinearLayout(this)
                excuseItem.orientation = LinearLayout.VERTICAL

                excuseItem.id = num
                excuseItem.setTag(str_excuse)
                excuseItem.setBackgroundResource(R.drawable.excuse_item_box)
                excuseItem.elevation=3F
                val param3 = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param3.setMargins(0,0,0,30)
                excuseItem.layoutParams = param3

                excuseItem.setOnClickListener {
                    val intent = Intent(this, AddExcuseActivity::class.java)
                    intent.putExtra("EXCUSE_ID", excuseId) // ID 전달
                    startActivity(intent)
                }

                var innerLayout : LinearLayout = LinearLayout(this)
                innerLayout.orientation= LinearLayout.VERTICAL
                innerLayout.setPadding(50,50,50,50)

                var todoLayout : LinearLayout = LinearLayout(this)
                todoLayout.orientation= LinearLayout.HORIZONTAL
                todoLayout.gravity = Gravity.CENTER_VERTICAL
                val param1 = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param1.setMargins(0,0,0,30)
                todoLayout.layoutParams = param1

                var todoLeftLayout : LinearLayout = LinearLayout(this)
                todoLeftLayout.orientation= LinearLayout.VERTICAL
                todoLayout.addView(todoLeftLayout)

                //중요 표시 생성
                var isImportantTextView : ImageView = ImageView(this)
                isImportantTextView.setImageResource(R.drawable.star_image)
                isImportantTextView.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_100))
                isImportantTextView.visibility = if (isImportant == 1) View.VISIBLE else View.INVISIBLE
                isImportantTextView.setPadding(0,0,10,0)
                todoLayout.addView(isImportantTextView)

                val leftParam = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                todoLeftLayout.layoutParams = leftParam

                val rightParam = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                isImportantTextView.layoutParams = rightParam

                //시간 텍스트뷰 생성
                var timeTextView : TextView = TextView(this)
                timeTextView.text = str_timeFormatted
                timeTextView.setTextColor(ContextCompat.getColor(this, R.color.gray_70))
                timeTextView.textSize=10F
                todoLeftLayout.addView(timeTextView)

                //할일 텍스트뷰 생성
                var todoTextView : TextView = TextView(this)
                todoTextView.setTextColor(ContextCompat.getColor(this, R.color.gray))
                todoTextView.text = str_todo
                todoTextView.textSize = 15F
                val textColor = if (isImportant == 1) R.color.blue_100 else R.color.gray
                todoTextView.setTextColor(ContextCompat.getColor(this, textColor))
                todoTextView.typeface = ResourcesCompat.getFont(this, R.font.paperlogy_semibold)
                todoLeftLayout.addView(todoTextView)

                innerLayout.addView(todoLayout)

                //구분선 생성
                val horizontalLineView = View(this)
                val param2 = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
                )
                param2.setMargins(0, 10, 0, 10)
                horizontalLineView.layoutParams = param2
                horizontalLineView.setBackgroundColor(Color.LTGRAY)
                innerLayout.addView(horizontalLineView)

                //핑계 텍스트뷰 생성
                var excuseTextView : TextView = TextView(this)
                excuseTextView.setTextColor(ContextCompat.getColor(this, R.color.gray))
                excuseTextView.text = str_excuse
                excuseTextView.textSize = 13F
                excuseTextView.setPadding(0,10,0,0)
                innerLayout.addView(excuseTextView)


                excuseItem.addView((innerLayout))
                excuseLayout.addView(excuseItem)
                num++
            }
        }


        cursor.close()
    }
}