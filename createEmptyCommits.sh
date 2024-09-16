#!/bin/bash

branch_name=DALA-4667_markdown_in_comments
number_of_commits=1

for ((i=1; i<=$number_of_commits; i++)); do
echo "Creating empty commit $i"
git commit --allow-empty -m "Empty CI run ($i/$number_of_commits)"
git push origin $branch_name
done
