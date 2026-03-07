package com.example.whynottoday

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CommonUIHandler {

    //상단 헤더 좌측 타이틀
    private lateinit var header : ConstraintLayout
//    private lateinit var logoImageButton : ImageButton
//    private lateinit var settingImageButton : ImageButton
    private lateinit var menuImageView : ImageView
    private lateinit var menutextView : TextView
    //상단 헤더 우측 토글
    private lateinit var toggle : LinearLayout
    private lateinit var toggleSwitch: androidx.appcompat.widget.SwitchCompat
    private lateinit var toggleImageView : ImageView
    private lateinit var toggleTextView : TextView

    //프로필 영역
//    private lateinit var pfpImageButton : ImageButton
//    private lateinit var subGreetingTextView : TextView
    //하단 네비게이션 바
    private lateinit var homeMenu : LinearLayout
    private lateinit var statMenu : LinearLayout
    private lateinit var settingMenu : LinearLayout
    private lateinit var setting2Menu : LinearLayout

    public fun setupListener(activity : Activity){

        //edge-to-edge 하단 UI 겹침 이슈 해결
        val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
        applyTopBottomPaddingForEdgeToEdge(rootView)

        //statActivity에서 헤더 elevation 제거
        header = activity.findViewById(R.id.header)
        if(activity is StatActivity)
            header.elevation = 0f

        //헤더 리스너 연결
//        logoImageButton = activity.findViewById<ImageButton>(R.id.menuImageButton)
//        settingImageButton = activity.findViewById<ImageButton>(R.id.settingImageButton)

//        logoImageButton.setOnClickListener {
//            if (activity !is MainActivity) {
//                val intent = Intent(activity, MainActivity::class.java)
//                activity.startActivity(intent)
//            }
//        }
//        settingImageButton.setOnClickListener {
//            val intent = Intent(activity, ProfileActivity::class.java)
//            activity.startActivity(intent)
//        }

        //액티비티 별 헤더 설정
        menuImageView = activity.findViewById(R.id.menuImageView)
        menutextView = activity.findViewById(R.id.menutextView)

        when(activity){
            is MainActivity -> {
                menuImageView.setImageResource(R.drawable.home_menu_image)
                menutextView.setText("홈")
            }
            is StatActivity -> {
                menuImageView.setImageResource(R.drawable.stat_menu_image)
                menutextView.setText("통계")
            }
            is SettingActivity -> {
                menuImageView.setImageResource(R.drawable.setting_menu_image)
                menutextView.setText("설정1")
            }
            is Setting2Activity -> {
                menuImageView.setImageResource(R.drawable.setting_menu_image)
                menutextView.setText("설정2")
            }
        }

        //3. MainActivity에서만 토글 활성화
        toggle = activity.findViewById(R.id.toggle)

        if (activity is MainActivity) {
            toggle.visibility = View.VISIBLE
        } else {
            toggle.visibility = View.GONE
        }

        //토글 설정
        toggleSwitch = activity.findViewById(R.id.toggleSwitch)
        toggleImageView = activity.findViewById(R.id.toggleImageView)
        toggleTextView = activity.findViewById(R.id.toggleTextView)


        toggleSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            val targetColor = ContextCompat.getColor(toggleImageView.context, if(isChecked) R.color.red else R.color.header_blue)
            val targetImage = if(isChecked) R.drawable.list_image else R.drawable.check_image
            val targetText = if(isChecked) "핑계" else "할일"
            val targetActivity = if(isChecked) StatActivity::class.java else MainActivity::class.java

            toggleImageView.apply {
                setImageResource(targetImage)
                setColorFilter(targetColor)
            }
            toggleTextView.apply{
                setText(targetText)
                setTextColor(targetColor)
            }

            menuImageView.setColorFilter(targetColor)
            menutextView.setTextColor(targetColor)

            activity.startActivity(Intent(activity, targetActivity))
        }

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
        homeMenu = activity.findViewById(R.id.homeMenu)
        statMenu = activity.findViewById(R.id.statMenu)
        settingMenu = activity.findViewById(R.id.settingMenu)
        setting2Menu = activity.findViewById(R.id.setting2Menu)

        //1. 화면 변경 시 해당 버튼을 선택된 상태 = blue 색상 변경 처리
        homeMenu.isSelected = activity is MainActivity
        statMenu.isSelected = activity is StatActivity
        settingMenu.isSelected = activity is SettingActivity
        setting2Menu.isSelected = activity is Setting2Activity

        //2. 각 버튼에 리스너 연결
        homeMenu.setOnClickListener {
            if (activity !is MainActivity) activity.startActivity(Intent(activity, MainActivity::class.java))
        }
        statMenu.setOnClickListener {
            if (activity !is StatActivity) activity.startActivity(Intent(activity, StatActivity::class.java))
        }
        settingMenu.setOnClickListener {
            if (activity !is SettingActivity) activity.startActivity(Intent(activity, SettingActivity::class.java))
        }
        setting2Menu.setOnClickListener {
            if (activity !is Setting2Activity) activity.startActivity(Intent(activity, Setting2Activity::class.java))
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