#!/bin/bash
set -euxo pipefail
for i in {1..10}
do
   #git commit --allow-empty -m "Empty-Commit To trigger CI $i"
   #git push origin investigating_cypress_ci_stalls
   #sleep 1
   ./gradlew :dataland-frontend:generateClients :dataland-frontend:npm_run_testcomponent --no-daemon --stacktrace
done
echo "All good"