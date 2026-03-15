# uvicorn service:app --host 0.0.0.0 --port 3012
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from semantic_search import Semantic_Search
from fastapi.middleware.cors import CORSMiddleware
from translate import translater
from recommend_system import RecommendationSystem
import uvicorn
import logging
from log_config import setup_logging

setup_logging()
logger = logging.getLogger(__name__)

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],     
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class QuerySearch(BaseModel):
    text: str
    top_k: int = 5

class QueryTranslate(BaseModel):
    text_list: list
    language: str

class QueryRecommend(BaseModel):
    customer_id: str
    top_k: int = 10

class QuerySimilar(BaseModel):
    product_id: str
    top_k: int = 10


semantic_search = Semantic_Search(model_name = "intfloat/multilingual-e5-base", vector_path = "Vector/vectors.txt")
recommend_system = RecommendationSystem(
    product_api="http://127.0.0.1:3001/product/getall",
    order_api="http://localhost:3007/api/orders",
)

@app.post("/api/ai/search")
def search_api(q: QuerySearch):
    if not q.text:
        logger.warning("Search request with empty query")
        raise HTTPException(status_code=400, detail="Query không được để trống!")
    logger.info(f"Search: text='{q.text}', top_k={q.top_k}")
    results = semantic_search.search(q.text, q.top_k)
    logger.info(f"Search returned {len(results)} results")
    return {"message": "Tìm kiếm thành công", "data": results, "statusCode": 200}

@app.post("/api/ai/update")
def update_api():
    logger.info("Updating Vector Store...")
    result = semantic_search.update_vectors_store(db_url = "http://127.0.0.1:3001/product/getall")
    logger.info(f"Vector Store update result: {result}")
    return {"message": result, "statusCode": 200}

@app.post("/api/ai/translate")
def translater_api(q: QueryTranslate):
    if not q.text_list:
        logger.warning("Translate request with empty text_list")
        raise HTTPException(status_code=400, detail="Trường Query không được để trống!")
    if not q.language:
        logger.warning("Translate request with empty language")
        raise HTTPException(status_code=400, detail="Trường Language không được để trống!")
    logger.info(f"Translate: {len(q.text_list)} item(s) to '{q.language}'")
    results = translater(q.text_list, q.language)
    logger.info("Translate completed")
    return {"message": "Dịch thành công", "data": results, "statusCode": 200}
    
@app.post("/api/ai/recommendsystem")
def recommendsystem(q: QueryRecommend):
    logger.info(f"Recommend: customer_id='{q.customer_id}', top_k={q.top_k}")
    try:
        results = recommend_system.recommend(q.customer_id, q.top_k)
        logger.info(f"Recommend returned {len(results)} products")
        return {"message": "Gợi ý sản phẩm thành công", "data": results, "statusCode": 200}
    except RuntimeError as e:
        logger.error(f"Recommend failed: {e}")
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/api/ai/recommend/train")
def recommend_train():
    logger.info("Training recommendation model (background)...")
    try:
        result = recommend_system.train_async()
        logger.info(f"Train request result: {result}")
        return {"message": result["message"], "data": result, "statusCode": 200}
    except Exception as e:
        logger.error(f"Training failed: {e}")
        raise HTTPException(status_code=500, detail=f"Lỗi khi train mô hình: {e}")

@app.post("/api/ai/recommend/train/sync")
def recommend_train_sync():
    """Synchronous training — blocks until done."""
    logger.info("Training recommendation model (sync)...")
    try:
        result = recommend_system.train()
        logger.info(f"Training completed: {result}")
        return {"message": "Train mô hình thành công", "data": result, "statusCode": 200}
    except Exception as e:
        logger.error(f"Training failed: {e}")
        raise HTTPException(status_code=500, detail=f"Lỗi khi train mô hình: {e}")

@app.get("/api/ai/recommend/status")
def recommend_status():
    status = recommend_system.get_status()
    return {"message": "Trạng thái model", "data": status, "statusCode": 200}

@app.post("/api/ai/recommend/similar")
def recommend_similar(q: QuerySimilar):
    logger.info(f"Similar products: product_id='{q.product_id}', top_k={q.top_k}")
    try:
        results = recommend_system.recommend_similar(q.product_id, q.top_k)
        logger.info(f"Similar products returned {len(results)} results")
        return {"message": "Tìm sản phẩm tương tự thành công", "data": results, "statusCode": 200}
    except RuntimeError as e:
        logger.error(f"Similar products failed: {e}")
        raise HTTPException(status_code=400, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=3012)