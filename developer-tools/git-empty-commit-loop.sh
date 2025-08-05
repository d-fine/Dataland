#!/usr/bin/env bash
set -euxo pipefail

# Set the number of commits you want to create
NUM_COMMITS=2

# Set the delay between commits in seconds
DELAY=5

echo "start creating $NUM_COMMITS commits with delay $DELAY"

# Loop to create and push empty commits
for ((i=1; i<=NUM_COMMITS; i++))
do
    echo "Creating commit #$i"
    # Create an empty commit
    git commit --allow-empty -m "CI run ($i)" --no-verify

    echo "Pushing commit #$i"
    # Push the commit to the remote repository
    git push

    sleep $DELAY
done

echo "All done!"
