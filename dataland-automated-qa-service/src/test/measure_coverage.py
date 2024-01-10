import unittest
from typing import Self

from main.entrypoint import main


class MeasureCoverage(unittest.TestCase):
    def test_something(self: Self) -> None:
        main()
        self.assertEqual(1, 2)
