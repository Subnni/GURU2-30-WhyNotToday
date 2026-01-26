package com.example.whynottoday

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CommonUIHandler {

    //상단 헤더
    private lateinit var logoImageButton : ImageButton
    private lateinit var settingImageButton : ImageButton
    //프로필 영역
//    private lateinit var pfpImageButton : ImageButton
    private lateinit var subGreetingTextView : TextView
    //하단 네비게이션 바
    private lateinit var homeMenuImageButton : ImageButton
    private lateinit var listMenuImageButton : ImageButton
    private lateinit var statMenuImageButton : ImageButton
    public fun setupListener(activity : Activity){

        //edge-to-edge 하단 UI 겹침 이슈 해결
        val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
        applyTopBottomPaddingForEdgeToEdge(rootView)

        //헤더 리스너 연결
        logoImageButton = activity.findViewById<ImageButton>(R.id.logoImageButton)
//        settingImageButton = activity.findViewById<ImageButton>(R.id.settingImageButton)

        logoImageButton.setOnClickListener {
            if (activity !is MainActivity) {
                val intent = Intent(activity, MainActivity::class.java)
                activity.startActivity(intent)
            }
        }
//        settingImageButton.setOnClickListener {
//            val intent = Intent(activity, ProfileActivity::class.java)
//            activity.startActivity(intent)
//        }

        //프로필 설정
//        subGreetingTextView = activity.findViewById<TextView>(R.id.subGreetingTextView)
//        val subGreetingText = when(activity){
//            is MainActivity -> "오늘 할 일을 알려드릴게요!"
//            is ListActivity -> "했던 핑계들을 살펴볼게요!"
//            is StatActivity -> "얼마나 해냈고, 왜 미뤘는지 확인해 보세요."
//            else -> ""
//        }
//        subGreetingTextView.text = subGreetingText

        //하단 네비게이션 바
        homeMenuImageButton = activity.findViewById<ImageButton>(R.id.homeMenuImageButton)
        listMenuImageButton = activity.findViewById<ImageButton>(R.id.listMenuImageButton)
        statMenuImageButton = activity.findViewById<ImageButton>(R.id.statMenuImageButton)

        val buttons = listOf(homeMenuImageButton, listMenuImageButton, statMenuImageButton)

        homeMenuImageButton.isSelected = activity is MainActivity
        listMenuImageButton.isSelected = activity is ListActivity
        statMenuImageButton.isSelected = activity is StatActivity

        buttons.forEach { button ->
            if (button.isSelected) {
                button.setColorFilter(Color.BLUE)
            } else {
                button.setColorFilter(Color.LTGRAY)
            }
        }

        homeMenuImageButton.setOnClickListener {
            if (activity !is MainActivity) activity.startActivity(Intent(activity, MainActivity::class.java))
        }
        listMenuImageButton.setOnClickListener {
            if (activity !is ListActivity) activity.startActivity(Intent(activity, ListActivity::class.java))
        }
        statMenuImageButton.setOnClickListener {
            if (activity !is StatActivity) activity.startActivity(Intent(activity, StatActivity::class.java))
        }
    }

    private fun applyTopBottomPaddingForEdgeToEdge(rootView: View) {

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            // 상태바와 내비게이션 바 높이 얻기
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val naviBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            // 키보드가 올라왔을 때 아래쪽 패딩 계산
            val bottomPadding = getBottomPaddingChangedByKeyboard(view, naviBarHeight)

            // 상단/하단 패딩 적용
            view.setPadding(0, 0, 0, bottomPadding)
            insets
        }
    }

    private fun getBottomPaddingChangedByKeyboard(view: View, naviBarHeight: Int): Int {
        // 화면에 키보드가 올라왔는지 확인하고, 올려졌으면 키보드 높이만큼 패딩 적용
        val rect = Rect().apply { view.rootView.getWindowVisibleDisplayFrame(this) }
        return if (isKeyBoardVisible(view.height, rect)) {
            view.height - rect.bottom
        } else {
            naviBarHeight
        }
    }

    private fun isKeyBoardVisible(screenHeight: Int, rect: Rect): Boolean {
        // 키보드가 화면에 올라왔는지 체크 (높이가 화면 높이의 13% 이상일 경우 키보드가 올라왔다고 판단)
        val keyboardHeight = screenHeight - rect.bottom
        return keyboardHeight > screenHeight * 0.13
    }


}