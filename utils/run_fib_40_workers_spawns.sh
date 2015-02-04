#!/bin/bash

export i=40

for w in 64 128 256 512 768 1024 2048
do
	for s in 4 6 8 10 12
	do
		echo "fib $i workers $w spawns $s"
		SPAWNS=$s WORKERS=$w FORCE=GPU $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_fib.sh $i | grep -v R
	done
done