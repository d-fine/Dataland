import unittest

from main.entrypoint import main


class MeasureCoverage(unittest.TestCase):
    def run_regular_entrypoint(self):
        main()
        self.assertEqual(1, 2)
