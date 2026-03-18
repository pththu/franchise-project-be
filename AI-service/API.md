# AI Service API Documentation

> **Base URL:** `http://localhost:3012`
>
> **Khởi chạy:** `uvicorn service:app --host 0.0.0.0 --port 3012`

---

## Tổng quan

| # | Method | Endpoint | Mô tả |
|---|--------|----------|--------|
| 1 | POST | `/api/ai/search` | Tìm kiếm sản phẩm bằng ngữ nghĩa (semantic search) |
| 2 | POST | `/api/ai/update` | Cập nhật vector store từ dữ liệu sản phẩm |
| 3 | POST | `/api/ai/translate` | Dịch văn bản sang ngôn ngữ khác |
| 4 | POST | `/api/ai/recommendsystem` | Gợi ý sản phẩm cho khách hàng |
| 5 | POST | `/api/ai/recommend/train` | Train model gợi ý (chạy nền, không block) |
| 6 | POST | `/api/ai/recommend/train/sync` | Train model gợi ý (đồng bộ, block đến khi xong) |
| 7 | GET  | `/api/ai/recommend/status` | Xem trạng thái hiện tại của model |
| 8 | POST | `/api/ai/recommend/similar` | Tìm sản phẩm tương tự |

---

## Response Format chung

Mọi response thành công đều có cấu trúc:

```json
{
  "message": "Thông báo",
  "data": "...",
  "statusCode": 200
}
```

Lỗi trả về dạng HTTPException với `detail` mô tả nguyên nhân.

---

## Chi tiết các Endpoint

### 1. Tìm kiếm sản phẩm (Semantic Search)

```
POST /api/ai/search
```

**Mô tả:** Tìm kiếm sản phẩm dựa trên ngữ nghĩa văn bản, sử dụng mô hình `multilingual-e5-base` và Faiss vector search.

**Request Body:**

| Trường | Kiểu | Bắt buộc | Mặc định | Mô tả |
|--------|------|----------|----------|-------|
| `text` | string | ✅ | — | Từ khóa / câu tìm kiếm |
| `top_k` | int | ❌ | 5 | Số kết quả trả về |

**Ví dụ:**

```json
{
  "text": "trà sữa trân châu",
  "top_k": 5
}
```

**Response:**

```json
{
  "message": "Tìm kiếm thành công",
  "data": [ ... ],
  "statusCode": 200
}
```

---

### 2. Cập nhật Vector Store

```
POST /api/ai/update
```

**Mô tả:** Lấy toàn bộ sản phẩm từ Product Service, tính toán lại embedding vector và cập nhật vector store. Gọi khi dữ liệu sản phẩm thay đổi (thêm/sửa/xóa).

**Request Body:** Không có.

**Response:**

```json
{
  "message": "Cập nhật thành công / thất bại",
  "statusCode": 200
}
```

---

### 3. Dịch văn bản

```
POST /api/ai/translate
```

**Mô tả:** Dịch danh sách văn bản sang ngôn ngữ chỉ định, sử dụng Google Gemini API.

**Request Body:**

| Trường | Kiểu | Bắt buộc | Mặc định | Mô tả |
|--------|------|----------|----------|-------|
| `text_list` | list[string] | ✅ | — | Danh sách các đoạn văn bản cần dịch |
| `language` | string | ✅ | — | Ngôn ngữ đích (vd: `"en"`, `"en, jp, de"`) |

**Ví dụ:**

```json
{
  "text_list": ["Xin chào", "Cảm ơn"],
  "language": "en"
}
```

**Response:**

```json
{
  "message": "Dịch thành công",
  "data": ["Hello", "Thank you"],
  "statusCode": 200
}
```

---

### 4. Gợi ý sản phẩm cho khách hàng

```
POST /api/ai/recommendsystem
```

**Mô tả:** Trả về danh sách sản phẩm gợi ý cho một khách hàng cụ thể. Pipeline: ALS collaborative filtering → LightGBM ranking → Top-K. Nếu khách hàng mới (cold-start), trả về sản phẩm phổ biến.

