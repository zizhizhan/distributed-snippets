package com.zizhizhan.notes.elastic;

import com.alibaba.fastjson.JSON;

public class ElasticAddDocMain {

    public static void main(String[] args) throws Exception {
        String s = "{\"@timestamp\":\"2020-06-30T02:54:40.731Z\",\"@metadata\":{\"beat\":\"filebeat\",\"type\":\"_doc\",\"version\":\"7.3.2\"}," +
                "\"input\":{\"type\":\"log\"},\"ecs\":{\"version\":\"1.0.1\"},\"host\":{\"architecture\":\"x86_64\",\"os\":" +
                "{\"version\":\"7 (Core)\",\"family\":\"redhat\",\"name\":\"CentOS Linux\",\"kernel\":\"3.10.0-514.26.2.el7.x86_64\"," +
                "\"codename\":\"Core\",\"platform\":\"centos\"},\"id\":\"f9d400c5e1e8c3a8209e990d887d4ac1\",\"name\":\"VM_117_30_centos\"," +
                "\"containerized\":false,\"hostname\":\"VM_117_30_centos\"},\"agent\":{\"hostname\":\"VM_117_30_centos1\"," +
                "\"id\":\"2d9ebd73-b6aa-412e-8fc7-c169a44fe69c\",\"version\":\"7.3.2\",\"type\":\"filebeat\"," +
                "\"ephemeral_id\":\"55d4fa3f-0d5d-4471-9909-a41dcf820c2e\"},\"cloud\":{\"region\":\"china-south-gz\"," +
                "\"availability_zone\":\"gz-azone4\",\"instance\":{\"id\":\"ins-eyfne0xw\"},\"provider\":\"qcloud\"}," +
                "\"log\":{\"offset\":1274,\"file\":{\"path\":\"gdatalogsai-solutionbusinesssolution1.log\"}}," +
                "\"message\":\"2019-09-26 23:39:10.005 [QuartzScheduler_Worker-1] INFO  c.w.s.a.service.ShoposConfigService - " +
                "[SOLUTION] - get config c_Predict_PICK_BBG value is 1569305973483  \"}";
        ElasticSearchUtils.initEs("elastic", null, "127.0.0.1", 9200);
        String index = "2019-12xx";
        for (int i = 0; i < 1000; i++) {
            ElasticSearchUtils.addDocument(index, JSON.parseObject(s));
        }
    }
}
