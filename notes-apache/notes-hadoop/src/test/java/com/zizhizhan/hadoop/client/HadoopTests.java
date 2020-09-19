package com.zizhizhan.hadoop.client;
/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */

import org.apache.hadoop.mapreduce.protocol.ClientProtocolProvider;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.ServiceLoader;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zizhi.zhzzh
 *         Date: 4/20/14
 *         Time: 3:44 PM
 */
public class HadoopTests {

    @Test
    public void load() throws IOException {
        ServiceLoader<ClientProtocolProvider> frameworkLoader = ServiceLoader.load(ClientProtocolProvider.class);
        for (ClientProtocolProvider provider : frameworkLoader) {
            System.out.println(provider);
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        listResources(cl, "META-INF/services");
        listResources(cl, "META-INF/services/org.apache.hadoop.mapreduce.protocol.ClientProtocolProvider");
    }

    private void listResources(ClassLoader cl, String resourceName) throws IOException{
        System.out.format("\nList : %s \n", resourceName);
        Enumeration<URL> resources = cl.getResources(resourceName);
        while (resources.hasMoreElements()) {
            URL url =  resources.nextElement();
            System.out.println(url);
        }
    }

}
