#!/bin/bash
echo $SSH_PRIVATE_KEY > ~/.ssh/id-rsa
ssh ubuntu@3.71.162.94 ls