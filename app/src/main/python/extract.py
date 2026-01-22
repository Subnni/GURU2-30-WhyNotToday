import langextract as lx
import textwrap


def extractExcuse(api_key, input_text):
	MY_API_KEY = api_key
	
	# 1. Define the prompt and extraction rules
	prompt = textwrap.dedent("""\
	    문장들에서 '하지 못한 이유' 혹은 '핑계'를 추출하세요.
	    
	    규칙: 
	    1. 추출할 때는 "~해서", "~하느라", "~때문에"와 같이 
	    원문에서 이유를 포함한 자연스러운 문장 형태로 추출해주세요.
	    2. 추출된 핑계들 중 의미가 비슷하거나 중복되는 것들은 그룹화하세요.
	    (ex. '비가 와서', '날씨가 별로여서' -> '날씨가 안 좋아서'로 그룹화)
	
	    출력 형식:
	    각 extraction은 하나의 핑계 그룹을 의미합니다.
	    extraction_class는 excuse_group으로 설정하세요.
	    extraction_text에는 그룹화한 대표 핑계를 넣으세요. 
	    attributes에는 다음과 같은 형태로 원문 핑계를 넣으세요.
	    {
	        "members" : [원문 핑계1, 원문 핑계2, ...]    
	    } 
	    """)
	
	# 2. Provide a high-quality example to guide the model
	examples = [
	    lx.data.ExampleData(
	        text="비가 너무 많이 와서 안 감. 솔직히 귀찮기도 했고.. \n 날씨가 너무 별로였음. 더웠어",
	        extractions=[
	            lx.data.Extraction(
	                extraction_class="excuse_group",
	                extraction_text="날씨가 안 좋아서",
	                attributes={
	                    "members" : [
	                        "비가 너무 많이 와서" ,
	                        "날씨가 별로여서"
	                    ]
	                }
	            ),
	            lx.data.Extraction(
	                extraction_class="excuse_group",
	                extraction_text="귀찮아서",
	                attributes={
	                    "members" : ["귀찮아서"]
	                }
	            ),
			
	        ]
	    )
	]
	
	
	# Run the extraction
	result = lx.extract(
	    text_or_documents=input_text, #인풋값
	    prompt_description=prompt,
	    examples=examples,
	    api_key=MY_API_KEY,
	    model_id="gemini-2.5-flash",
	)
	
	# Generate the result	    
	excuse_list=[]
	for i in result.extractions:
	    count = len(i.attributes["members"]) #빈도
	    excuse_list.append({
            "excuse" : i.extraction_text,
            "count" : count,
            "members" : i.attributes["members"]
        })
	
	#sort by count
	excuse_list.sort(key=lambda x : x["count"], reverse=True)
	return excuse_list[0:5]

ak = "AIzaSyDFvo6PNYi-PdmG7_lCDD06_LC8is8rNLg"
it = "두쫀쿠 사러 가야 해서 어쩔 수 없었음 \n 몸이 안좋아서 뭘 하기가 힘들었음 \n 머리가 아픈데 타이레놀을 못찾겠어서 \n 배가 아파서 \n 버스가 안와서 \n 택시 타려 갈랬는데 돈이 없어서"
print("예시 문장:\n")
print(it)
print("분석 결과:\n")
print(extractExcuse(ak, it))

	