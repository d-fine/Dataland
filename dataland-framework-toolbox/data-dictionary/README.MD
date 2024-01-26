# Data Dictionary XLSX conversion
The definitive source of truth for data-dictionaries is the `dataDictionary.xlsx` file on the Dataland teams.
You should follow these steps to obtain framework-specific CSV files for the framework toolbox:

1. Download a copy of the data dictionary XLSX file and paste it into this folder.
The `.gitignore` file ensures that no XLSX file is accidentally committed to git
2. Execute `./convert-data-dictionary.sh` to trigger the conversion. The generated files are put in the
`csv` directory which is deleted before re-execution.

Any framework-toolbox framework should directly reference the generated CSV files.
When you add or migrate a new framework, remember to add an exception for the CSV to the `.gitignore` file.
