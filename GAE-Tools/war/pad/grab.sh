#!/bin/sh
START=$1
END=$2
COUNTER=$(($1-1))

echo '{'
while [ "$COUNTER" -lt "${END:-681}" ]; do
	COUNTER=$(($COUNTER+1))
	HTML=$(wget http://zh.pad.wikia.com/wiki/$COUNTER -O - | sed 's/\/\*/<!--CM-->/g'| sed 's/$/<!--NL-->/g')
	echo $COUNTER':{'
	echo "\"n\":\""$(echo $HTML | sed 's/<!--NL-->/\n/g' | fgrep -1 '</td><th style="height: 50px; width: 50px;">名称' | tail -n 1 | cut -d '>' -f 3)"\","
	echo "\"r\":\""$(echo $HTML | sed 's/<!--NL-->/\n/g' | sed 's/<!--CM-->/\/\*/g' | fgrep -1 '</td><th style="width: 50px;">稀有' | tail -n 1 | cut -d '>' -f 3)"\","
	echo "\"a\":\""$(echo $HTML | sed 's/<!--NL-->/\n/g' | sed 's/<!--CM-->/\/\*/g' | fgrep -1 '</td><th style="width: 50px;">属性' | tail -n 1 | cut -d '>' -f 3)"\","
	echo "\"s\":\""$(echo $HTML | sed 's/<!--NL-->/\n/g' | sed 's/<!--CM-->/\/\*/g' | fgrep -1 '</td><th style="width: 50px;">系列' | tail -n 1 | cut -d '>' -f 3)"\","
	echo "\"i\":\""$(echo $HTML | sed 's/<!--NL-->/\n/g' | sed 's/<!--CM-->/\/\*/g' | fgrep -1 '</td><th style="height: 50px; width: 50px;">名称' | head -n 1 | sed 's/\s/\n/g' | fgrep 'href=' | cut -d '"' -f 2 | sed 's/http:\/\///g')"\""
	echo '}'
	if [ "$COUNTER" -lt "${END:-681}" ]; then
		echo ','
	fi
done
echo '}'
