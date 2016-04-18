CLASSPATH="../../../target/test-classes"
CLASSNAME="fr.inria.zvtm.cluster.tests.ClusteredViewBenchmark"



java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -cp "../../../target/*" fr.inria.zvtm.cluster.SlaveApp -x 500 -s -u -wb 2 -y 100 -fps -b 0 -n "Benchmark" &
pid1=$!
sleep 5
java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -cp "../../../target/*" fr.inria.zvtm.cluster.SlaveApp -x 500 -s -u -wb 2 -y 300 -fps -b 1 -n "Benchmark" &
pid2=$!

sleep 5

java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -cp "$CLASSPATH:../../../target/*" $CLASSNAME --glyph-number 100 "$@"


kill $pid1 $pid2 
