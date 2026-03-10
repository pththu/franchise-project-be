from google import genai
import time


def translater(text_to_translate: list, target_language: str):
    client = genai.Client(api_key='AIzaSyCx_YyRCxZMPrjj8vol_a4rviST19URW_o')

    system_prompt = (
        "Bạn là một người phiên dịch chuyên nghiệp. "
        "Nhiệm vụ: Dịch văn bản tiếng việt sang các ngôn ngữ khác. "
        "Quy tắc: Giữ đúng thứ tự, không giải thích, không thêm lời chào, Nếu có nhiều ngôn ngữ cần làm thì làm lần lượt từng ngôn ngữ và trả về trên 1 dòng phân chia bởi dấu ','."
        "Chỉ trả về nội dung đã dịch."
    )

    text = ", ".join(text_to_translate)

    start = time.time()

    response = client.models.generate_content(
        model="gemini-3.1-flash-lite-preview", 
        contents= f"{text} dịch tất cả sang ngôn ngữ {target_language}",
        config={
            "system_instruction": system_prompt,
            "temperature": 0.0, 
        }
    )

    response_api = response.text.split(',')

    end = time.time()

    return {"time response": end - start, "text": response_api}


if __name__ == "__main__":
    response = translater(["tôi là thực tập sinh", "tôi là sinh viên trường đại học FPT"], 'jp')
    print(response['time response'])
    print(response['text'])