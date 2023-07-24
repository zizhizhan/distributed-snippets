package com.zizhizhan.flink.examples.datagen;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.ExecutionOptions;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class Datagen {

    public static void main(String[] args) throws Exception {
        // final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment("localhost", 8081,
              "/opt/var/maven/org/apache/flink/flink-connector-datagen/1.17.1/flink-connector-datagen-1.17.1.jar",
                "/opt/rootfs/codehub/javaprojects/distributed-snippets/notes-apache/notes-flink117/target/notes-flink117-1.0-SNAPSHOT.jar");

        env.setRuntimeMode(ExecutionOptions.RUNTIME_MODE.defaultValue());

        GeneratorFunction<Long, String> generatorFunction = index -> "Number: " + index;
        long numberOfRecords = 10000000;

        DataGeneratorSource<String> source = new DataGeneratorSource<>(generatorFunction, numberOfRecords, Types.STRING);

        DataStreamSource<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Generator Source");

        DataStream<Tuple2<String, Integer>> counts = stream.flatMap(new Tokenizer())
                .name("tokenizer")
                .keyBy(value -> value.f0)
                .sum(1)
                .name("counter");

        counts.print().name("print-sink");
        env.execute("Datagen");
    }

    public static final class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // normalize and split the line
            String[] tokens = value.toLowerCase().split("\\W+");

            // emit the pairs
            for (String token : tokens) {
                if (token.length() > 0) {
                    out.collect(new Tuple2<>(token, 1));
                }
            }
        }
    }

}
