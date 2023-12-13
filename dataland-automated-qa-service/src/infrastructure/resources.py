class Resource:
    def __init__(self, resource_id: str):
        self.id = resource_id
        self.content = None
        self._load()

    def _load(self):
        raise NotImplementedError


class DataResource(Resource):
    def _load(self):
        # TODO actually implement it
        print("TODO")
        self.whatever = None


class DocumentResource(Resource):
    def _load(self):
        # TODO actually implement it
        print("TODO")
