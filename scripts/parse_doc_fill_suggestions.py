#!/usr/bin/env python3
"""
Helper for scripts/transfer_doc_fill_suggestions.sh.

Reads the doc_fill_suggestions.csv file (columns: document_id, current_file_name,
suggested_file_name, current_publication_date, suggested_publication_date,
fields_filled) and emits one record per line to stdout, with fields separated by
the ASCII "unit separator" character (0x1F):

    document_id<US>fields_filled<US>suggested_file_name<US>suggested_publication_date

Using a real CSV parser (instead of naive comma splitting in bash) is necessary
because suggested_file_name values can themselves contain commas (the CSV quotes
such fields). The unit separator is used instead of a comma/tab so that
suggested_file_name values (which may contain arbitrary characters, but not the
unit separator) can safely be read back with a simple `IFS` split in bash.

Usage:
    python3 parse_doc_fill_suggestions.py <path-to-csv>
"""

import csv
import sys

UNIT_SEPARATOR = "\x1f"

EXPECTED_HEADER = [
    "document_id",
    "current_file_name",
    "suggested_file_name",
    "current_publication_date",
    "suggested_publication_date",
    "fields_filled",
]


def main() -> int:
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <path-to-csv>", file=sys.stderr)
        return 1

    csv_path = sys.argv[1]

    with open(csv_path, newline="", encoding="utf-8") as csv_file:
        reader = csv.reader(csv_file)
        try:
            header = next(reader)
        except StopIteration:
            print("CSV file is empty.", file=sys.stderr)
            return 1

        if header != EXPECTED_HEADER:
            print(
                f"Unexpected CSV header.\nExpected: {EXPECTED_HEADER}\nGot:      {header}",
                file=sys.stderr,
            )
            return 1

        out = sys.stdout
        for line_number, row in enumerate(reader, start=2):
            if len(row) != len(EXPECTED_HEADER):
                print(
                    f"Skipping malformed row at line {line_number}: {row}",
                    file=sys.stderr,
                )
                continue

            document_id, _current_file_name, suggested_file_name, _current_publication_date, \
                suggested_publication_date, fields_filled = row

            if UNIT_SEPARATOR in "".join(row):
                print(
                    f"Skipping line {line_number}: row contains the reserved unit "
                    "separator character (0x1F).",
                    file=sys.stderr,
                )
                continue

            fields_filled_set = {field.strip() for field in fields_filled.split(",")}

            if "fileName" in fields_filled_set and not suggested_file_name:
                print(
                    f"Skipping line {line_number}: fields_filled mentions fileName but "
                    "suggested_file_name is empty.",
                    file=sys.stderr,
                )
                continue
            if "publicationDate" in fields_filled_set and not suggested_publication_date:
                print(
                    f"Skipping line {line_number}: fields_filled mentions publicationDate but "
                    "suggested_publication_date is empty.",
                    file=sys.stderr,
                )
                continue

            record = UNIT_SEPARATOR.join(
                [document_id, fields_filled, suggested_file_name, suggested_publication_date]
            )
            out.write(record + "\n")

    return 0


if __name__ == "__main__":
    sys.exit(main())
