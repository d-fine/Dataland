from typing import List


class ResourceValidator:
    def validate(self, resource):
        raise NotImplementedError()


class ValidatorHolder:
    def __init__(self):
        self._validators: List[ResourceValidator] = []

    def register_data_validator(self, data_validator: ResourceValidator):
        self._validators.append(data_validator)

    def validate_resource(self, resource):
        for validator in self._validators:
            validator.validate(resource)
