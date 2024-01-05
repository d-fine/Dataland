class AutomaticQaNotPossibleError(Exception):
    def __init__(self, comment: str | None = None):
        self.comment = comment
