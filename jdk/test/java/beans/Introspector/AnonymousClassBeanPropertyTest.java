/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.beans.BeanInfo;
import java.beans.BeanProperty;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;

import java.util.Arrays;


/**
 * @test
 * @bug 8132973 8132732 8155013
 * @summary Some check for BeanProperty annotation
 * @author a.stepanov
 * @run main AnonymousClassBeanPropertyTest
 */


public class AnonymousClassBeanPropertyTest {

    private final static String  DESCRIPTION = "TEST";
    private final static boolean BOUND       = true;
    private final static boolean EXPERT      = false;
    private final static boolean HIDDEN      = true;
    private final static boolean PREFERRED   = false;
    private final static boolean REQUIRED    = true;
    private final static boolean UPDATE      = false;

    private final static double X = java.lang.Math.PI;

    private final static String
        V_NAME  = "java.lang.Math.PI",
        V_SHORT = "PI",
        V = Double.toString(X);

    private final static String DESCRIPTION_2 = "XYZ";


    // ---------- test cases (interfaces) ----------

    private interface IGet {
        double getX();
    }

    private interface ISet {
        void setX(double v);
    }

    private interface IGetByIndex {
        double getX(int i);
    }

    private interface ISetByIndex {
        void setX(int i, double v);
    }

    private interface IGetArray {
        double[] getX();
    }

    private interface ISetArray {
        void setX(double a[]);
    }

    private interface IGetBoth {
        double   getX(int i);
        double[] getX();
    }

    private interface ISetBoth {
        void setX(int i, double v);
        void setX(double a[]);
    }

    private interface IGetSet {
        double getX();
        void setX(double v);
    }

    private interface IGetSetByIndex {
        double getX(int i);
        void setX(int i, double v);
    }

    private interface IGetSetBoth {
        double   getX(int i);
        double[] getX();
        void setX(int i, double v);
        void setX(double a[]);
    }


    // ---------- checks ----------

    private static boolean check(String what, boolean v, boolean ref) {

        boolean ok = (v == ref);
        if (!ok) { System.out.println(
            "invalid " + what + ": " + v + ", expected: " + ref); }
        return ok;
    }

    private static boolean checkInfo(Class<?> c, String what) {

        BeanInfo i;
        try { i = Introspector.getBeanInfo(c, Object.class); }
        catch (IntrospectionException e) { throw new RuntimeException(e); }

        System.out.println("\nchecking info for " + what);

        PropertyDescriptor descriptors[] = i.getPropertyDescriptors();
        int nd = descriptors.length;
        if (nd != 1) {
            System.out.println("invalid number of descriptors: " + nd);
            return false;
        }

        PropertyDescriptor d = descriptors[0];

        String descr = d.getShortDescription();
        boolean ok = descr.equals(DESCRIPTION);
        if (!ok) { System.out.println("invalid description: " + descr +
                ", expected: " + DESCRIPTION); }

        ok &= check("isBound",  d.isBound(),  BOUND);
        ok &= check("isExpert", d.isExpert(), EXPERT);
        ok &= check("isHidden", d.isHidden(), HIDDEN);
        ok &= check("isPreferred", d.isPreferred(), PREFERRED);
        ok &= check("required", (boolean) d.getValue("required"), REQUIRED);
        ok &= check("visualUpdate",
            (boolean) d.getValue("visualUpdate"), UPDATE);

        Object vals[] = (Object[]) d.getValue("enumerationValues");
        if (vals == null) {
            System.out.println("null enumerationValues");
            return false;
        }

        if (vals.length == 0) {
            System.out.println("empty enumerationValues");
            return false;
        }

        boolean okVals = (
            (vals.length == 3) &&
             vals[0].toString().equals(V_SHORT) &&
             vals[1].toString().equals(V)       &&
             vals[2].toString().equals(V_NAME));

        if (!okVals) {
            System.out.println("invalid enumerationValues:");
            for (Object v: vals) { System.out.println(v.toString()); }
        }

        return (ok && okVals);
    }

