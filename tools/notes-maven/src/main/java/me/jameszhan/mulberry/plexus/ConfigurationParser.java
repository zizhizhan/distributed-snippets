package me.jameszhan.mulberry.plexus;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zizhi.zhzzh
 * Date: 2/24/14
 * Time: 4:38 PM
 */
public class ConfigurationParser {


    public static final String MAIN_PREFIX = "main is";

    public static final String SET_PREFIX = "set";

    public static final String IMPORT_PREFIX = "import";

    public static final String LOAD_PREFIX = "load";

    /**
     * Optionally spec prefix.
     */
    public static final String OPTIONALLY_PREFIX = "optionally";

    private Configurator handler;

    private Properties systemProperties;

    public ConfigurationParser(Configurator handler, Properties systemProperties) {
        this.handler = handler;
        this.systemProperties = systemProperties;
    }

    /**
     * Parse launcher configuration file and send events to the handler.
     */
    public void parse(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

        String line = null;

        int lineNo = 0;

        boolean mainSet = false;

        String curRealm = null;

        while (true) {
            line = reader.readLine();

            if (line == null) {
                break;
            }

            ++lineNo;
            line = line.trim();

            if (canIgnore(line)) {
                continue;
            }

            if (line.startsWith(MAIN_PREFIX)) {
                if (mainSet) {
                    throw new ConfigurationException("Duplicate main configuration", lineNo, line);
                }

                String conf = line.substring(MAIN_PREFIX.length()).trim();

                int fromLoc = conf.indexOf("from");

                if (fromLoc < 0) {
                    throw new ConfigurationException("Missing from clause", lineNo, line);
                }

                String mainClassName = filter(conf.substring(0, fromLoc).trim());

                String mainRealmName = filter(conf.substring(fromLoc + 4).trim());

                this.handler.setAppMain(mainClassName, mainRealmName);

                mainSet = true;
            } else if (line.startsWith(SET_PREFIX)) {
                String conf = line.substring(SET_PREFIX.length()).trim();

                int usingLoc = conf.indexOf(" using") + 1;

                String property = null;

                String propertiesFileName = null;

                if (usingLoc > 0) {
                    property = conf.substring(0, usingLoc).trim();

                    propertiesFileName = filter(conf.substring(usingLoc + 5).trim());

                    conf = propertiesFileName;
                }

                String defaultValue = null;

                int defaultLoc = conf.indexOf(" default") + 1;

                if (defaultLoc > 0) {
                    defaultValue = filter(conf.substring(defaultLoc + 7).trim());

                    if (property == null) {
                        property = conf.substring(0, defaultLoc).trim();
                    } else {
                        propertiesFileName = conf.substring(0, defaultLoc).trim();
                    }
                }

                String value = systemProperties.getProperty(property);

                if (value != null) {
                    continue;
                }

                if (propertiesFileName != null) {
                    File propertiesFile = new File(propertiesFileName);

                    if (propertiesFile.exists()) {
                        Properties properties = new Properties();

                        try {
                            properties.load(new FileInputStream(propertiesFileName));

                            value = properties.getProperty(property);
                        } catch (Exception e) {
                            // do nothing
                        }
                    }
                }

                if (value == null && defaultValue != null) {
                    value = defaultValue;
                }

                if (value != null) {
                    value = filter(value);
                    systemProperties.setProperty(property, value);
                }
            } else if (line.startsWith("[")) {
                int rbrack = line.indexOf("]");

                if (rbrack < 0) {
                    throw new ConfigurationException("Invalid realm specifier", lineNo, line);
                }

                String realmName = line.substring(1, rbrack);

                handler.addRealm(realmName);

                curRealm = realmName;
            } else if (line.startsWith(IMPORT_PREFIX)) {
                if (curRealm == null) {
                    throw new ConfigurationException("Unhandled import", lineNo, line);
                }

                String conf = line.substring(IMPORT_PREFIX.length()).trim();

                int fromLoc = conf.indexOf("from");

                if (fromLoc < 0) {
                    throw new ConfigurationException("Missing from clause", lineNo, line);
                }

                String importSpec = conf.substring(0, fromLoc).trim();

                String relamName = conf.substring(fromLoc + 4).trim();

                handler.addImportFrom(relamName, importSpec);

            } else if (line.startsWith(LOAD_PREFIX)) {
                String constituent = line.substring(LOAD_PREFIX.length()).trim();

                constituent = filter(constituent);

                if (constituent.contains("*")) {
                    loadGlob(constituent, false /*not optionally*/);
                } else {
                    File file = new File(constituent);

                    if (file.exists()) {
                        handler.addLoadFile(file);
                    } else {
                        try {
                            handler.addLoadURL(new URL(constituent));
                        } catch (MalformedURLException e) {
                            throw new FileNotFoundException(constituent);
                        }
                    }
                }
            } else if (line.startsWith(OPTIONALLY_PREFIX)) {
                String constituent = line.substring(OPTIONALLY_PREFIX.length()).trim();

                constituent = filter(constituent);

                if (constituent.contains("*")) {
                    loadGlob(constituent, true /*optionally*/);
                } else {
                    File file = new File(constituent);
                    if (file.exists()) {
                        handler.addLoadFile(file);
                    } else {
                        try {
                            handler.addLoadURL(new URL(constituent));
                        } catch (MalformedURLException e) {
                            // swallow
                        }
                    }
                }
            } else {
                throw new ConfigurationException("Unhandled configuration", lineNo, line);
            }
        }

        reader.close();
    }

