#!/bin/bash
for i in {1..38}
do
  echo "fib $i hybrid"
  FIFO=1 $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_fib.sh $i | grep -v R
done