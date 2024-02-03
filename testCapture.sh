#! /bin/bash
# Switch to new branch, stash and pop any pending changes
set -o nounset
set -o errexit

readonly DIRNAME=$(dirname "$0")
readonly SELF=$(cd "${DIRNAME}" && pwd)

readonly CAPTURE=$1
shift 1

readonly TEST=$1
shift 1

mkdir -p $CAPTURE

function capture {
  local test=$1
  local size=$2
  app/build/install/app/bin/app $test 10000 $size 30 > "${CAPTURE}/${test}_${size}.csv"
}

capture $TEST 1
capture $TEST 2
capture $TEST 3
capture $TEST 4
capture $TEST 5
capture $TEST 6
capture $TEST 7
capture $TEST 8
capture $TEST 9
capture $TEST 10
capture $TEST 12
capture $TEST 16
capture $TEST 24
capture $TEST 30
capture $TEST 42
capture $TEST 56
capture $TEST 72
capture $TEST 90
capture $TEST 100
