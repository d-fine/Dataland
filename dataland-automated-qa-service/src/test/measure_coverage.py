import unittest

from main.entrypoint import main


class MeasureCoverage(unittest.TestCase):
    def test_entrypoint(self) -> None:  # noqa: PLR6301
        main()