    private static boolean checkAlternativeInfo(Class<?> c, String what) {

        BeanInfo i;
        try { i = Introspector.getBeanInfo(c, Object.class); }
        catch (IntrospectionException e) { throw new RuntimeException(e); }

        System.out.println("checking alternative info for " + what);

        PropertyDescriptor descriptors[] = i.getPropertyDescriptors();
        int nd = descriptors.length;
        if (nd != 1) {
            System.out.println("invalid number of descriptors: " + nd);
            return false;
        }

        PropertyDescriptor d = descriptors[0];

        String descr = d.getShortDescription();
        boolean ok = descr.equals(DESCRIPTION_2);
        if (!ok) { System.out.println("invalid alternative description: " +
            descr + ", expected: " + DESCRIPTION_2); }

        ok &= check("isBound",  d.isBound(),  !BOUND);
        ok &= check("isExpert", d.isExpert(), !EXPERT);
        ok &= check("isHidden", d.isHidden(), !HIDDEN);
        ok &= check("isPreferred", d.isPreferred(), !PREFERRED);
        ok &= check("required", (boolean) d.getValue("required"), !REQUIRED);
        ok &= check("visualUpdate",
            (boolean) d.getValue("visualUpdate"), !UPDATE);

        Object vals[] = (Object[]) d.getValue("enumerationValues");
        if (vals != null || vals.length > 0) {
            System.out.println("non-empty enumerationValues");
            return false;
        }

        return ok;
    }



    // ---------- run tests ----------

