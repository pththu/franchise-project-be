from sentence_transformers import SentenceTransformer
from vector_store import VectorStore
import numpy as np
import requests
import logging

logger = logging.getLogger(__name__)

class Semantic_Search:
    def __init__(self, model_name = "intfloat/multilingual-e5-base", vector_path = "Vector/vectors.txt"):
        self.model = SentenceTransformer(model_name)
        self._check_load(vector_path)
        self.store = VectorStore(vector_path)
        self.w_core = 0.4
        self.w_desc = 0.6

    def _setweight(self, w_core, w_desc):
        self.w_core = w_core
        self.w_desc = w_desc

    def _check_load(self, path):

        if self.model is not None:
            logger.info("Load thành công model")
        else:
            logger.error("Model chưa load được")
        
        try:
            with open(path, "r") as f:
                logger.info("Store Vectors check thành công !!!")
        except:
            logger.warning(f"Chưa có VectorStore!!! đang build lại.")
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

        if top_k == -1:
            top_k = len(scores)

        idx = np.argpartition(-scores, top_k)[:top_k]
        idx = idx[np.argsort(-scores[idx])]

        return [
            {
                "id": str(store.ids[i]),
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
        results = self._search(vector_search, self.store, top_k, w_core= self.w_core, w_desc= self.w_desc)

        return results
    
    def update_vectors_store(self, db_url):
        """
            Fetch paginated products and update vectors
        """
        vectors_store = []
        page = 0
        total_pages = 1

        while page < total_pages:
            try:
                paged_url = f"{db_url}?page={page}" if "?" not in db_url else f"{db_url}&page={page}"
                respone = requests.get(paged_url, timeout=15)
                respone.raise_for_status()
                json_data = respone.json()
            except Exception as e:
                logger.error(f"Lỗi gọi API semantic search: {e}")
                return "Database không phản hồi!!! Chưa thể thực hiện Update"

            data = json_data.get("data", {})
            if isinstance(data, list):
                content = data
                total_pages = 1
            else:
                content = data.get("content", [])
                total_pages = data.get("totalPages", 1)

            for item in content:
                try:
                    name = item.get("name", "")
                    brand = item.get("brand", "")
                    cat = item.get("category", {})
                    cat_name = cat.get("name", item.get("categoryName", ""))
                    cat_desc = cat.get("description", item.get("description", ""))

                    variants = item.get("variants", [])
                    active_variants = [v for v in variants if v.get("status") == "ACTIVE"]
                    if not active_variants:
                        active_variants = variants

                    color_map = {
                        "black": "đen",
                        "white": "trắng",
                        "red": "đỏ",
                        "blue": "xanh"
                    }

                    colors = set()
                    sizes = set()
                    prices = []

                    for v in active_variants:
                        c = str(v.get("color", "")).strip().lower()
                        if c and c != "none":
                            colors.add(color_map.get(c, c))
                        
                        s = str(v.get("size", "")).strip().lower()
                        if s and s != "none":
                            sizes.add(s)

                        p = v.get("price")
                        if p is not None:
                            try:
                                prices.append(float(p))
                            except ValueError:
                                pass

                    if not prices and item.get("price") is not None:
                        try:
                            prices.append(float(item.get("price")))
                        except ValueError:
                            pass

                    # Build core_txt
                    core_parts = [f"passage: {name}.", f"Danh mục: {cat_name}."]
                    if colors:
                        core_parts.append(f"Màu sắc: {', '.join(sorted(colors))}.")
                    if sizes:
                        core_parts.append(f"Kích thước: {', '.join(sorted(sizes))}.")
                    
                    if prices:
                        price_min = min(prices)
                        price_max = max(prices)
                        pmin_str = f"{int(price_min)}" if price_min.is_integer() else f"{price_min}"
                        pmax_str = f"{int(price_max)}" if price_max.is_integer() else f"{price_max}"
                        
                        if price_min == price_max:
                            core_parts.append(f"Giá {pmin_str}.")
                        else:
                            core_parts.append(f"Giá từ {pmin_str} đến {pmax_str}.")

                    core_txt = "\n".join(core_parts)
                    
                    # Build desc_txt
                    desc_parts = [f"passage: {cat_desc}."]
                    if brand:
                        desc_parts.append(f"Thương hiệu: {brand}.")
                    desc_txt = " ".join(desc_parts)

                except Exception as e:
                    logger.warning(f"Lỗi tạo passage: {e}")
                    continue
                
                desc_vec = self.model.encode(desc_txt, normalize_embeddings=True)
                core_vec = self.model.encode(core_txt, normalize_embeddings=True)

                vector = {
                            "id": item["id"],
                            "v_core": core_vec,
                            "v_desc": desc_vec
                         }

                vectors_store.append(vector)
            
            page += 1

        with open("Vector/vectors.txt", "w") as f:
            f.write(str(len(vectors_store)) + "\n")

            for it in vectors_store:
                f.write(str(it["id"]) + "\n")
                f.write(" ".join(str(x) for x in it["v_core"]) + "\n")
                f.write(" ".join(str(x) for x in it["v_desc"]) + "\n")
                
        self.store = VectorStore("Vector/vectors.txt")
                
        return "Đã Update Vector Store thành công !!!"