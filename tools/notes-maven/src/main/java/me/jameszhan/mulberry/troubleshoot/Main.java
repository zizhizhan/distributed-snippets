package me.jameszhan.mulberry.troubleshoot;

import me.jameszhan.mulberry.maven.MavenStart;
import me.jameszhan.mulberry.plexus.DebugClassWorldListener;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zizhi.zhzzh
 *         Date: 3/3/14
 *         Time: 12:07 AM
 */
public class Main {

    private static final String MVN_VERSION = "3.6.0";

    public static void main(String[] args) throws Exception {
        String currentDir = new File(".").getCanonicalPath();
        System.out.println("Current Directory: " + currentDir);
        System.setProperty("user.dir", currentDir);
        System.setProperty("maven.home", "/usr/local/Cellar/maven/" + MVN_VERSION + "/libexec");
        String mavenCli = "org.apache.maven.cli.MavenCli";
        String realmId = "maven.core";

        ClassWorld world = new ClassWorld(realmId, ClassLoader.getSystemClassLoader());
        world.addListener(new DebugClassWorldListener());
        ClassRealm classRealm = world.getClassRealm(realmId);

        for(File file : MavenStart.globFiles(String.format("%s%s", System.getProperty("maven.home"), "/lib/*.jar"))) {
            classRealm.addURL(file.toURI().toURL());
        }

        String[] arguments = new String[]{ "clean" };
        Class<?> mainClass = classRealm.loadClass(mavenCli);

        Method m = mainClass.getMethod("main", new Class[]{String[].class, ClassWorld.class});
        int modifiers = m.getModifiers();

        if ( Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
            if (m.getReturnType() == Integer.TYPE || m.getReturnType() == Void.TYPE) {
                m.invoke(mainClass, arguments, world);
            }
        }
    }

}
