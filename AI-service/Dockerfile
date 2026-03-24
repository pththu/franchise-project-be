FROM python:3.10.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    HF_HOME=/app/.cache/huggingface \
    HF_HUB_CACHE=/app/.cache/huggingface/hub \
    TRANSFORMERS_CACHE=/app/.cache/transformers \
    TORCH_HOME=/app/.cache/torch

WORKDIR /app

# Runtime packages for ML wheels + healthcheck.
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgomp1 \
    curl \
    tini \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt ./
RUN pip install --upgrade pip setuptools wheel && pip install -r requirements.txt

COPY . .

# Preload embedding model at build-time to avoid first-request download latency.
RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('intfloat/multilingual-e5-base')"

# Ensure runtime write paths exist and are writable by non-root user.
RUN useradd --create-home --uid 10001 appuser \
    && mkdir -p /app/log /app/models/recommend /app/Vector /app/config /app/.cache \
    && chown -R appuser:appuser /app

USER appuser

EXPOSE 3012

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:3012/api/ai/config || exit 1

ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["uvicorn", "service:app", "--host", "0.0.0.0", "--port", "3012"]