**Request Body:**

| Trường | Kiểu | Bắt buộc | Mặc định | Mô tả |
|--------|------|----------|----------|-------|
| `customer_id` | string | ✅ | — | ID của khách hàng |
| `top_k` | int | ❌ | 10 | Số sản phẩm gợi ý |

**Ví dụ:**

```json
{
  "customer_id": "abc123",
  "top_k": 10
}
```

**Response:**

```json
{
  "message": "Gợi ý sản phẩm thành công",
  "data": [
    { "product_id": "prod_001", "score": 0.95 },
    { "product_id": "prod_002", "score": 0.87 }
  ],
  "statusCode": 200
}
```

> **Lưu ý:** Chỉ trả về `product_id` và `score`. Backend Product Service sẽ xử lý để lấy thông tin chi tiết sản phẩm.

---

### 5. Train model gợi ý (Async)

```
POST /api/ai/recommend/train
```

**Mô tả:** Khởi chạy quá trình train model ở background. API trả về ngay lập tức, model cũ vẫn tiếp tục phục vụ request trong khi model mới đang train. Khi train xong sẽ tự động hot-swap sang model mới.

**Request Body:** Không có.

**Response:**

```json
{
  "message": "Đang train model ở background...",
  "data": {
    "message": "Đang train model ở background...",
    "status": "training"
  },
  "statusCode": 200
}
```

---

### 6. Train model gợi ý (Sync)

```
POST /api/ai/recommend/train/sync
```

**Mô tả:** Train model đồng bộ — request sẽ block cho đến khi quá trình train hoàn tất. Phù hợp cho lần train đầu tiên hoặc khi cần chắc chắn model đã sẵn sàng.

**Request Body:** Không có.

**Response:**

```json
{
  "message": "Train mô hình thành công",
  "data": { ... },
  "statusCode": 200
}
```

> **Cảnh báo:** API này có thể mất vài phút tùy lượng dữ liệu.

---

### 7. Trạng thái model

```
GET /api/ai/recommend/status
```

**Mô tả:** Kiểm tra trạng thái hiện tại của recommendation model (đã train chưa, đang train không, thời gian train gần nhất...).

**Request Body:** Không có.

**Response:**

```json
{
  "message": "Trạng thái model",
  "data": {
    "trained": true,
    "is_training": false,
    "last_trained": "2026-03-12T10:30:00",
    "checkpoint_path": "models/recommend/checkpoint_20260312_103000.pkl"
  },
  "statusCode": 200
}
```

---

### 8. Sản phẩm tương tự

```
POST /api/ai/recommend/similar
```

**Mô tả:** Tìm các sản phẩm tương tự với một sản phẩm cho trước, dựa trên content-based similarity (Faiss vector search theo category, giá, độ phổ biến).

**Request Body:**

| Trường | Kiểu | Bắt buộc | Mặc định | Mô tả |
|--------|------|----------|----------|-------|
| `product_id` | string | ✅ | — | ID sản phẩm gốc |
| `top_k` | int | ❌ | 10 | Số sản phẩm tương tự |

**Ví dụ:**

```json
{
  "product_id": "prod_001",
  "top_k": 5
}
```

**Response:**

```json
{
  "message": "Tìm sản phẩm tương tự thành công",
  "data": [
    { "product_id": "prod_005", "score": 0.92 },
    { "product_id": "prod_012", "score": 0.88 }
  ],
  "statusCode": 200
}
```

---

## Yêu cầu hệ thống

- **Model cần được train trước** khi gọi `/recommendsystem` hoặc `/recommend/similar`. Nếu chưa train, hệ thống sẽ tự load checkpoint gần nhất (nếu có).
- **Vector store cần được cập nhật** khi dữ liệu sản phẩm thay đổi bằng cách gọi `/api/ai/update`.
- Các service phụ thuộc:
  - **Product Service:** `http://localhost:3001/product/getall`
  - **Order Service:** `http://localhost:3007/api/orders`
