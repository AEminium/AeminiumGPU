#!/bin/bash

export i=36

for w in 64 128 256 512 768 1024 2048
do
  echo "fib $i workers $w"
  WORKERS=$w $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_fib.sh $i | grep -v R
done