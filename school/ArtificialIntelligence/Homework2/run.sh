#!/bin/bash
COUNTER=0
SUCCESS=0
while [[ $COUNTER -lt 100 && $SUCCESS -eq 0 ]]; do
    ./alchemy/bin/infer -i tpp1.kb -e test/$1  -r out.results -q Drive,Visited,In,Buy,Have -m -mwsMaxSteps 500000 > out
    tail -n 17 out > $1.results
    echo >> $1.results; echo "*********** results *************" >> $1.results; echo >> $1.results
    cat out.results | grep -v ".*0$" | sed s/1$// >> $1.results
    SUCCESS=`cat $1.results | grep "Lowest num. of false clauses: 0" | wc -l`
    let COUNTER=$COUNTER+1
    echo >> $1.results; echo "*********************************" >> $1.results;
    if [ $SUCCESS -eq 1 ]
    then
        echo "Success after $COUNTER attempt(s)" >> $1.results
    else
        echo "Failure after $COUNTER attempt(s)" >> $1.results
    fi
    echo "*********************************" >> $1.results
    cat $1.results
done
rm out
rm out.results

# End script