    public static void main(String[] args) {

        boolean passed = true, ok, ok2;

        //----------------------------------------------------------------------

        IGet testGet = new IGet() {
            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double getX() { return X; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGet.getClass(), "IGet");
        System.out.println("OK = " + ok);
        passed = passed && ok;

        //----------------------------------------------------------------------

        ISet testSet = new ISet() {

            private double x;

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public void setX(double v) { x = v; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testSet.getClass(), "ISet");
        System.out.println("OK = " + ok);
        passed = passed && ok;

        //----------------------------------------------------------------------

        IGetByIndex testGetByIndex = new IGetByIndex() {

            private final double x[] = {X, X};

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double getX(int i) { return x[i]; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetByIndex.getClass(), "IGetByIndex");
        System.out.println("OK = " + ok);
        passed = passed && ok;

        //----------------------------------------------------------------------

        ISetByIndex testSetByIndex = new ISetByIndex() {

            private final double x[] = {X, X, X};

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public void setX(int i, double v) { x[i] = v; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testSetByIndex.getClass(), "ISetByIndex");
        System.out.println("OK = " + ok);
        passed = passed && ok;

        //----------------------------------------------------------------------

        // TODO: please uncomment/update after 8155013 fix
        /*
        IGetArray testGetArray = new IGetArray() {

            private final double x[] = {X, X};

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double[] getX() { return x; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetArray.getClass(), "IGetArray");
        System.out.println("OK = " + ok);
        passed = passed && ok;
        */

        //----------------------------------------------------------------------

        // TODO: please uncomment/update after 8155013 fix
        /*
        ISetArray testSetArray = new ISetArray() {

            private double x[];

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public void setX(double a[]) { x = Arrays.copyOf(a, a.length); }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testSetArray.getClass(), "ISetArray");
        System.out.println("OK = " + ok);
        passed = passed && ok;
        */

        //----------------------------------------------------------------------

        IGetBoth testGetBoth_1 = new IGetBoth() {

            private final double x[] = {X, X};

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double getX(int i) { return x[i]; }
            @Override
            public double[] getX() { return x; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetBoth_1.getClass(), "IGetBoth-1");
        System.out.println("OK = " + ok);
        passed = passed && ok;

        // TODO: please uncomment/update after 8155013 fix
        /*
        IGetBoth testGetBoth_2 = new IGetBoth() {

            private final double x[] = {X, X};

            @Override
            public double getX(int i) { return x[i]; }
            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double[] getX() { return x; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetBoth_2.getClass(), "IGetBoth-2");
        System.out.println("OK = " + ok);
        passed = passed && ok;
        */

        // TODO: please uncomment/update after 8132732 fix
        /*
        IGetBoth testGetBoth_3 = new IGetBoth() {

            private final double x[] = {X, X};

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double getX(int i) { return x[i]; }
            @BeanProperty(
                description  = DESCRIPTION_2,
                bound        = !BOUND,
                expert       = !EXPERT,
                hidden       = !HIDDEN,
                preferred    = !PREFERRED,
                required     = !REQUIRED,
                visualUpdate = !UPDATE)
            @Override
            public double[] getX() { return x; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetBoth_3.getClass(), "IGetBoth-3");
        System.out.println("OK = " + ok);
        ok2 = checkAlternativeInfo(testGetBoth_3.getClass(), "IGetBoth-3");
        System.out.println("OK = " + ok2);
        passed = passed && ok && ok2;
        */

        //----------------------------------------------------------------------

        ISetBoth testSetBoth_1 = new ISetBoth() {

            private double x[] = new double[3];

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public void setX(int i, double v) { x[i] = v; }
            @Override
            public void setX(double[] a) { x = Arrays.copyOf(a, a.length); }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testSetBoth_1.getClass(), "ISetBoth-1");
        System.out.println("OK = " + ok);
        passed = passed && ok;

        // TODO: please uncomment/update after 8155013 fix
        /*
        ISetBoth testSetBoth_2 = new ISetBoth() {

            private double x[] = new double[3];

            @Override
            public void setX(int i, double v) { x[i] = v; }
            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public void setX(double[] a) { x = Arrays.copyOf(a, a.length); }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testSetBoth_2.getClass(), "ISetBoth-2");
        System.out.println("OK = " + ok);
        passed = passed && ok;
        */

        // TODO: please uncomment/update after 8132732 fix
        /*
        ISetBoth testSetBoth_3 = new ISetBoth() {

            private double x[] = {X, X};

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public void setX(int i, double v) { x[i] = v; }
            @BeanProperty(
                description  = DESCRIPTION_2,
                bound        = !BOUND,
                expert       = !EXPERT,
                hidden       = !HIDDEN,
                preferred    = !PREFERRED,
                required     = !REQUIRED,
                visualUpdate = !UPDATE)
            @Override
            public void setX(double[] a) { x = Arrays.copyOf(a, a.length); }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testSetBoth_3.getClass(), "ISetBoth-3");
        System.out.println("OK = " + ok);
        ok2 = checkAlternativeInfo(testSetBoth_3.getClass(), "ISetBoth-3");
        System.out.println("OK = " + ok2);
        passed = passed && ok && ok2;
        */

        //----------------------------------------------------------------------

        IGetSet testGetSet_1 = new IGetSet() {

            private double x;

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double getX() { return x; }
            @Override
            public void setX(double v) { x = v; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetSet_1.getClass(), "IGetSet-1");
        System.out.println("OK = " + ok);
        passed = passed && ok;


        IGetSet testGetSet_2 = new IGetSet() {

            private double x;

            @Override
            public double getX() { return x; }
            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public void setX(double v) { x = v; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetSet_2.getClass(), "IGetSet-2");
        System.out.println("OK = " + ok);
        passed = passed && ok;

        // TODO: please uncomment/update after 8132973 fix
        /*
        IGetSet testGetSet_3 = new IGetSet() {

            private double x;

            @Override
            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            public double getX() { return x; }
            @BeanProperty(
                description  = DESCRIPTION_2,
                bound        = !BOUND,
                expert       = !EXPERT,
                hidden       = !HIDDEN,
                preferred    = !PREFERRED,
                required     = !REQUIRED,
                visualUpdate = !UPDATE)
            @Override
            public void setX(double v) { x = v; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetSet_3.getClass(), "IGetSet-3");
        System.out.println("OK = " + ok);
        ok2 = checkAlternativeInfo(testGetSet_3.getClass(), "IGetSet-3");
        System.out.println("OK = " + ok2);
        passed = passed && ok && ok2;
        */

        //----------------------------------------------------------------------

        IGetSetByIndex testGetSetByIndex_1 = new IGetSetByIndex() {

            private final double x[] = {X, X};

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double getX(int i) { return x[i]; }
            @Override
            public void setX(int i, double v) { x[i] = v; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetSetByIndex_1.getClass(), "IGetSetByIndex-1");
        System.out.println("OK = " + ok);
        passed = passed && ok;


        IGetSetByIndex testGetSetByIndex_2 = new IGetSetByIndex() {

            private final double x[] = {X, X};

            @Override
            public double getX(int i) { return x[i]; }
            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public void setX(int i, double v) { x[i] = v; }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetSetByIndex_2.getClass(), "IGetSetByIndex-2");
        System.out.println("OK = " + ok);
        passed = passed && ok;

        // TODO: please uncomment/update after 8132973 fix
        /*
        IGetSetByIndex testGetSetByIndex_3 = new IGetSetByIndex() {

            private double x[] = {X, X};

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double getX(int i) {
                return x[i];
            }
            @BeanProperty(
                description  = DESCRIPTION_2,
                bound        = !BOUND,
                expert       = !EXPERT,
                hidden       = !HIDDEN,
                preferred    = !PREFERRED,
                required     = !REQUIRED,
                visualUpdate = !UPDATE)
            @Override
            public void setX(int i, double v) {
                x[i] = v;
            }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetSetByIndex_3.getClass(), "IGetSetByIndex-3");
        System.out.println("OK = " + ok);
        ok2 = checkAlternativeInfo(
            testGetSetByIndex_3.getClass(), "IGetSetByIndex-3");
        System.out.println("OK = " + ok2);
        passed = passed && ok && ok2;
        */

        //----------------------------------------------------------------------

        // TODO: please uncomment/update after 8155013 fix
        /*
        IGetSetBoth testGetSetBoth_1 = new IGetSetBoth() {

            private double x[] = {X, X};

            @Override
            public double getX(int i) { return x[i]; }
            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double[] getX() { return x; }
            @Override
            public void setX(int i, double v) { x[i] = v; }
            @Override
            public void setX(double[] a) { x = Arrays.copyOf(a, a.length); }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetSetBoth_1.getClass(), "IGetSetBoth-1");
        System.out.println("OK = " + ok);
        passed = passed && ok;
        */

        // TODO: please uncomment/update after 8155013 fix
        /*
        IGetSetBoth testGetSetBoth_2 = new IGetSetBoth() {

            private double x[] = {X, X};

            @Override
            public double getX(int i) { return x[i]; }
            @Override
            public double[] getX() { return x; }
            @Override
            public void setX(int i, double v) { x[i] = v; }
            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public void setX(double[] a) { x = Arrays.copyOf(a, a.length); }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetSetBoth_2.getClass(), "IGetSetBoth-2");
        System.out.println("OK = " + ok);
        passed = passed && ok;
        */

        // TODO: please uncomment/update after 8132973 fix
        /*
        IGetSetBoth testGetSetBoth_3 = new IGetSetBoth() {

            private double x[] = {X, X};

            @BeanProperty(
                description  = DESCRIPTION,
                bound        = BOUND,
                expert       = EXPERT,
                hidden       = HIDDEN,
                preferred    = PREFERRED,
                required     = REQUIRED,
                visualUpdate = UPDATE,
                enumerationValues = {V_NAME})
            @Override
            public double getX(int i) { return x[i]; }
            @Override
            public double[] getX() { return x; }
            @Override
            public void setX(int i, double v) { x[i] = v; }
            @BeanProperty(
                description  = DESCRIPTION_2,
                bound        = !BOUND,
                expert       = !EXPERT,
                hidden       = !HIDDEN,
                preferred    = !PREFERRED,
                required     = !REQUIRED,
                visualUpdate = !UPDATE)
            @Override
            public void setX(double[] a) { x = Arrays.copyOf(a, a.length); }

            public void addPropertyChangeListener(PropertyChangeListener l)    {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        ok = checkInfo(testGetSetBoth_3.getClass(), "IGetSetBoth-3");
        System.out.println("OK = " + ok);
        ok2 = checkAlternativeInfo(
            testGetSetBoth_3.getClass(), "IGetSetBoth-3");
        System.out.println("OK = " + ok2);
        passed = passed && ok && ok2;
        */

        if (!passed) { throw new RuntimeException("test failed"); }
        System.out.println("\ntest passed");
    }
}
