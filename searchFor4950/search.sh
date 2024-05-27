#!/bin/bash

#Read the json file line by line
entries=$(jq -c '.[]' failedCiRuns.json)

echo "$entries" | while read entry; do
  url=$(echo $entry | jq -r '.url')
  run_id=$(echo $url | awk -F '/' '{print $NF}')
  log_output=$(gh run view $run_id --log-failed)
  if echo "$log_output" | grep -q "but expected 50 companies"; then
      echo "MATCH: '$url'"
  else
      echo "NO MATCH: '$url'"
  fi
done
