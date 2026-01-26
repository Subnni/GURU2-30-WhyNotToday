package com.example.whynottoday

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
//import com.example.whynottoday.BuildConfig
import kotlinx.coroutines.launch

class StatActivity : AppCompatActivity() {

    //핑계 키워드 분석기
    //BuildConfig 임포트 안되는 문제로 하드코딩. 추후 수정 필요
    var GOOGLE_API_KEY ="AIzaSyDFvo6PNYi-PdmG7_lCDD06_LC8is8rNLg"
    private val excuseExtract = ExcuseExtract(GOOGLE_API_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        val extractExcusetextViewList = listOf<TextView>(
            findViewById(R.id.extractExcuseTextView1),
            findViewById(R.id.extractExcuseTextView2),
            findViewById(R.id.extractExcuseTextView3),
            findViewById(R.id.extractExcuseTextView4),
            findViewById(R.id.extractExcuseTextView5)
        )
        val inputText = "배가 너무 아파서 하기가 싫었음. 배가 고픈건지..아픈건지... \n머리가 아파서 못함 \n 몸이 안좋아서 뭘 하기가 힘들었음"
        lifecycleScope.launch {
            try {
                //텍스트뷰 숨기기
                for (textView in extractExcusetextViewList) {
                    textView.visibility = View.GONE
                }

                //키워드 추출
                val excuseList = excuseExtract.extract(inputText)
                //정렬(sort by count, descending)
                val sortedList = excuseList.sortedByDescending { group -> group.count }

                //top 5 뽑아 개수만큼 텍스트 보이기
                if (sortedList.isNotEmpty()){
                    val top5List = sortedList.take(5)
                    for(i in top5List.indices){
                        extractExcusetextViewList[i].visibility = View.VISIBLE
                        extractExcusetextViewList[i].text = "${i+1}위 : ${top5List[i].extract_text}"
                    }

                }
            }catch(e : Exception){
                extractExcusetextViewList[0].visibility = View.VISIBLE
                extractExcusetextViewList[0].text = "오류 발생: " + e.message
            }
        }

    }
}

