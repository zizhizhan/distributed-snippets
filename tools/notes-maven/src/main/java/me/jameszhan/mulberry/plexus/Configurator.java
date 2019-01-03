package me.jameszhan.mulberry.plexus;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zizhi.zhzzh
 * Date: 2/24/14
 * Time: 4:39 PM
 */
public class Configurator {

    private Launcher launcher;

    private ClassWorld world;

    private Map<String, ClassRealm> configuredRealms = new ConcurrentHashMap<>();

    private ClassRealm curRealm;

    private ClassLoader foreignClassLoader = null;

    public Configurator(Launcher launcher) {
        this.launcher = launcher;
        this.foreignClassLoader = launcher.getSystemClassLoader();
        this.world = new ClassWorld();
        this.world.addListener(new DebugClassWorldListener());
    }

    public Configurator(Launcher launcher, ClassWorld classWorld) {
        this(launcher);
        this.world = classWorld;
    }


    public void configure(InputStream is) throws Exception {
        ConfigurationParser parser = new ConfigurationParser(this, System.getProperties());
        parser.parse(is);
        // Associate child realms to their parents.
        associateRealms();
        if (this.launcher != null) {
            this.launcher.setWorld(world);
        }
    }

    public void associateRealms() {
        List<String> sortRealmNames = new ArrayList<String>(configuredRealms.keySet());
        Comparator<String> comparator = new Comparator<String>() {
            public int compare(String g1, String g2) {
                return g1.compareTo(g2);
            }
        };

        Collections.sort(sortRealmNames, comparator);

        // So now we have something like the following for defined
        // realms:
        //
        // root
        // root.maven
        // root.maven.plugin
        //
        // Now if the name of a realm is a superset of an existing realm
        // the we want to make child/parent associations.
        for (String realmName : sortRealmNames) {
            int j = realmName.lastIndexOf('.');
            if (j > 0) {
                String parentRealmName = realmName.substring(0, j);
                ClassRealm parentRealm = configuredRealms.get(parentRealmName);
                if (parentRealm != null) {
                    ClassRealm realm = configuredRealms.get(realmName);
                    realm.setParentRealm(parentRealm);
                }
            }
        }
    }

    public void addImportFrom(String relamName, String importSpec) throws NoSuchRealmException {
        curRealm.importFrom(relamName, importSpec);
    }

    public void addLoadFile(File file) {
        System.out.println("addLoadFile: " + file);
        try {
            curRealm.addURL(file.toURI().toURL());
        } catch (MalformedURLException e) {
            // can't really happen... or can it?
        }
    }

    public void addLoadURL(URL url) {
        System.out.println("addLoadURL: " + url);
        curRealm.addURL(url);
    }

    public void addRealm(String realmName) throws DuplicateRealmException {
        curRealm = world.newRealm(realmName, foreignClassLoader);
        // Stash the configured realm for subsequent association processing.
        configuredRealms.put(realmName, curRealm);
    }

    public void setAppMain(String mainClassName, String mainRealmName) {
        if (this.launcher != null) {
            this.launcher.setAppMain(mainClassName, mainRealmName);
        }
    }

}
