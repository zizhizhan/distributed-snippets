package com.zizhizhan.hadoop.client;

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
 *         Time: 3:30 PM
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
