from google import genai
import time
import logging

logger = logging.getLogger(__name__)


def translater(text_to_translate: list, target_language: str):
    client = genai.Client(api_key='AIzaSyCNZ3X2OEe1mBRMC71vtvuhuYW-vuogmdw')

    system_prompt = (
        "Bạn là một hệ thống dịch máy chính xác."

        "Nhiệm vụ:"
        "- Dịch từng phần tử trong danh sách input sang ngôn ngữ đích."

        "QUY TẮC BẮT BUỘC:"
        "1. Mỗi input chỉ được dịch thành DUY NHẤT 1 kết quả."
        "2. KHÔNG được tạo nhiều biến thể, KHÔNG paraphrase."
        "3. KHÔNG thêm từ, KHÔNG thêm lời chào, KHÔNG giải thích."
        "4. Giữ nguyên thứ tự input."
        "5. Output phải là danh sách ngăn cách nhau bởi dấu '|', mỗi phần tử tương ứng 1 input cho mỗi ngôn ngữ cần dịch."
        "6. Nếu input chỉ có 1 từ → chỉ trả về 1 từ/1 cụm từ tương đương."

        "FORMAT OUTPUT:"
        "Chỉ trả về chuỗi kết quả lần lượt từng ngôn ngữ theo thứ tự không thêm gì khác có dạng như sau:"
        "kết quả 1|kết quả 2|..."

        "Ví dụ:"
        "Input: hi, good morning"
        "language: en, de"
        "Output: xin chào|chào buổi sáng|hallo|guten morgen"
    )

    text = "| ".join(text_to_translate)

    logger.info(f"Translating {len(text_to_translate)} item(s) to '{target_language}'")
    start = time.time()

    response = client.models.generate_content(
        model="gemini-3.1-flash-lite-preview", 
        contents= f"{text} dịch tất cả sang ngôn ngữ {target_language}",
        config={
            "system_instruction": system_prompt,
            "temperature": 0.0, 
        }
    )

    response_api = response.text.split('|')
    print(response.text)

    end = time.time()
    logger.info(f"Translation completed in {end - start:.2f}s")

    return {"time response": end - start, "text": response_api}


if __name__ == "__main__":
    response = translater(["tôi là thực tập sinh", "tôi là sinh viên trường đại học FPT"], 'jp')
    print(response['time response'])
    print(response['text'])