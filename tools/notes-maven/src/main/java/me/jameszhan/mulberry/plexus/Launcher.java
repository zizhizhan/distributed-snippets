package me.jameszhan.mulberry.plexus;

import me.jameszhan.notes.maven.Utils;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zizhi.zhzzh
 *         Date: 2/24/14
 *         Time: 4:19 PM
 */
public class Launcher {

    private static ClassLoader systemClassLoader;
    private String mainClassName;
    private String mainRealmName;
    private ClassWorld world;
    private int exitCode = 0;

    static {
        systemClassLoader = Thread.currentThread().getContextClassLoader();
    }


    public static void main(String[] args) throws Exception {
        String classworldsConf = "m3.conf";
        Utils.prepareMavenEnv("tools/notes-maven");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream in = cl.getResourceAsStream(classworldsConf);
        Launcher launcher = new Launcher();
        launcher.configure(in);
        launcher.launchEnhanced(args);
    }

    protected void launchEnhanced(String[] args) throws ClassNotFoundException, NoSuchMethodException, NoSuchRealmException {
        ClassRealm mainRealm = getMainRealm();
        Class<?> mainClass = getMainClass();
        Method mainMethod = Utils.getEnhancedMainMethod(getMainClass());
        Thread.currentThread().setContextClassLoader(mainRealm);
        try {
            Object ret = mainMethod.invoke(mainClass, args, getWorld());
            if (ret instanceof Integer) {
                exitCode = ((Integer)ret);
            }
            System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            Thread.currentThread().setContextClassLoader(systemClassLoader);
        }
    }

    protected void launchStandard(String[] args) throws ClassNotFoundException, NoSuchMethodException, NoSuchRealmException {
        ClassRealm mainRealm = getMainRealm();
        Class<?> mainClass = getMainClass();
        Method mainMethod = Utils.getMainMethod(mainClass);
        Thread.currentThread().setContextClassLoader(mainRealm);
        try {
            Object ret = mainMethod.invoke(mainClass, new Object[]{args});
            if (ret instanceof Integer) {
                exitCode = ((Integer)ret);
            }
            System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            Thread.currentThread().setContextClassLoader(systemClassLoader);
        }
    }


    public void configure(InputStream in) throws Exception {
        Configurator configurator = new Configurator(this);
        configurator.configure(in);
    }

    public void setWorld(ClassWorld classWorld) {
        this.world = classWorld;
    }

    public void setAppMain(String mainClassName, String mainRealmName) {
        this.mainClassName = mainClassName;
        this.mainRealmName = mainRealmName;
    }

    public ClassLoader getSystemClassLoader() {
        return systemClassLoader;
    }

    public Class<?> getMainClass() throws ClassNotFoundException, NoSuchRealmException {
        return getMainRealm().loadClass(getMainClassName());
    }

    public ClassRealm getMainRealm() throws NoSuchRealmException {
        return getWorld().getRealm(getMainRealmName());
    }


    public ClassWorld getWorld() {
        return world;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public String getMainRealmName() {
        return mainRealmName;
    }
}
