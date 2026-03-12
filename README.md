안드로이드 스튜디오 프로젝트 폴더 파일

주요 액티비티 파일 및 프래그먼트 파일
- MainActivity : 홈/핑계 화면 컨테이너
    - HomeFragment : 홈(할일) 화면
    - ListFragment : 핑계 화면
- StatActivity : 통계 화면
- SettingActivity : 루틴 관리 화 -> (액티비티명 변경 필요)
- Setting2Activity : 카테고리 관리 화면 -> (액티비티명 변경 필요)

보조 액티비티 파일
- SplashActivity : 스플래시 화면(고전 방식 사용)
- AddTodoActivity : 할 일 작성 화면 -> (모달 변경 필요)
- AddExcuseActivity : 핑계 작성 화면 -> (모달 변경 필요)

공통 코드 파일
- DBManager : 데이터베이스 세팅
- CommonUIHolder : 상단, 하단 UI 세팅

액티비티 별 코드 파일
- CalendarAdapter, CalendaerUtil, CalendarViewHolder : MainActivty 보조
- ExcuseExtract : StatActivity 보조 

작성자 | 최수빈 
