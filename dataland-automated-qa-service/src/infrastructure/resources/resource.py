class Resource:
    def __init__(self, resource_id: str):
        self.id = resource_id
        self._load()

    def _load(self):
        raise NotImplementedError
