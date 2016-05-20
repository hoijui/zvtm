CLASSPATH="../../../target/test-classes"
CLASSNAME="fr.inria.zvtm.cluster.tests.ClusteredViewBenchmark"



java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -cp "../../../target/*" fr.inria.zvtm.cluster.SlaveApp -x 1500 -s -u -wb 2 -hb 2 -y 100 -fps -b 0 -n "Benchmark" &
pid1=$!
sleep 5
java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -cp "../../../target/*" fr.inria.zvtm.cluster.SlaveApp -x 1900 -s -u -wb 1 -hb 2 -y 100 -fps -b 4 -n "Benchmark" &
pid2=$!
sleep 5


java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -cp "$CLASSPATH:../../../target/*" $CLASSNAME -d -c 3 --glyph-number 20 "$@"

kill $pid1 $pid2 
