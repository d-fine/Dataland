class AutomaticQaNotPossibleError(Exception):
    """
    This error is supposed to be raised during validation
    and indicates that an automatic review was not possible
    """

    def __init__(self, comment: str) -> None:
        """
        :param comment: details on the failure of the automatic review
        """
        self.comment = comment
