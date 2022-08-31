#!/bin/sh
set -ex
CYPRESS_TEST_GROUP=101 ./testing/execute_e2e_tests.sh
CYPRESS_TEST_GROUP=102 ./testing/execute_e2e_tests.sh