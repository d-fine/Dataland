# Developer Tools
The scripts in this folder are intended for special developer tasks occurring only rarely. The scripts present in this
folder are not subject to the same quality standards as the rest of the code. Please keep that in mind when using them.

## Running a reduced local stack

Often it is not required to run all services during local development. To reduce load and speed up the local startup 
time, you can run a reduced local stack by following the instructions below:

1. Determine which services you do not require and set them to false in the ``ReducedStack.kt`` script. There are 
   certain service that are always required and are marked as such in the script. The services set to true by default 
   represent a reduced service setup that covers the most common use cases. 
2. Run the ``ReducedStack.kt`` script from the IDE or via gradle.
3. The script will overwrite certain nginx configuration files and the ``localContainer.conf``. Do not commit these or 
   the changes made to the ``ReducedStack.kt`` file!
4. Start the local stack by running ``startDevelopmentStack.sh`` or ``resetDevelopmentStack.sh``. The script will 
   automatically detect the reduced stack and start only the required services.
5. Start the local frontend via npm if required.

