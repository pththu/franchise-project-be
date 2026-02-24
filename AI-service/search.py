import numpy as np

def semantic_search(
    query_vec: np.ndarray,
    store,
    top_k=5,
    w_core=0.6,
    w_desc=0.4
):
    """
    query_vec: (768,) normalized
    """

    score_core = store.v_core @ query_vec
    score_desc = store.v_desc @ query_vec

    scores = w_core * score_core + w_desc * score_desc

    idx = np.argpartition(-scores, top_k)[:top_k]
    idx = idx[np.argsort(-scores[idx])]

    return [
        {
            "id": int(store.ids[i]),
            "type": int(store.types[i]),
            "score": float(scores[i])
        }
        for i in idx
    ]