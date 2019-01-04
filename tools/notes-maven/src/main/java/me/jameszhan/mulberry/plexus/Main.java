package me.jameszhan.mulberry.plexus;

import me.jameszhan.mulberry.maven.MavenStart;
import me.jameszhan.notes.maven.Utils;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zizhi.zhzzh
 *         Date: 2/24/14
 *         Time: 11:58 PM
 */
public class Main {
    private static final String DEFAULT_REALM_NAME = "plexus.core";

    private static ClassLoader systemClassLoader;

    static {
        systemClassLoader = Thread.currentThread().getContextClassLoader();
    }

    public static void main(final String[] args) throws Exception {
        Utils.prepareMavenEnv("tools/notes-maven");
        String mavenCli = "org.apache.maven.cli.MavenCli";

        final ClassWorld world = new ClassWorld("bootstrap", systemClassLoader);
        world.addListener(new DebugClassWorldListener());

        ClassRealm classRealm = world.newRealm(DEFAULT_REALM_NAME, systemClassLoader);

        for(File file : Utils.globFiles(String.format("%s%s", System.getProperty("maven.home"), "/lib/*.jar"))) {
            classRealm.addURL(file.toURI().toURL());
        }

        final String[] arguments = new String[]{ "clean", "install" };
        final Class<?> mainClass = classRealm.loadClass(mavenCli);
        final Method method = Utils.getEnhancedMainMethod(mainClass);
        executeIn(classRealm, () -> {
            try {
                method.invoke(mainClass, arguments, world);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    static void executeIn(ClassLoader cl, Runnable runnable) throws Exception {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            runnable.run();
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }



}
