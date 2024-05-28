#!/bin/bash

# Loop to create 5 empty commits
for ((i=1; i<=5; i++)); do
    echo "Creating empty commit $i"
    git commit --allow-empty -m "Empty commit $i"
    git push origin instable-e2e-test  
done