    /**
     * Load a glob into the specified classloader.
     *
     * @param line       The path configuration line.
     * @param optionally Whether the path is optional or required
     * @throws MalformedURLException  If the line does not represent
     *                                a valid path element.
     * @throws FileNotFoundException  If the line does not represent
     *                                a valid path element in the filesystem.
     * @throws ConfigurationException
     */
    protected void loadGlob(String line, boolean optionally) throws MalformedURLException, FileNotFoundException, ConfigurationException {
        File globFile = new File(line);

        File dir = globFile.getParentFile();
        if (!dir.exists()) {
            if (optionally) {
                return;
            } else {
                throw new FileNotFoundException(dir.toString());
            }
        }

        String localName = globFile.getName();

        int starLoc = localName.indexOf("*");

        final String prefix = localName.substring(0, starLoc);
        final String suffix = localName.substring(starLoc + 1);

        File[] matches = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (!name.startsWith(prefix)) {
                    return false;
                }

                if (!name.endsWith(suffix)) {
                    return false;
                }

                return true;
            }
        });

        for (File match : matches) {
            handler.addLoadFile(match);
        }
    }

    /**
     * Filter a string for system properties.
     *
     * @param text The text to filter.
     * @return The filtered text.
     * @throws ConfigurationException If the property does not
     *                                exist or if there is a syntax error.
     */
    protected String filter(String text) throws ConfigurationException {
        String result = "";

        int cur = 0;
        int textLen = text.length();

        int propStart = -1;
        int propStop = -1;

        String propName = null;
        String propValue = null;

        while (cur < textLen) {
            propStart = text.indexOf("${", cur);

            if (propStart < 0) {
                break;
            }

            result += text.substring(cur, propStart);

            propStop = text.indexOf("}", propStart);

            if (propStop < 0) {
                throw new ConfigurationException("Unterminated property: " + text.substring(propStart));
            }

            propName = text.substring(propStart + 2, propStop);

            propValue = systemProperties.getProperty(propName);

            /* do our best if we are not running from surefire */
            if (propName.equals("basedir") && (propValue == null || propValue.equals(""))) {
                propValue = (new File("")).getAbsolutePath();

            }

            if (propValue == null) {
                throw new ConfigurationException("No such property: " + propName);
            }
            result += propValue;

            cur = propStop + 1;
        }

        result += text.substring(cur);

        return result;
    }

    /**
     * Determine if a line can be ignored because it is
     * a comment or simply blank.
     *
     * @param line The line to test.
     * @return <code>true</code> if the line is ignorable,
     * otherwise <code>false</code>.
     */
    private boolean canIgnore(String line) {
        return (line.length() == 0 || line.startsWith("#"));
    }
}
