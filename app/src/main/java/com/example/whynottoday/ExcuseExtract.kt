package com.example.whynottoday

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


//사용자의 문장에서 "핑계" 키워드 추출
class ExcuseExtract(apiKey : String) {

    //google AI 모델
    val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )
    //JSON 변환기
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun extract(inputText: String) :  List<ExcuseGroup> {
        val prompt = """
            다음 문장들에서 '하지 못한 이유' 혹은 '핑계'를 추출해서 그룹화하세요.
            문장들은 \n으로 구분됩니다.
            
            반드시 아래의 JSON 형식으로만 응답하세요.
            설명은 생략하고 JSON 배열만 반환하세요.
            
            [형식]
            [
              {
                "extract_text": "대표 핑계",
                "members": ["원문1", "원문2", ...]
              }
            ]
            
            [입력 텍스트]
            $inputText
            
            [규칙]
            1. '~해서', '~하느라' 등 이유가 포함된 자연스러운 문장으로 추출할 것.
            2. 비슷한 핑계는 하나로 그룹화할 것.
            
            
            [예시]
            inputText : 비가 너무 많이 와서 안 감. 솔직히 귀찮기도 했고.. \n 날씨가 너무 별로였음. 더웠어  
            
            [
              {
                "extract_text": "날씨가 안 좋아서",
                "members": ["비가 너무 많이 와서", "날씨가 별로여서"],
              }
            ]
            
            [
              {
                "extract_text": "솔직히 너무 귀찮았음",
                "members": ["귀찮아서"],
              }
            ]

        """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text?.replace("```json", "")
                ?.replace("```", "")
                ?.trim() ?: ""
            return json.decodeFromString<List<ExcuseGroup>>(jsonText)
        } catch (e: Exception) {
            "분석 오류: ${e.message}"
            return emptyList() //빈 리스트
        }
    }

}

@Serializable
data class ExcuseGroup(
    val extract_text: String,    // 대표 핑계
    val members: List<String>,     // 핑계 목록
){
    val count: Int get() = members.size  // 개수
}