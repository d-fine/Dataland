#!/bin/bash

# Run Ruff formatter on dataland-automated-qa-service src
ruff format src

# more info about ruff here: https://github.com/astral-sh/ruff

# Install autopep8
pip install autopep8

# Run autopep8 on your project files
autopep8 --in-place --recursive --max-line-length 100 src