import numpy as np

class VectorStore:
    def __init__(self, path: str):
        self.ids = []
        self.v_core = []
        self.v_desc = []

        with open(path, "r") as f:
            num_items = int(f.readline())

            print(f"[VectorStore] items={num_items}")

            for idx in range(num_items):
                _id = int(f.readline())

                v_core_str = f.readline()
                v_core = np.fromstring(v_core_str, sep=' ')
                
                v_desc_str = f.readline().strip()
                v_desc = np.fromstring(v_desc_str, sep=' ')

                self.ids.append(_id)
                self.v_core.append(v_core)
                self.v_desc.append(v_desc)

        self.v_core = np.stack(self.v_core)   
        self.v_desc = np.stack(self.v_desc)   

        print("[VectorStore] Loaded OK:", len(self.ids))