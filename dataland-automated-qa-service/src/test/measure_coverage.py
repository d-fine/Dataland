import unittest

from main.entrypoint import main


class MeasureCoverage(unittest.TestCase):
    def test_something(self) -> None:
        main()
        self.assertEqual(1, 2)
