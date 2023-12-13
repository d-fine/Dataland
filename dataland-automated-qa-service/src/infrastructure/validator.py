from typing import List


class AutomaticQaNotPossibleException(Exception):
    pass


class ResourceValidator:
    def validate(self, resource) -> str:  # todo return type should be QaStatus enum
        raise NotImplementedError()


class ValidatorHolder:
    def __init__(self):
        self._validators: List[ResourceValidator] = []

    def register_data_validator(self, data_validator: ResourceValidator):
        self._validators.append(data_validator)

    def validate_resource(self, resource):
        if len(self._validators) == 0:
            raise AutomaticQaNotPossibleException()
        for validator in self._validators:
            validator.validate(resource)
