package me.jameszhan.notes.yarn;

import org.apache.spark.deploy.yarn.Client;
import org.apache.spark.deploy.yarn.ClientArguments;
import org.apache.spark.SparkConf;

public class SubmitJavaJob {

    public static void main(String[] arguments) {
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        // identify that you will be using Spark as YARN mode
        System.setProperty("SPARK_YARN_MODE", "true");

        // prepare arguments to be passed to
        // org.apache.spark.deploy.yarn.Client object
        String[] args = new String[] {
            // path to your application's JAR file required in yarn-cluster mode
            "--jar", "/usr/local/Cellar/apache-spark/2.4.3/libexec/examples/jars/spark-examples_2.11-2.4.3.jar",
            // name of your application's main class (required)
            "--class", "org.apache.spark.examples.SparkPi",

            // argument 1 to your Spark program (SparkFriendRecommendation)
            "--arg", "10",

            // argument 2 to your Spark program (SparkFriendRecommendation)
            "--arg", "/friends/input"
        };

        // create an instance of SparkConf object
        SparkConf sparkConf = new SparkConf();
        sparkConf.setSparkHome("/usr/local/Cellar/apache-spark/2.4.3/libexec");
        sparkConf.setAppName("submitLocalJob");
        sparkConf.setMaster("yarn");
        sparkConf.set("spark.submit.deployMode", "client");

        // create ClientArguments, which will be passed to Client
        ClientArguments cArgs = new ClientArguments(args);

        // create an instance of yarn Client client
        Client client = new Client(cArgs, sparkConf);

        // submit Spark job to YARN
        client.run();
    }
}
