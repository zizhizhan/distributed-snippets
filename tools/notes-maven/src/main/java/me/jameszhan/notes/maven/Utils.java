package me.jameszhan.notes.maven;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.classworlds.ClassWorld;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Email: zhiqiangzhan@gmail.com
 * Date: 2019-01-04
 * Time: 14:28
 */
@Slf4j
public final class Utils {
    private Utils() {}

    private static final String MVN_VERSION = "3.6.0";

    public static void prepareMavenEnv(String dir) {
        String userDir = Paths.get(dir).toAbsolutePath().toString();
        String mavenHome = "/usr/local/Cellar/maven/" + MVN_VERSION + "/libexec";
        System.setProperty("user.dir", userDir);
        System.setProperty("maven.home", mavenHome);
        log.info("user.dir: {}.", userDir);
        log.info("maven.home: {}.", mavenHome);
    }

    public static Method getEnhancedMainMethod(Class<?> mainClass) throws NoSuchMethodException {
        Method m = mainClass.getMethod("main", new Class[]{ String[].class, ClassWorld.class });
        return checkMainMethod(m, mainClass);
    }

    public static Method getMainMethod(Class<?> mainClass) throws NoSuchMethodException {
        Method m = mainClass.getMethod("main", new Class[]{ String[].class });
        return checkMainMethod(m, mainClass);
    }

    public static List<File> globFiles(String globPattern) {
        File globFile = new File(globPattern);
        File dir = globFile.getParentFile();
        if (!dir.exists()) {
            return Collections.emptyList();
        }
        String localName = globFile.getName();
        int starLoc = localName.indexOf("*");
        final String prefix = localName.substring(0, starLoc);
        final String suffix = localName.substring(starLoc + 1);
        File[] files = dir.listFiles((_dir, name) -> name.startsWith(prefix) && name.endsWith(suffix));
        if (files != null) {
            return Arrays.asList(files);
        } else {
            return new ArrayList<>();
        }
    }

    private static Method checkMainMethod(Method m, Class<?> mainClass) throws NoSuchMethodException {
        if (m != null) {
            int modifiers = m.getModifiers();
            if ( Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                if (m.getReturnType() == Integer.TYPE || m.getReturnType() == Void.TYPE) {
                    return m;
                }
            }
        }
        throw new NoSuchMethodException("public static void main(String[] args) in " + mainClass);
    }
}
