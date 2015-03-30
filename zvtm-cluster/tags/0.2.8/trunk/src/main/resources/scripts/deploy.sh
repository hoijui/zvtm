#!/bin/bash

#Usage example: ./deploy.sh foobar /Users/wild/sandboxes/romain/

for col in {a..d}
do
	for row in {1..4}
      do
		  scp -r $1 wild@$col$row.wild.lri.fr:$2 
      done
done

