package com.blogspot.cdcsutils.lognote

class TooltipStringsKo private constructor() {
    companion object {
        val STRINGS = TooltipStrings.DEFAULT_STRINGS.toMutableMap()
        init {
            val stringList = listOf(
                "adb logcat 시작 및 로그 수신"
                , "adb logcat 일시 중지"
                , "adb logcat 중지"
                , "로그 화면 지움"
                , "새 파일 저장"
                , "연결된 장치 리스트 또는 연결할 주소 입력\n 엔터키(또는 ctrl-r) 입력시 재연결 후 로그 수신 시작함"
                , "adb connect [주소]"
                , "adb 연결된 장치 리스트를 가져옴"
                , "adb disconnect"
                , "로그 뷰에 유지할 로그 라인수(0 : 제한 없음)"
                , "체크된 경우 스크롤 라인수 마다 저장 파일 변경함"
                , "라인수와 파일 분할 적용"
                , "체크된 경우 로그 뷰에 로그 유지함\n로그 검토시 적용된 라인수에 의해 로그가 밀려 올라갈 경우 사용\n장시간 유지시 메모리 부족 현상 발생"
                , "로그 화면 상하, 좌우 변경"
                , "로그 필터에서 대소문자 구분 여부"
                , "자주 사용하는 필터 관리"
                , "전체 로그 필터"
                , "정규식 지원, 필터 : 검색, -필터 : 검색 제외, ex 필터1|필터2|-제외1"
                , "태그 필터"
                , "정규식 지원, 필터 : 검색, -필터 : 검색 제외, ex 필터1|필터2|-제외1"
                , "PID 필터"
                , "정규식 지원, 필터 : 검색, -필터 : 검색 제외, ex 필터1|필터2|-제외1"
                , "TID 필터"
                , "정규식 지원, 필터 : 검색, -필터 : 검색 제외, ex 필터1|필터2|-제외1"
                , "텍스를 진하게만 변경"
                , "정규식 지원 ex 텍스트1|텍스트2"
                , "첫번째 라인으로 이동"
                , "마지막 라인으로 이동"
                , "PID 강조"
                , "TID 강조"
                , "태그 강조"
                , "전체 로그 보이기"
                , "북마크만 보이기"
                , "뷰를 새창으로 이동"
                , "저장된 파일명"
                , "필터 리스트 열기(필터 추가)"
                , "명령 리스트 열기(명령 추가)"
                , "자주 사용하는 명령 관리"
                , "로그 수신 실패시 자동 재시도"
                , "파일 로그 읽기 모드에 대한 컨트롤 버튼 보이기/숨기기"
                , "파일에서 추가되는 로그를 계속 읽어들임"
                , "로그 추가를 멈춤"
                , "로그 명령 편집 : Setting > adb"
                , "정규식 지원 ex 텍스트1|텍스트2"
                , "검색시 대소문자 구분 여부"
                , "F3 : 이전 검색 항목으로 이동"
                , "F4 : 다음 검색 항목으로 이동"
                , "검색 중인 뷰 : 필터 로그 뷰, 전체 로그 뷰"
                , "ESC : 검색 바 닫기"
                , "숫자만 입력(0-9)"
                , "로그 포멧"
                , "로그 레벨"
                , "토큰 필터"
                , "정규식 지원, 필터 : 검색, -필터 : 검색 제외, ex 필터1|필터2|-제외1"
                , "토큰 강조"
                , "잘못된 포멧(숫자)"
                , "잘못된 이름(공백 허용 안됨)"
                , "로그를 가져올 패키지를 설정 합니다"
                , "이 패키지는 설치되어 있지 않아 적용할 수 없습니다"
                , "선택된 패키지는 logcat 을 시작할때 적용됩니다"
//            , ""
            )

            for ((idx, str) in stringList.withIndex()) {
                if (str.isNotEmpty()) {
                    STRINGS[idx.toString()] = str
                }
            }
        }
    }
}
