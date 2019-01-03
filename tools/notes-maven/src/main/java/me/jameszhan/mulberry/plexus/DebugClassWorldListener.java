package me.jameszhan.mulberry.plexus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.codehaus.plexus.classworlds.ClassWorldListener;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zizhi.zhzzh
 * Date: 2/28/14
 * Time: 11:41 PM
 */
public class DebugClassWorldListener implements ClassWorldListener {

    private boolean hasLogged;

    public void realmCreated(ClassRealm realm) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(JSON.toJSONString(realm, new PropertyFilter() {
            public boolean apply(Object object, String name, Object value) {
                return !("strategy".equalsIgnoreCase(name) || "world".equalsIgnoreCase(name));
            }
        }, SerializerFeature.PrettyFormat));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    public void realmDisposed(ClassRealm realm) {
        logRealm(realm);
    }

    private void logRealm(ClassRealm realm) {
        if (!hasLogged) {
            for (ClassRealm cr : realm.getWorld().getRealms()) {
                System.out.println("**************************************");
                System.out.println(cr.getId());
                System.out.println("foreignImports: " + getFieldValue("foreignImports", cr));
                System.out.println("parentImports: " + getFieldValue("parentImports", cr));
                System.out.println("parentClassLoader: " + getFieldValue("parentClassLoader", cr));
                System.out.println("parent: " + getFieldValue("parent", cr));
                System.out.println(JSON.toJSONString(cr, new PropertyFilter() {
                    public boolean apply(Object object, String name, Object value) {
                        return !name.equalsIgnoreCase("world")
                                && !name.equalsIgnoreCase("importRealms")
                                && !name.equalsIgnoreCase("strategy");
                    }
                }, SerializerFeature.PrettyFormat));
                System.out.println("**************************************");
            }
            hasLogged = true;
        }
    }

    private Object getFieldValue(String fieldName, Object target) {
        Object retValue = null;
        try {
            Field f = findFieldIncludeSuperclass(fieldName, target.getClass());
            f.setAccessible(true);
            retValue = f.get(target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    private Field findFieldIncludeSuperclass(String fieldName, Class<?> clazz) {
        Field retValue = null;
        try {
            retValue = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            clazz = clazz.getSuperclass();
            if (clazz != null) {
                retValue = findFieldIncludeSuperclass(fieldName, clazz);
            }
        }
        return retValue;
    }

}
