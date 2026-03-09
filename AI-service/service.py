# uvicorn service:app --host 0.0.0.0 --port 3012
from fastapi import FastAPI
from pydantic import BaseModel
from semantic_search import Semantic_Search
from fastapi.middleware.cors import CORSMiddleware
from translate import translater

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


semantic_search = Semantic_Search(model_name = "intfloat/multilingual-e5-base", vector_path = "Vector/vectors.txt")

@app.post("/api/ai/search")
def search_api(q: QuerySearch):
    if not q.text: 
        return {"Error": "Query không được để trống!"}
    else:
        results = semantic_search.search(q.text, q.top_k)
        return results

@app.post("/api/ai/update")
def update_api():
    result = semantic_search.update_vectors_store(db_url = "http://127.0.0.1:3001/product/getall")
    return result

@app.post("/api/ai/translate")
def translater_api(q: QueryTranslate):
    if not q.text_list : 
        return {"Error": "Trường Query không được để trống!"}
    elif not q.language:
        return {"Error": "Trường Language không được để trống!"}
    else:
        results = translater(q.text_list, q.language)
        return results