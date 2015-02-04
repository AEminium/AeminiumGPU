#!/bin/bash
for i in {1..37}
do
  echo "fib $i gpu"
  FIFO=1 FORCE=GPU $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_fib.sh $i | grep -v R
done