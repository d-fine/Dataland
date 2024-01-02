# Setup IntelliJ

To make this subproject compatible with IntelliJ, there are a few steps to do:
- Install the Jetbrains Python plugin for IntelliJ
- Using `Ctrl + Shift + Alt + S` add the subproject as a submodule.
Select a python interpreter and setup a venv.
- Before applying the changes, in the dependencies tab, also select your created Python interpreter.
- Install the requirements listed in `./requirements.txt`. There should be an interactive banner when you open the file.

# Execution

The following describes steps required to run the service on the host machine:
- You first need to make the modules for the clients to interact with the other Dataland services available:
  - Create the clients by running the `./generate_clients.sh` script. There should be a non-empty folder `./build/clients`
  - Create the modules from these clients: If you went with the venv setup as described above just run 
`pip install ./build/clients/<client_module>` (if your terminal's working directory is the subproject root) for each of the clients.
- To run the module locally run `./src/main/entrypoint.py` using the `python` command 
(e.g. `python ./src/main/entrypoint.py`, depending on your terminal's working directory)