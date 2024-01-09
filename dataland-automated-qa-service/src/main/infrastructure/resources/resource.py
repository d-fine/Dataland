class Resource:
    """
    This is the base class for any type of data that is fetched for review from other services
    """
    def __init__(self, resource_id: str):
        self.id = resource_id
        self._load()

    def _load(self):
        raise NotImplementedError
