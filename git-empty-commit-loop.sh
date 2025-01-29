#!/bin/bash

# Set the number of commits you want to create
NUM_COMMITS=3

# Set the delay between commits in seconds
DELAY=52

echo "start creating $NUM_COMMITS commits with delay $DELAY"

# Loop to create and push empty commits
for ((i=1; i<=NUM_COMMITS; i++))
do
    sleep $DELAY

    echo "Creating commit #$i"
    # Create an empty commit
    git commit --allow-empty -m "CI run ($i)"
    
    echo "Pushing commit #$i"
    # Push the commit to the remote repository
    git push
    
done

echo "All done!"