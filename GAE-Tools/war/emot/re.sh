#!/bin/sh

START=$1
END=$(($2 + 1))

while [ $START -lt $END ]; do
	TO=$(($START - 1))
	if [ $START -lt 10 ]; then
		mv emot0$START.gif emot0$TO.gif
	else
		mv emot$START.gif emot$TO.gif
	fi
	START=$(($START + 1))
done
