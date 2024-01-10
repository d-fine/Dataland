from typing import Self


class Resource:
    """
    This is the base class for any type of data that is fetched for review from other services
    """

    def __init__(self: Self, resource_id: str) -> None:
        """
        Initializes an instance
        :param resource_id: the id of the resource in dataland
        """
        self.id = resource_id
        self._load()

    def _load(self: Self) -> None:
        raise NotImplementedError
