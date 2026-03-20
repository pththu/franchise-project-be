# uvicorn service:app --host 0.0.0.0 --port 3012
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from semantic_search import Semantic_Search
from fastapi.middleware.cors import CORSMiddleware
from translate import translater
from recommend_system import RecommendationSystem
import uvicorn
import logging
import json
import os
import threading
import time
from log_config import setup_logging

setup_logging()
logger = logging.getLogger(__name__)

# ── Config persistence ────────────────────────────────────────────
CONFIG_PATH = os.path.join(os.path.dirname(__file__), "config", "ai_config.json")

DEFAULT_CONFIG = {
    "w_core": 0.4,
    "w_desc": 0.6,
    "schedule_enabled": False,
    "schedule_interval_hours": 24,
}

def load_config() -> dict:
    """Load config from file, fallback to defaults."""
    try:
        with open(CONFIG_PATH, "r", encoding="utf-8") as f:
            cfg = json.load(f)
            # Merge with defaults for any missing keys
            return {**DEFAULT_CONFIG, **cfg}
    except Exception:
        logger.warning(f"Cannot read {CONFIG_PATH}, using defaults.")
        return dict(DEFAULT_CONFIG)

def save_config(cfg: dict):
    """Persist config to JSON file."""
    os.makedirs(os.path.dirname(CONFIG_PATH), exist_ok=True)
    with open(CONFIG_PATH, "w", encoding="utf-8") as f:
        json.dump(cfg, f, indent=2, ensure_ascii=False)
    logger.info(f"Config saved: {cfg}")

# Load config on startup
ai_config = load_config()

# ── FastAPI App ───────────────────────────────────────────────────
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

class ConfigUpdate(BaseModel):
    w_core: float = None
    w_desc: float = None
    schedule_enabled: bool = None
    schedule_interval_hours: int = None

# ── Initialize services ──────────────────────────────────────────
semantic_search = Semantic_Search(model_name = "intfloat/multilingual-e5-base", vector_path = "Vector/vectors.txt")
recommend_system = RecommendationSystem(
    product_api="http://127.0.0.1:3001/product/getall",
    order_api="http://localhost:3007/api/orders",
)

# Apply weights from config on startup
semantic_search._setweight(ai_config["w_core"], ai_config["w_desc"])
logger.info(f"Applied weights from config: w_core={ai_config['w_core']}, w_desc={ai_config['w_desc']}")

# ── Scheduled Training ───────────────────────────────────────────
_schedule_thread = None
_schedule_stop = threading.Event()

def _schedule_loop():
    """Background loop that trains recommendation model on interval."""
    while not _schedule_stop.is_set():
        interval_hours = ai_config.get("schedule_interval_hours", 24)
        interval_seconds = interval_hours * 3600
        logger.info(f"[Scheduler] Sleeping {interval_hours}h before next train...")
        if _schedule_stop.wait(timeout=interval_seconds):
            break  # Stop event was set
        if not ai_config.get("schedule_enabled", False):
            continue
        logger.info("[Scheduler] Auto-training recommendation model...")
        try:
            recommend_system.train()
            logger.info("[Scheduler] Auto-training completed.")
        except Exception as e:
            logger.error(f"[Scheduler] Auto-training failed: {e}")

def start_scheduler():
    global _schedule_thread
    if _schedule_thread and _schedule_thread.is_alive():
        return
    _schedule_stop.clear()
    _schedule_thread = threading.Thread(target=_schedule_loop, daemon=True)
    _schedule_thread.start()
    logger.info("[Scheduler] Started.")

def stop_scheduler():
    global _schedule_thread
    _schedule_stop.set()
    if _schedule_thread:
        _schedule_thread.join(timeout=2)
    _schedule_thread = None
    logger.info("[Scheduler] Stopped.")

def restart_scheduler():
    stop_scheduler()
    if ai_config.get("schedule_enabled", False):
        start_scheduler()

# Start scheduler if enabled in config
if ai_config.get("schedule_enabled", False):
    start_scheduler()

# ── Endpoints ─────────────────────────────────────────────────────

@app.get("/api/ai/config")
def get_config():
    """Return current AI config (weights + schedule)."""
    return {
        "message": "Lấy config thành công",
        "data": {
            "w_core": ai_config["w_core"],
            "w_desc": ai_config["w_desc"],
            "schedule_enabled": ai_config["schedule_enabled"],
            "schedule_interval_hours": ai_config["schedule_interval_hours"],
        },
        "statusCode": 200,
    }

@app.post("/api/ai/config")
def update_config(cfg: ConfigUpdate):
    """Update AI config: weights and/or schedule settings."""
    updated_fields = []

    # Update weights
    if cfg.w_core is not None and cfg.w_desc is not None:
        # Validate: w_core + w_desc should equal 1.0 (with tolerance)
        total = cfg.w_core + cfg.w_desc
        if abs(total - 1.0) > 0.01:
            raise HTTPException(status_code=400, detail=f"w_core + w_desc phải bằng 1.0 (hiện tại = {total})")
        if cfg.w_core < 0 or cfg.w_desc < 0:
            raise HTTPException(status_code=400, detail="Weights không được âm")
        ai_config["w_core"] = round(cfg.w_core, 4)
        ai_config["w_desc"] = round(cfg.w_desc, 4)
        semantic_search._setweight(ai_config["w_core"], ai_config["w_desc"])
        updated_fields.extend(["w_core", "w_desc"])
        logger.info(f"Weights updated: w_core={ai_config['w_core']}, w_desc={ai_config['w_desc']}")
    elif cfg.w_core is not None or cfg.w_desc is not None:
        # Only one weight provided — apply both
        if cfg.w_core is not None:
            ai_config["w_core"] = round(cfg.w_core, 4)
            ai_config["w_desc"] = round(1.0 - cfg.w_core, 4)
        else:
            ai_config["w_desc"] = round(cfg.w_desc, 4)
            ai_config["w_core"] = round(1.0 - cfg.w_desc, 4)
        semantic_search._setweight(ai_config["w_core"], ai_config["w_desc"])
        updated_fields.extend(["w_core", "w_desc"])

    # Update schedule
    if cfg.schedule_enabled is not None:
        ai_config["schedule_enabled"] = cfg.schedule_enabled
        updated_fields.append("schedule_enabled")

    if cfg.schedule_interval_hours is not None:
        if cfg.schedule_interval_hours < 1:
            raise HTTPException(status_code=400, detail="Interval phải >= 1 giờ")
        ai_config["schedule_interval_hours"] = cfg.schedule_interval_hours
        updated_fields.append("schedule_interval_hours")

    # Persist and restart scheduler if needed
    save_config(ai_config)

    if "schedule_enabled" in updated_fields or "schedule_interval_hours" in updated_fields:
        restart_scheduler()

    return {
        "message": f"Cập nhật thành công: {', '.join(updated_fields)}",
        "data": {
            "w_core": ai_config["w_core"],
            "w_desc": ai_config["w_desc"],
            "schedule_enabled": ai_config["schedule_enabled"],
            "schedule_interval_hours": ai_config["schedule_interval_hours"],
        },
        "statusCode": 200,
    }

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
    result = semantic_search.update_vectors_store(db_url = "http://localhost:3000/api/products/getall")
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