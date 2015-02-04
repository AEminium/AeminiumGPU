export i=35
echo "fib $i lifo"
FORCE=GPU $APPLEBIN/bin/_repeat.sh sudo -E bash utils/run_fib.sh $i | grep -v R