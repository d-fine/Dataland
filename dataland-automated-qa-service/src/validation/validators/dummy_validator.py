import logging

from infrastructure.validator import ResourceValidator


class DummyValidator(ResourceValidator):
    def validate(self, resource):
        logging.info(f"Doing some dummy validation for resource: {resource}")