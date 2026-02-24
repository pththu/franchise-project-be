# embedding.py
import torch
import numpy as np
from transformers import AutoTokenizer, AutoModel

MODEL = "intfloat/multilingual-e5-base"

tokenizer = AutoTokenizer.from_pretrained(MODEL)
model = AutoModel.from_pretrained(MODEL)
model.eval()

@torch.no_grad()
def embed_query(text: str) -> np.ndarray:
    text = f"query: {text}"

    tokens = tokenizer(
        text,
        return_tensors="pt",
        truncation=True,
        padding="max_length",
        max_length=64
    )

    out = model(**tokens)
    emb = out.last_hidden_state.mean(dim=1)[0].numpy()
    emb /= np.linalg.norm(emb)

    return emb.astype(np.float32)