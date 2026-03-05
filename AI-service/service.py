# uvicorn service:app --host 0.0.0.0 --port 3012
from fastapi import FastAPI
from pydantic import BaseModel
from semantic_search import Semantic_Search
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],     
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class Query(BaseModel):
    text: str
    top_k: int = 5


semantic_search = Semantic_Search(model_name = "intfloat/multilingual-e5-base", vector_path = "Vector/vectors.txt")

@app.get("/AI")
def get_docs():
    return "/AI/search: semantic search " \
    "/AI/update: update vetor store for semantic search"

@app.post("/AI/search")
def search_api(q: Query):
    if not q.text: 
        return {"Error": "Query không được để trống!"}
    else:
        results = semantic_search.search(q.text, q.top_k)
        return results

@app.post("/AI/update")
def update_api():
    result = semantic_search.update_vectors_store(db_url = "http://127.0.0.1:3001/product/getall")
    return result
