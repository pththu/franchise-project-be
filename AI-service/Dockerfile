FROM python:3.10.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    HF_HOME=/app/.cache \
    TRANSFORMERS_CACHE=/app/.cache \
    TORCH_HOME=/app/.cache \
    TOKENIZERS_PARALLELISM=false

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libgomp1 \
    curl \
    tini \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .

RUN pip install --upgrade pip setuptools wheel \
    && pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu \
    && pip install --no-cache-dir -r requirements.txt 

# Preload model 
RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('intfloat/multilingual-e5-base')" \
    && rm -rf /root/.cache/pip

# Copy source sau cùng
COPY . .

RUN useradd --create-home --uid 10001 appuser \
    && mkdir -p /app/log /app/models/recommend /app/Vector /app/config /app/.cache \
    && chown -R appuser:appuser /app

USER appuser

EXPOSE 3012

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:3012/api/ai/config || exit 1

ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["uvicorn", "service:app", "--host", "0.0.0.0", "--port", "3012"]