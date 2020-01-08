#!/bin/bash
if [ "$1" != "" ] && [ "$2" != "" ]; then
	SBT_OPTS="-Xms512M -Xmx6G -Xss2M -XX:MaxMetaspaceSize=1024M" sbt "runMain ch.ethz.dalab.web2text.ExtractPageFeatures $1 $2"
else
	echo "Positional parameter is empty. Usage: ./extract_page_features <full_path_to_file.html> <output_prefix>"
fi
