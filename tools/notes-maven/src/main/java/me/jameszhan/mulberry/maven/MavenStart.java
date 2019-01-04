package me.jameszhan.mulberry.maven;

import me.jameszhan.mulberry.plexus.DebugClassWorldListener;
import me.jameszhan.notes.maven.Utils;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zizhi.zhzzh
 *         Date: 3/1/14
 *         Time: 1:05 PM
 */
public class MavenStart {

    public static void main(String[] args) throws Exception {
        Utils.prepareMavenEnv("tools/notes-maven");

        String mavenCli = "org.apache.maven.cli.MavenCli";

        final ClassWorld world = new ClassWorld();
        world.addListener(new DebugClassWorldListener());
        ClassRealm classRealm = world.newRealm("maven.core");

        for(File file : Utils.globFiles(String.format("%s%s", System.getProperty("maven.home"), "/lib/*.jar"))) {
            classRealm.addURL(file.toURI().toURL());
        }

        Launcher launcher = new Launcher(world, mavenCli, "maven.core");
        launcher.launchEnhanced(new String[]{"--debug", "install"});
    }

}

