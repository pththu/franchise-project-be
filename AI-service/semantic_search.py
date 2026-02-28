from sentence_transformers import SentenceTransformer
from vector_store import VectorStore
import numpy as np
import requests

class Semantic_Search:
    def __init__(self, model_name = "intfloat/multilingual-e5-base", vector_path = "Vector/vectors.txt"):
        self.model = SentenceTransformer(model_name)
        self._check_load(vector_path)
        self.store = VectorStore(vector_path)


    def _check_load(self, path):

        if self.model is not None:
            print("Load thành công model")
        else:
            print("Model chưa load được")
        
        try:
            with open(path, "r") as f:
                print("Store Vectors check thành công !!!")
        except:
            print(f"Chưa có VectorStore!!! đang build lại.")
            self.update_vectors_store()

    def _search(self, 
                query_vec: np.ndarray, 
                store,
                top_k=5,
                w_core=0.6,
                w_desc=0.4):
        """
        query_vec: (768,) normalized

        return list [
                    {
                        "id": int(store.ids[i]),
                        "score": float(scores[i])
                    }
                    for i in idx
                ]
        """

        score_core = store.v_core @ query_vec
        score_desc = store.v_desc @ query_vec

        scores = w_core * score_core + w_desc * score_desc

        idx = np.argpartition(-scores, top_k)[:top_k]
        idx = idx[np.argsort(-scores[idx])]

        return [
            {
                "id": int(store.ids[i]),
                "score": float(scores[i])
            }
            for i in idx
        ]
    
    def _embedding_query(self, query):
        """
            query: string

            return query_vector (768,) (normalized) type: ndarray
        """

        embedding_text = f"query: {query}"

        return self.model.encode(embedding_text, normalize_embeddings=True)
    
    def _embedding_passage(self, text):
        """
            text: string

            return passage_vector (768,) (normalized) types ndarray
        """

        embedding_text = f"passage: {text}"

        return self.model.encode(embedding_text, normalize_embeddings=True)
    
    def search(self, query, top_k):
        """
            query: str
            top_k: int

            return [
                        {
                            "id":
                            "Type":
                            "score":
                        }, ...
                    ]
        """

        vector_search = self._embedding_query(query)
        results = self._search(vector_search, self.store, top_k)

        return results
    
    def update_vectors_store(self, db_url = "127.0.0.1:3001/product/getall"):
        """
            data: list(dict(products))

            return vectors_store.txt
        """
        try:
            respone = requests.get(db_url)
        except:
            return "Database không phản hồi!!! Chưa thể thực hiện Update"
        data = respone.json()

        vectors_store = []

        for item in data:
            desc_txt = f"passage: {item.description}"
            core_txt = f"passage: {item.name} được làm từ {', '.join(item.category)} có giá là {item.price}"

            desc_vec = self.model.encode(desc_txt, normalize_embeddings=True)
            core_vec = self.model.encode(core_txt, normalize_embeddings=True)

            vector = {
                        "id": item.id,
                        "v_core": core_vec,
                        "v_desc": desc_vec
                     }

            vectors_store.append(vector)
        
            with open("Vector/vectors.txt", "w") as f:
                f.write(len(vectors_store))  
                f.write(len(vectors_store[0]["v_core"]))  

                for it in vectors_store:
                    f.write(str(it["id"]) + "\n")
                    f.write(" ".join(str(x) for x in it["v_core"]) + "\n")
                    f.write(" ".join(str(x) for x in it["v_desc"]) + "\n")
            return "Đã Update Vector Store thành công !!!"