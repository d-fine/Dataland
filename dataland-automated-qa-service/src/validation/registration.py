from validation.validators.dummy_validator import DummyValidator
from infrastructure.validator import ValidatorHolder


def register_data_validators(data_validators: ValidatorHolder):
    data_validators.register_data_validator(DummyValidator())


def register_document_validators(document_validators: ValidatorHolder):
    document_validators.register_data_validator(DummyValidator())