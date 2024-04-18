import logging

from typing_extensions import override

from main.infrastructure.resources import Resource


class UnloadedResource(Resource):
    """
    This class represents a dataset
    """

    @override
    def _load(self) -> None:
        logging.info(f"Loading nothing for resource with ID {self.id}")
