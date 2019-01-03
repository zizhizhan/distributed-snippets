package me.jameszhan.mulberry.plexus;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
        System.setProperty("maven.home", "/usr/local/Cellar/maven/3.2.1/libexec");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream in = cl.getResourceAsStream(classworldsConf);
        Launcher launcher = new Launcher();
        launcher.configure(in);
        launcher.launchEnhanced(args);
    }

    protected void launchEnhanced(String[] args) throws ClassNotFoundException, NoSuchMethodException, NoSuchRealmException {
        ClassRealm mainRealm = getMainRealm();
        Class<?> mainClass = getMainClass();
        Method mainMethod = getEnhancedMainMethod();
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

    protected Method getEnhancedMainMethod() throws ClassNotFoundException, NoSuchMethodException, NoSuchRealmException {
        Method m = getMainClass().getMethod("main", new Class[]{String[].class, ClassWorld.class});

        int modifiers = m.getModifiers();

        if ( Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
            if (m.getReturnType() == Integer.TYPE || m.getReturnType() == Void.TYPE) {
                return m;
            }
        }
        throw new NoSuchMethodException("public static void main(String[] args) in " + getMainClass());
    }

    protected void launchStandard(String[] args) throws ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchRealmException {
        ClassRealm mainRealm = getMainRealm();
        Class<?> mainClass = getMainClass();
        Method mainMethod = getMainMethod();
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

    protected Method getMainMethod() throws ClassNotFoundException, NoSuchMethodException, NoSuchRealmException {
        Method m = getMainClass().getMethod("main", new Class[]{ String[].class });

        int modifiers = m.getModifiers();

        if ( Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
            if (m.getReturnType() == Integer.TYPE || m.getReturnType() == Void.TYPE) {
                return m;
            }
        }
        throw new NoSuchMethodException("public static void main(String[] args) in " + getMainClass());
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
