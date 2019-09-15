package me.jameszhan.notes.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class YarnMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(YarnMonitor.class);

    public static void main(String[] args) throws IOException {
        System.setProperty("HADOOP_USER_NAME","hadoop");

        Configuration conf = new YarnConfiguration();
        conf.set("mapreduce.framework.name", "yarn");
        conf.set("fs.default.name", "hdfs://master:9000");
        conf.set("yarn.resourcemanager.scheduler.address", "master:8030");
        conf.set("yarn.resourcemanager.resource-tracker.address", "master:8031");
        conf.set("yarn.resourcemanager.address", "master:8032");
        conf.set("yarn.resourcemanager.admin.address", "master:8033");

        YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(conf);
        yarnClient.start();

        LOGGER.info("Begin to check the yarn application status");
        try {
            yarnClient.getAllQueues().forEach(q -> System.out.format("queue: {%s}\n", q));
            System.out.format("config: {%s}\n", yarnClient.getConfig());

//            List<ApplicationReport> applications = yarnClient.getApplications(EnumSet.of(YarnApplicationState.RUNNING, YarnApplicationState.FINISHED));
            List<ApplicationReport> applications = yarnClient.getApplications();
            applications.forEach(app -> {
                System.out.format("ApplicationId: %s\n", app.getApplicationId());
                System.out.format("Type: %s\n", app.getApplicationType());
                System.out.format("Name: %s\n", app.getName());
                System.out.format("Queue: %s\n", app.getQueue());
                System.out.format("User: %s\n", app.getUser());
            });

        } catch (YarnException e) {
            LOGGER.error("YARN ERROR: ", e);
        }
        LOGGER.info("End to check the yarn application status\n");
        yarnClient.stop();
    }
}
