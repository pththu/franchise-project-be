import numpy as np
import struct

class VectorStore:
    def __init__(self, path: str):
        self.ids = []
        self.types = []
        self.v_core = []
        self.v_desc = []

        with open(path, "rb") as f:
            num_items = struct.unpack("<i", f.read(4))[0]
            dim = struct.unpack("<i", f.read(4))[0]

            print(f"[VectorStore] items={num_items}, dim={dim}")

            for idx in range(num_items):
                id_bytes = f.read(4)
                type_bytes = f.read(4)

                _id = struct.unpack("<i", id_bytes)[0]
                _type = struct.unpack("<i", type_bytes)[0]

                core_bytes = f.read(dim * 4)
                desc_bytes = f.read(dim * 4)

                v_core = np.frombuffer(core_bytes, dtype=np.float32)
                v_desc = np.frombuffer(desc_bytes, dtype=np.float32)

                self.ids.append(_id)
                self.types.append(_type)
                self.v_core.append(v_core)
                self.v_desc.append(v_desc)

        self.v_core = np.stack(self.v_core)   
        self.v_desc = np.stack(self.v_desc)   

        print("[VectorStore] Loaded OK:", len(self.ids))