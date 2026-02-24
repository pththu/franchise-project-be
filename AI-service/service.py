# uvicorn service:app --host 0.0.0.0 --port 3012
from fastapi import FastAPI
from pydantic import BaseModel
from vector_store import VectorStore
from embedding import embed_query
from search import semantic_search
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],     
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

store = VectorStore("Vector/vectors.bin")

class Query(BaseModel):
    text: str
    top_k: int = 5

@app.post("/search")
def search_api(q: Query):
    q_vec = embed_query(q.text)
    results = semantic_search(q_vec, store, q.top_k)
    return results

