from pathlib import Path
import dataclasses
import re

base_folder = Path("C:\\Users\\d92582\\Downloads\\e2etests_testgroup_1")
log_files = {
    "backend": base_folder / "dala-e2e-test-backend-1.log",
    "internal_storage": base_folder / "dala-e2e-test-internal-storage-1.log"
}

log_text = {
    key: log_files[key].read_text() for key in log_files.keys()
}

@dataclasses.dataclass
class UploadCase:
    data_id: str
    data_type: str
    correlation_id: str
    is_qa_received: bool = dataclasses.field(init=False)
    is_stored_to_database: bool  = dataclasses.field(init=False)

    def __post_init__(self):
        self.is_qa_received = f"Received quality assurance: Accepted for data upload with DataId: {self.data_id}" in log_text["backend"]
        self.is_stored_to_database = f"Inserting data into database with data ID: {self.data_id} and correlation ID: {self.correlation_id}." in log_text["internal_storage"]

    def all_good(self) -> bool:
        return self.is_qa_received and self.is_stored_to_database

received_re = re.compile(r"Stored StorableDataSet of type '(?P<datatype>.*)' for company ID '(.*)' in temporary storage\. Data ID '(?P<dataid>.*)'\. Correlation ID: '(?P<corrid>.*)'\.")
matches = list(received_re.finditer(log_text["backend"]))
cases = [UploadCase(data_id=match.group("dataid"), data_type=match.group("datatype"), correlation_id=match.group("corrid")) for match in matches]


print(f"Found {len(matches)} uploads.")
print([case for case in cases if not case.all_good()])
