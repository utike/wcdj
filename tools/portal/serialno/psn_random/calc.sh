#!/bin/bash

rm out

CNT=$1
if [[ "$CNT" -lt "1" ]]; then
	CNT=1
fi
echo "$CNT" process


for ((i=0; i<$CNT; i++))
do
	./random_test >> out_$i&
done

wait

echo "total:"
wc out_* -l

echo "result:"

sort out_* | uniq -d | wc -l
