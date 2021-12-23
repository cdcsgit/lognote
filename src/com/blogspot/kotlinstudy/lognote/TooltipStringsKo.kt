package com.blogspot.kotlinstudy.lognote

class TooltipStringsKo private constructor() {
    companion object {
        val STRINGS = listOf(
            "adb logcat 시작 및 로그 수신"
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
            , "체크된 경우 로그 뷰에 로그 유지함, 로그 검토시 적용된 라인수에 의해 로그가 밀려 올라갈 경우 사용"
            , "로그 화면 상하, 좌우 변경"
            , "로그 필터에서 대소문자 구분 여부"
            , "자주 사용하는 필터 저장"
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
//            , ""
        )
    }
}