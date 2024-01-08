class AutomaticQaNotPossibleError(Exception):
    def __init__(self, comment: str):
        self.comment = comment
