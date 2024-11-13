# Setup IntelliJ

To make this subproject compatible with IntelliJ, there are a few steps to take:
- Install the Jetbrains Python plugin for IntelliJ
- Using `Ctrl + Shift + Alt + S` add the subproject as a submodule.
Select a python interpreter and set up a venv.
- Before applying the changes, in the dependencies tab, also select your created Python interpreter.
- Install the requirements listed in `./requirements.txt`. There should be an interactive banner when you open the file.

# Execution on host and setup for local unit testing

The following describes steps required to run the service on the host machine:
- You first need to make the modules for the clients to interact with the other Dataland services available:
  - Create the clients by running the `./generate_clients.sh` script. There should be a non-empty folder `./build/clients`
  - Create the modules from these clients: If you went with the venv setup as described above just run 
`pip install ./build/clients/<client_module>` (if your terminal's working directory is the subproject root) for each of the clients.
- Also, the package needs to be available for itself. Run `pip install -e .` to install the code as a package.
- To run the module locally you need to change some entries in `properties.py`:
  - `host` in the `ConnectionParameters` must be set to `"localhost"`
  - `document_manager_api_url` must be set to `"https://local-dev.dataland.com/documents"`
- Then run `./src/main/entrypoint.py` using the `python` command 
(e.g. `python ./src/main/entrypoint.py`, depending on your terminal's working directory)

# Auto Formatting (for Linter)

- running `ruff check --preview src` from /dataland-automated-qa-service will show you the linting messages
- Currently, you can run `ruff format --preview dataland-automated-qa-service/src` from the root project to automatically
fix some formatting issues and make the python linter happier
