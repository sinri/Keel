package io.github.sinri.keel.test.v1.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CheckStaticOfClass {
    public static String s = "SSS";

    public static String sm() {
        return "SM";
    }

    public static void main(String[] args) {
        try {
            Field s = CheckStaticOfClass.class.getField("s2");
            System.out.println(s.getName() + " : " + s.getType());

            Object o = s.get(CheckStaticOfClass.class);
            System.out.println("v: " + o);

            Method sm = CheckStaticOfClass.class.getMethod("sm");
            boolean isSMStatic = Modifier.isStatic(sm.getModifiers());
            System.out.println("sm status? " + isSMStatic);
            Object so = sm.invoke(CheckStaticOfClass.class);
            System.out.println("sv: " + so);

            Method m = CheckStaticOfClass.class.getMethod("m");
            Object no = m.invoke(CheckStaticOfClass.class);
            System.out.println("nv: " + no);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public String m() {
        return "M";
    }
}
