import requests
import pandas as pd
import numpy as np
from scipy.sparse import csr_matrix, save_npz, load_npz
from implicit.als import AlternatingLeastSquares
import lightgbm as lgb
from sklearn.preprocessing import LabelEncoder
from datetime import datetime, timezone
from typing import Optional
from dataclasses import dataclass
import logging
import faiss
import pickle
import json
import os
import shutil
import threading

logger = logging.getLogger(__name__)

MODELS_DIR = os.path.join(os.path.dirname(__file__), "models", "recommend")
MAX_SNAPSHOTS = 2


@dataclass
class ModelSnapshot:
    """Immutable bundle of all trained model artifacts. Used for atomic hot-swap."""
    products_df: pd.DataFrame
    orders_df: pd.DataFrame
    interactions_df: pd.DataFrame
    user_features_df: pd.DataFrame
    product_features_df: pd.DataFrame
    user_encoder: LabelEncoder
    product_encoder: LabelEncoder
    category_encoder: LabelEncoder
    als_model: AlternatingLeastSquares
    lgbm_model: Optional[lgb.Booster]
    user_item_matrix: csr_matrix
    faiss_index: faiss.IndexFlatIP
    product_content_vectors: np.ndarray
    snapshot_id: str


class RecommendationSystem:
    """
    Pipeline: ALS Candidate Generation → LightGBM Ranking → Top-K Recommendation
    - Persists trained models to disk (models/recommend/<snapshot_id>/)
    - Hot-swaps: old model keeps serving while new one trains
    - Keeps last 2 snapshots, prunes older ones
    """

    def __init__(
        self,
        product_api: str,
        order_api: str,
        als_factors: int = 64,
        als_iterations: int = 15,
        als_regularization: float = 0.1,
        candidate_k: int = 100,
    ):
        self.product_api = product_api
        self.order_api = order_api
        self.als_factors = als_factors
        self.als_iterations = als_iterations
        self.als_regularization = als_regularization
        self.candidate_k = candidate_k

        # Active snapshot (serves predictions)
        self._snapshot: Optional[ModelSnapshot] = None
        self._is_training = False
        self._lock = threading.Lock()

        # Try to load latest snapshot from disk on startup
        self._try_load_latest()

    # Snapshot Persistence
    def _snapshot_dir(self, snapshot_id: str) -> str:
        return os.path.join(MODELS_DIR, snapshot_id)

    def _save_snapshot(self, snap: ModelSnapshot):
        """Save all model artifacts to disk."""
        sdir = self._snapshot_dir(snap.snapshot_id)
        os.makedirs(sdir, exist_ok=True)
        logger.info(f"Saving snapshot to {sdir}")

        # DataFrames
        with open(os.path.join(sdir, "dataframes.pkl"), "wb") as f:
            pickle.dump({
                "products_df": snap.products_df,
                "orders_df": snap.orders_df,
                "interactions_df": snap.interactions_df,
                "user_features_df": snap.user_features_df,
                "product_features_df": snap.product_features_df,
            }, f)

        # Encoders
        with open(os.path.join(sdir, "encoders.pkl"), "wb") as f:
            pickle.dump({
                "user_encoder": snap.user_encoder,
                "product_encoder": snap.product_encoder,
                "category_encoder": snap.category_encoder,
            }, f)

        # ALS
        save_npz(os.path.join(sdir, "user_item_matrix.npz"), snap.user_item_matrix)
        with open(os.path.join(sdir, "als_model.pkl"), "wb") as f:
            pickle.dump(snap.als_model, f)

        # LightGBM
        if snap.lgbm_model is not None:
            snap.lgbm_model.save_model(os.path.join(sdir, "lgbm_model.txt"))

        # Faiss
        faiss.write_index(snap.faiss_index, os.path.join(sdir, "faiss.index"))
        np.save(os.path.join(sdir, "content_vectors.npy"), snap.product_content_vectors)

        # Metadata
        meta = {
            "snapshot_id": snap.snapshot_id,
            "n_products": len(snap.products_df),
            "n_users": snap.interactions_df["customer_id"].nunique() if snap.interactions_df is not None else 0,
            "n_interactions": len(snap.interactions_df) if snap.interactions_df is not None else 0,
            "created_at": datetime.now(timezone.utc).isoformat(),
        }
        with open(os.path.join(sdir, "metadata.json"), "w") as f:
            json.dump(meta, f, indent=2)

        # Update "latest" pointer
        latest_path = os.path.join(MODELS_DIR, "latest.txt")
        with open(latest_path, "w") as f:
            f.write(snap.snapshot_id)

        logger.info(f"Snapshot {snap.snapshot_id} saved.")
        self._prune_old_snapshots()

    def _load_snapshot(self, snapshot_id: str) -> ModelSnapshot:
        """Load a snapshot from disk."""
        sdir = self._snapshot_dir(snapshot_id)
        logger.info(f"Loading snapshot from {sdir}")

        with open(os.path.join(sdir, "dataframes.pkl"), "rb") as f:
            dfs = pickle.load(f)

        with open(os.path.join(sdir, "encoders.pkl"), "rb") as f:
            encs = pickle.load(f)

        user_item_matrix = load_npz(os.path.join(sdir, "user_item_matrix.npz"))

        with open(os.path.join(sdir, "als_model.pkl"), "rb") as f:
            als_model = pickle.load(f)

        lgbm_model = None
        lgbm_path = os.path.join(sdir, "lgbm_model.txt")
        if os.path.exists(lgbm_path):
            lgbm_model = lgb.Booster(model_file=lgbm_path)

        faiss_index = faiss.read_index(os.path.join(sdir, "faiss.index"))
        content_vectors = np.load(os.path.join(sdir, "content_vectors.npy"))

        snap = ModelSnapshot(
            products_df=dfs["products_df"],
            orders_df=dfs["orders_df"],
            interactions_df=dfs["interactions_df"],
            user_features_df=dfs["user_features_df"],
            product_features_df=dfs["product_features_df"],
            user_encoder=encs["user_encoder"],
            product_encoder=encs["product_encoder"],
            category_encoder=encs["category_encoder"],
            als_model=als_model,
            lgbm_model=lgbm_model,
            user_item_matrix=user_item_matrix,
            faiss_index=faiss_index,
            product_content_vectors=content_vectors,
            snapshot_id=snapshot_id,
        )
        logger.info(f"Snapshot {snapshot_id} loaded.")
        return snap

    def _try_load_latest(self):
        """On startup, load the latest snapshot if available."""
        latest_path = os.path.join(MODELS_DIR, "latest.txt")
        if not os.path.exists(latest_path):
            logger.info("No saved model snapshot found. Call /api/ai/recommend/train first.")
            return
        with open(latest_path, "r") as f:
            snapshot_id = f.read().strip()
        sdir = self._snapshot_dir(snapshot_id)
        if not os.path.isdir(sdir):
            logger.warning(f"Snapshot dir {sdir} not found despite latest.txt pointing to it.")
            return
        try:
            self._snapshot = self._load_snapshot(snapshot_id)
            logger.info(f"Loaded latest snapshot: {snapshot_id}")
        except Exception as e:
            logger.error(f"Failed to load snapshot {snapshot_id}: {e}")

    def _prune_old_snapshots(self):
        """Keep only the last MAX_SNAPSHOTS snapshots, delete older ones."""
        if not os.path.isdir(MODELS_DIR):
            return
        dirs = []
        for name in os.listdir(MODELS_DIR):
            path = os.path.join(MODELS_DIR, name)
            if os.path.isdir(path) and name.startswith("snap_"):
                dirs.append(name)
        dirs.sort(reverse=True)  # newest first (timestamp-based name)
        for old_dir in dirs[MAX_SNAPSHOTS:]:
            path = os.path.join(MODELS_DIR, old_dir)
            logger.info(f"Pruning old snapshot: {old_dir}")
            shutil.rmtree(path)

    # Data Loading 
    def _load_products(self) -> pd.DataFrame:
        all_products = []
        page = 0
        total_pages = 1

        while page < total_pages:
            paged_url = f"{self.product_api}?page={page}" if "?" not in self.product_api else f"{self.product_api}&page={page}"
            resp = requests.get(paged_url, timeout=15)
            resp.raise_for_status()
            json_data = resp.json()
            
            data = json_data.get("data", {})
            if isinstance(data, list):
                content = data
                total_pages = 1
            else:
                content = data.get("content", [])
                total_pages = data.get("totalPages", 1)
                
            for p in content:
                p_id = p.get("id")
                p_name = p.get("name", "")
                cat = p.get("category", {})
                cat_id = cat.get("id", p.get("categoryId", ""))
                cat_name = cat.get("name", p.get("categoryName", ""))
                p_type = p.get("productType", "")
                p_status = p.get("status", "ACTIVE")
                
                # Fetch price from variants if available
                variants = p.get("variants", [])
                active_prices = [v.get("price", 0) for v in variants if v.get("status") == "ACTIVE"]
                price = min(active_prices) if active_prices else p.get("price", 0)
                    
                desc = cat.get("description", p.get("description", ""))
                
                all_products.append({
                    "id": p_id,
                    "name": p_name,
                    "description": desc,
                    "price": price,
                    "productType": p_type,
                    "categoryId": cat_id,
                    "categoryName": cat_name,
                    "status": p_status
                })
            
            page += 1

        df = pd.DataFrame(all_products)
        if df.empty:
            df = pd.DataFrame(columns=["id", "name", "description", "price", "productType", "categoryId", "categoryName"])
            df.rename(columns={"id": "product_id", "productType": "product_type"}, inplace=True)
            return df
            
        df = df[df["status"] == "ACTIVE"].reset_index(drop=True)
        # Handle cases where there are no active products
        if df.empty:
            df = pd.DataFrame(columns=["id", "name", "description", "price", "productType", "categoryId", "categoryName"])
        else:
            df = df[["id", "name", "description", "price", "productType", "categoryId", "categoryName"]].copy()
            
        df.rename(columns={"id": "product_id", "productType": "product_type"}, inplace=True)
        return df

    def _load_orders(self) -> pd.DataFrame:
        resp = requests.get(self.order_api, timeout=10)
        resp.raise_for_status()
        payload = resp.json()
        
        data = payload.get("data", payload)
        if isinstance(data, dict) and "content" in data:
            orders = data.get("content", [])
        elif isinstance(data, list):
            orders = data
        else:
            orders = []
            
        rows = []
        for order in orders:
            # Drop cancelled or incomplete orders if needed, for instance focus on COMPLETED or PAID.
            status = order.get("orderStatus", "")
            if status not in ["COMPLETED", "PAID"]:
                continue
                
            customer_id = order.get("customerId")
            if not customer_id:
                continue
                
            created_at = order.get("createAt")
            for detail in order.get("orderDetails", []):
                rows.append({
                    "customer_id": customer_id,
                    "product_id": detail.get("productId"),
                    "quantity": detail.get("quantity", 1),
                    "cost": detail.get("cost", detail.get("priceSnapshot", 0)),
                    "created_at": created_at,
                })
                
        df = pd.DataFrame(rows)
        # Ensure correct column types and empty shape
        if df.empty:
            return pd.DataFrame(columns=["customer_id", "product_id", "quantity", "cost", "created_at"])
        return df

    # Feature Engineering
    @staticmethod
    def _build_interactions(orders_df: pd.DataFrame) -> pd.DataFrame:
        df = orders_df.copy()
        df["created_at"] = pd.to_datetime(df["created_at"], utc=True)
        now = datetime.now(timezone.utc)
        days = (now - df["created_at"]).dt.total_seconds() / 86400.0
        decay_lambda = 0.01
        df["weighted_qty"] = df["quantity"] * np.exp(-decay_lambda * days)
        
        df = df.groupby(["customer_id", "product_id"]).agg(interaction_weight=("weighted_qty", "sum")).reset_index()
        df["interaction"] = np.log1p(df["interaction_weight"])
        return df

    @staticmethod
    def _build_user_features(orders_df: pd.DataFrame, products_df: pd.DataFrame) -> pd.DataFrame:
        odf = orders_df.copy()
        odf["created_at"] = pd.to_datetime(odf["created_at"], utc=True)
        now = datetime.now(timezone.utc)

        user_agg = odf.groupby("customer_id").agg(
            total_purchase_count=("quantity", "sum"),
            average_spending=("cost", "mean"),
            last_purchase=("created_at", "max"),
        ).reset_index()
        user_agg["days_since_last_purchase"] = (now - user_agg["last_purchase"]).dt.total_seconds() / 86400.0
        user_agg.drop(columns=["last_purchase"], inplace=True)

        merged = odf.merge(products_df[["product_id", "categoryName"]], on="product_id", how="left")
        fav = (
            merged.groupby(["customer_id", "categoryName"])
            .agg(cat_count=("quantity", "sum"))
            .reset_index()
            .sort_values("cat_count", ascending=False)
            .drop_duplicates(subset=["customer_id"], keep="first")
            .rename(columns={"categoryName": "favorite_category"})
            [["customer_id", "favorite_category"]]
        )
        user_agg = user_agg.merge(fav, on="customer_id", how="left")
        return user_agg

    @staticmethod
    def _build_product_features(orders_df: pd.DataFrame, products_df: pd.DataFrame) -> pd.DataFrame:
        pop = (
            orders_df
            .groupby("product_id")
            .agg(product_popularity=("quantity", "count"))
            .reset_index()
        )
        pdf = products_df.merge(pop, on="product_id", how="left")
        pdf["product_popularity"] = np.log1p(pdf["product_popularity"].fillna(0)).astype(np.float32)
        return pdf

    # ALS
    def _train_als(self, interactions_df, user_encoder, product_encoder):
        df = interactions_df.copy()
        df["user_idx"] = user_encoder.fit_transform(df["customer_id"])
        df["item_idx"] = product_encoder.fit_transform(df["product_id"])

        n_users = df["user_idx"].nunique()
        n_items = df["item_idx"].nunique()

        user_item_matrix = csr_matrix(
            (df["interaction"].astype(np.float32), (df["user_idx"], df["item_idx"])),
            shape=(n_users, n_items),
        )

        als_model = AlternatingLeastSquares(
            factors=self.als_factors,
            iterations=self.als_iterations,
            regularization=self.als_regularization,
            random_state=42,
        )
        als_model.fit(user_item_matrix)
        return als_model, user_item_matrix

    @staticmethod
    def _get_als_candidates(als_model, user_item_matrix, user_idx: int, k: int) -> list[int]:
        ids, scores = als_model.recommend(
            user_idx, user_item_matrix[user_idx], N=k, filter_already_liked_items=True
        )
        return ids.tolist()

    # LightGBM
    @staticmethod
    def _get_lgbm_feature_cols() -> list[str]:
        return [
            "price", "product_popularity", "category_encoded",
            "total_purchase_count", "average_spending", "days_since_last_purchase",
            "fav_category_encoded", "user_bought_product_before",
            "user_bought_same_category", "user_product_affinity",
        ]

    @staticmethod
    def _build_pair_features(
        user_id, product_ids, user_features_df, product_features_df,
        interactions_df, products_df, als_model, user_encoder, product_encoder,
    ) -> pd.DataFrame:
        u_feat = user_features_df[user_features_df["customer_id"] == user_id]
        if u_feat.empty:
            u_feat = pd.DataFrame([{
                "customer_id": user_id, "total_purchase_count": 0,
                "average_spending": 0.0, "days_since_last_purchase": 9999,
                "favorite_category": "UNKNOWN",
            }])

        p_feat = product_features_df[product_features_df["product_id"].isin(product_ids)].copy()
        pairs = p_feat.assign(customer_id=user_id).merge(u_feat, on="customer_id", how="left")

        bought_products = set(
            interactions_df[interactions_df["customer_id"] == user_id]["product_id"].tolist()
        ) if interactions_df is not None else set()
        pairs["user_bought_product_before"] = pairs["product_id"].isin(bought_products).astype(int)

        bought_categories = set()
        if bought_products:
            bought_categories = set(
                products_df[products_df["product_id"].isin(bought_products)]["categoryName"].tolist()
            )
        pairs["user_bought_same_category"] = pairs["categoryName"].isin(bought_categories).astype(int)

        pairs["user_product_affinity"] = 0.0
        if als_model is not None and user_id in user_encoder.classes_:
            u_idx = user_encoder.transform([user_id])[0]
            u_vec = als_model.user_factors[u_idx]
            for i, pid in enumerate(pairs["product_id"].values):
                if pid in product_encoder.classes_:
                    p_idx = product_encoder.transform([pid])[0]
                    p_vec = als_model.item_factors[p_idx]
                    pairs.iloc[i, pairs.columns.get_loc("user_product_affinity")] = float(np.dot(u_vec, p_vec))
        return pairs

    @staticmethod
    def _encode_category_columns(df: pd.DataFrame, category_encoder: LabelEncoder) -> pd.DataFrame:
        df = df.copy()
        df["category_encoded"] = category_encoder.transform(
            df["categoryName"].fillna("UNKNOWN").apply(
                lambda x: x if x in category_encoder.classes_ else "UNKNOWN"
            )
        )
        df["fav_category_encoded"] = category_encoder.transform(
            df["favorite_category"].fillna("UNKNOWN").apply(
                lambda x: x if x in category_encoder.classes_ else "UNKNOWN"
            )
        )
        return df

    def _train_lgbm(self, products_df, interactions_df, user_features_df,
                     product_features_df, als_model, user_encoder,
                     product_encoder, category_encoder):
        all_product_ids = products_df["product_id"].tolist()
        all_users = interactions_df["customer_id"].unique().tolist()

        all_categories = list(products_df["categoryName"].dropna().unique()) + ["UNKNOWN"]
        category_encoder.fit(all_categories)

        rows = []
        group_sizes = []
        feature_cols = self._get_lgbm_feature_cols()

        for uid in all_users:
            bought = set(interactions_df[interactions_df["customer_id"] == uid]["product_id"].tolist())
            sample_neg = [p for p in all_product_ids if p not in bought]
            if len(sample_neg) > len(bought) * 3:
                sample_neg = list(np.random.choice(sample_neg, size=len(bought) * 3, replace=False))
            candidate_pids = list(bought) + sample_neg
            labels = [1] * len(bought) + [0] * len(sample_neg)

            pair_df = self._build_pair_features(
                uid, candidate_pids, user_features_df, product_features_df,
                interactions_df, products_df, als_model, user_encoder, product_encoder,
            )
            pair_df["label"] = labels[:len(pair_df)]
            rows.append(pair_df)
            group_sizes.append(len(pair_df))

        if not rows:
            logger.warning("No training data for LightGBM, skipping.")
            return None

        train_df = pd.concat(rows, ignore_index=True)
        train_df = self._encode_category_columns(train_df, category_encoder)

        X = train_df[feature_cols].fillna(0).values
        y = train_df["label"].values
        train_data = lgb.Dataset(X, label=y, group=group_sizes)

        params = {
            "objective": "lambdarank",
            "metric": "ndcg",
            "ndcg_eval_at": [5, 10],
            "learning_rate": 0.1,
            "num_leaves": 31,
            "min_data_in_leaf": 1,
            "verbose": -1,
        }
        return lgb.train(params, train_data, num_boost_round=100)

    # Faiss Content-Based Index
    @staticmethod
    def _build_faiss_index(product_features_df):
        pdf = product_features_df.copy()
        cat_dummies = pd.get_dummies(pdf["categoryName"], prefix="cat").astype(np.float32)
        price_norm = ((pdf["price"] - pdf["price"].mean()) / (pdf["price"].std() + 1e-8)).values.astype(np.float32).reshape(-1, 1)
        pop_norm = ((pdf["product_popularity"] - pdf["product_popularity"].mean()) / (pdf["product_popularity"].std() + 1e-8)).values.astype(np.float32).reshape(-1, 1)

        vectors = np.hstack([cat_dummies.values, price_norm, pop_norm])
        norms = np.linalg.norm(vectors, axis=1, keepdims=True) + 1e-8
        vectors = (vectors / norms).astype(np.float32)

        index = faiss.IndexFlatIP(vectors.shape[1])
        index.add(vectors)
        return index, vectors

    # Public API
    @property
    def is_ready(self) -> bool:
        return self._snapshot is not None

    @property
    def is_training(self) -> bool:
        return self._is_training

    def train(self) -> dict:
        """
        Train a new model from fresh data. Old model continues serving.
        Saves new snapshot to disk, then hot-swaps.
        """
        if self._is_training:
            return {"status": "already_training", "message": "Đang train, vui lòng đợi."}

        self._is_training = True
        try:
            return self._do_train()
        finally:
            self._is_training = False

    def train_async(self) -> dict:
        """Kick off training in a background thread. Returns immediately."""
        if self._is_training:
            return {"status": "already_training", "message": "Đang train, vui lòng đợi."}

        self._is_training = True

        def _bg():
            try:
                self._do_train()
            except Exception as e:
                logger.error(f"Background training failed: {e}")
            finally:
                self._is_training = False

        t = threading.Thread(target=_bg, daemon=True)
        t.start()
        return {"status": "training_started", "message": "Đang train ở background. Model cũ vẫn phục vụ."}

    def _do_train(self) -> dict:
        """Core training logic — runs on local variables, swaps atomically at the end."""
        snapshot_id = f"snap_{datetime.now(timezone.utc).strftime('%Y%m%d_%H%M%S')}"
        logger.info(f"[{snapshot_id}] Starting training...")

        # 1. Load data
        logger.info(f"[{snapshot_id}] Loading products...")
        products_df = self._load_products()
        logger.info(f"[{snapshot_id}] Loaded {len(products_df)} active products.")

        logger.info(f"[{snapshot_id}] Loading orders...")
        orders_df = self._load_orders()
        logger.info(f"[{snapshot_id}] Loaded {len(orders_df)} order detail rows.")

        if orders_df.empty:
            logger.warning(f"[{snapshot_id}] No order data. Cold-start only.")
            product_features_df = self._build_product_features(orders_df, products_df)
            # Still need a minimal product_features_df for popular fallback
            product_features_df["product_popularity"] = 0
            faiss_index, content_vectors = self._build_faiss_index(product_features_df)

            snap = ModelSnapshot(
                products_df=products_df, orders_df=orders_df,
                interactions_df=pd.DataFrame(), user_features_df=pd.DataFrame(),
                product_features_df=product_features_df,
                user_encoder=LabelEncoder(), product_encoder=LabelEncoder(),
                category_encoder=LabelEncoder(),
                als_model=None, lgbm_model=None, user_item_matrix=None,
                faiss_index=faiss_index, product_content_vectors=content_vectors,
                snapshot_id=snapshot_id,
            )
            self._snapshot = snap
            self._save_snapshot(snap)
            return {"status": "trained", "snapshot": snapshot_id, "note": "No order data, cold-start only."}

        # 2. Feature engineering
        logger.info(f"[{snapshot_id}] Building features...")
        interactions_df = self._build_interactions(orders_df)
        user_features_df = self._build_user_features(orders_df, products_df)
        product_features_df = self._build_product_features(orders_df, products_df)

        # 3. ALS
        user_encoder = LabelEncoder()
        product_encoder = LabelEncoder()
        logger.info(f"[{snapshot_id}] Training ALS...")
        als_model, user_item_matrix = self._train_als(interactions_df, user_encoder, product_encoder)

        # 4. LightGBM
        category_encoder = LabelEncoder()
        logger.info(f"[{snapshot_id}] Training LightGBM...")
        lgbm_model = self._train_lgbm(
            products_df, interactions_df, user_features_df, product_features_df,
            als_model, user_encoder, product_encoder, category_encoder,
        )

        # 5. Faiss
        logger.info(f"[{snapshot_id}] Building Faiss index...")
        faiss_index, content_vectors = self._build_faiss_index(product_features_df)

        # 6. Create snapshot & atomic swap
        snap = ModelSnapshot(
            products_df=products_df, orders_df=orders_df,
            interactions_df=interactions_df, user_features_df=user_features_df,
            product_features_df=product_features_df,
            user_encoder=user_encoder, product_encoder=product_encoder,
            category_encoder=category_encoder,
            als_model=als_model, lgbm_model=lgbm_model,
            user_item_matrix=user_item_matrix,
            faiss_index=faiss_index, product_content_vectors=content_vectors,
            snapshot_id=snapshot_id,
        )

        # Save to disk first
        self._save_snapshot(snap)

        # Hot-swap (atomic reference assignment in CPython)
        self._snapshot = snap
        logger.info(f"[{snapshot_id}] Training complete. Model swapped.")

        return {
            "status": "trained",
            "snapshot": snapshot_id,
            "n_products": len(products_df),
            "n_users": interactions_df["customer_id"].nunique(),
            "n_interactions": len(interactions_df),
        }

    def recommend(self, customer_id: str, top_k: int = 10) -> list[dict]:
        snap = self._snapshot
        if snap is None:
            raise RuntimeError("Model chưa được train. Gọi /api/ai/recommend/train trước.")

        is_known_user = (
            snap.interactions_df is not None
            and not snap.interactions_df.empty
            and customer_id in snap.interactions_df["customer_id"].values
        )

        # Cold start: new user
        if not is_known_user:
            return self._cold_start_recommend(snap, top_k)

        # Step 1: ALS candidates
        u_idx = snap.user_encoder.transform([customer_id])[0]
        candidate_idxs = self._get_als_candidates(
            snap.als_model, snap.user_item_matrix, u_idx, self.candidate_k,
        )
        candidate_pids = snap.product_encoder.inverse_transform(candidate_idxs).tolist()
        active_ids = set(snap.products_df["product_id"].tolist())
        candidate_pids = [p for p in candidate_pids if p in active_ids]

        if not candidate_pids:
            return self._cold_start_recommend(snap, top_k)

        # Step 2: LightGBM ranking
        pairs = self._build_pair_features(
            customer_id, candidate_pids, snap.user_features_df,
            snap.product_features_df, snap.interactions_df, snap.products_df,
            snap.als_model, snap.user_encoder, snap.product_encoder,
        )
        pairs = self._encode_category_columns(pairs, snap.category_encoder)

        X = pairs[self._get_lgbm_feature_cols()].fillna(0).values
        if snap.lgbm_model is not None:
            pairs["score"] = snap.lgbm_model.predict(X)
        else:
            pairs["score"] = pairs["user_product_affinity"]

        pairs["strategy"] = "als_lgbm"
        return self._format_output(pairs, top_k)

    def recommend_similar(self, product_id: str, top_k: int = 10) -> list[dict]:
        snap = self._snapshot
        if snap is None:
            raise RuntimeError("Model chưa được train. Hãy train model trước.")

        if snap.faiss_index is None:
            return []
        mask = snap.product_features_df["product_id"] == product_id
        if not mask.any():
            return []
        idx = mask.values.nonzero()[0][0]
        query = snap.product_content_vectors[idx].reshape(1, -1)
        distances, indices = snap.faiss_index.search(query, top_k + 1)
        similar_ids = [
            snap.product_features_df.iloc[i]["product_id"]
            for i in indices[0]
            if i != idx and 0 <= i < len(snap.product_features_df)
        ][:top_k]

        if not similar_ids:
            return []
        results = snap.product_features_df[snap.product_features_df["product_id"].isin(similar_ids)].copy()
        results["score"] = range(len(results), 0, -1)
        results["strategy"] = "content_based_similarity"
        return self._format_output(results, top_k)

    def get_status(self) -> dict:
        """Return current model status."""
        snap = self._snapshot
        return {
            "is_ready": snap is not None,
            "is_training": self._is_training,
            "current_snapshot": snap.snapshot_id if snap else None,
        }

    # Helpers
    @staticmethod
    def _cold_start_recommend(snap: ModelSnapshot, top_k: int) -> list[dict]:
        pdf = snap.product_features_df
        results = pdf.sort_values("product_popularity", ascending=False).head(top_k).copy()
        results["score"] = range(len(results), 0, -1)
        results["strategy"] = "cold_start_popular"
        return RecommendationSystem._format_output(results, top_k)

    @staticmethod
    def _format_output(df: pd.DataFrame, top_k: int) -> list[dict]:
        df = df.sort_values("score", ascending=False).head(top_k)
        return [
            {
                "product_id": row.get("product_id", ""),
                "score": float(row.get("score", 0)),
                "strategy": row.get("strategy", ""),
            }
            for _, row in df.iterrows()
        ]