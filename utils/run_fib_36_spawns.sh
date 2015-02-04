#!/bin/bash

export i=36

for w in {2..16}
do
  echo "fib $i spawns $w"
  SPAWNS=$w $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_fib.sh $i | grep -v R
done