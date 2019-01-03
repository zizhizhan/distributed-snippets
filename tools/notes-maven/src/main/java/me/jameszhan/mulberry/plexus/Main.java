package me.jameszhan.mulberry.plexus;

import me.jameszhan.mulberry.maven.MavenStart;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zizhi.zhzzh
 *         Date: 2/24/14
 *         Time: 11:58 PM
 */
public class Main {
    private static final String MVN_VERSION = "3.6.0";
    private static final String DEFAULT_REALM_NAME = "plexus.core";

    private static ClassLoader systemClassLoader;

    static {
        systemClassLoader = Thread.currentThread().getContextClassLoader();
    }

    public static void main(final String[] args) throws Exception {
        String currentDir = new File(".").getCanonicalPath();
        System.out.println("Current Directory: " + currentDir);
        System.setProperty("user.dir", currentDir);
        System.setProperty("maven.home", "/usr/local/Cellar/maven/" + MVN_VERSION + "/libexec");
        String mavenCli = "org.apache.maven.cli.MavenCli";
        final ClassWorld world = new ClassWorld("bootstrap", systemClassLoader);
        world.addListener(new DebugClassWorldListener());

        ClassRealm classRealm = world.newRealm(DEFAULT_REALM_NAME, systemClassLoader);

        for(File file : MavenStart.globFiles(String.format("%s%s", System.getProperty("maven.home"), "/lib/*.jar"))) {
            classRealm.addURL(file.toURI().toURL());
        }

        final String[] arguments = new String[]{ "clean", "install", "-Dmaven.multiModuleProjectDirectory" };
        final Class<?> mainClass = classRealm.loadClass(mavenCli);
        final Method method = getMainMethod(mainClass);
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

    protected static Method getMainMethod(Class<?> mainClass) throws Exception {
        Method m = mainClass.getMethod("main", new Class[]{String[].class, ClassWorld.class});
        int modifiers = m.getModifiers();

        if ( Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
            if (m.getReturnType() == Integer.TYPE || m.getReturnType() == Void.TYPE) {
                return m;
            }
        }

        throw new NoSuchMethodException("public static void main(String[] args) in " + mainClass);
    }

}
