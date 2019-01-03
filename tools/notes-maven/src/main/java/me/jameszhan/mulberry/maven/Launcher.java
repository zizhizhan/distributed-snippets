package me.jameszhan.mulberry.maven;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zizhi.zhzzh
 *         Date: 3/1/14
 *         Time: 1:09 PM
 */
public class Launcher {

    private final ClassWorld classWorld;
    private final String mainClassName;
    private final String mainRealmName;

    public Launcher(ClassWorld classWorld, String mainClassName, String mainRealmName) {
        this.classWorld = classWorld;
        this.mainClassName = mainClassName;
        this.mainRealmName = mainRealmName;
    }

    protected void launchEnhanced(String[] args) throws ClassNotFoundException, NoSuchMethodException, NoSuchRealmException {
        ClassRealm mainRealm = classWorld.getRealm(mainRealmName);
        Class<?> mainClass = getMainClass(mainRealm);
        Method mainMethod = getEnhancedMainMethod(mainClass);
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            //Thread.currentThread().setContextClassLoader(mainRealm);
            Object ret = mainMethod.invoke(mainClass, args, classWorld);
            System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    protected Method getEnhancedMainMethod(Class<?> mainClass) throws NoSuchMethodException {
        Method m = mainClass.getMethod("main", new Class[]{String[].class, ClassWorld.class});
        int modifiers = m.getModifiers();
        if ( Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
            if (m.getReturnType() == Integer.TYPE || m.getReturnType() == Void.TYPE) {
                return m;
            }
        }
        throw new NoSuchMethodException("public static void main(String[] args) in " + mainClass);
    }

    public Class<?> getMainClass(ClassRealm classRealm) throws ClassNotFoundException, NoSuchRealmException {
        return classRealm.loadClass(mainClassName);
    }
}
