import unittest

from main.entrypoint import main


class MeasureCoverage(unittest.TestCase):
    def test_start_entrypoint(self):
        main()
        self.assertEqual(1, 2)
