/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test IntxTest
 * @bug 8028756
 * @library /testlibrary /../../test/lib
 * @build IntxTest
 * @run main ClassFileInstaller sun.hotspot.WhiteBox
 *                              sun.hotspot.WhiteBox$WhiteBoxPermission
 * @run main/othervm/timeout=600 -Xbootclasspath/a:. -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI IntxTest
 * @summary testing of WB::set/getIntxVMFlag()
 * @author igor.ignatyev@oracle.com
 */

public class IntxTest {
    private static final String FLAG_NAME = "OnStackReplacePercentage";
    private static final String FLAG_DEBUG_NAME = "InlineFrequencyCount";
    private static final Long[] TESTS = {0L, 100L, -1L,
            (long) Integer.MAX_VALUE, (long) Integer.MIN_VALUE};

    public static void main(String[] args) throws Exception {
        VmFlagTest.runTest(FLAG_NAME, TESTS,
            VmFlagTest.WHITE_BOX::setIntxVMFlag,
            VmFlagTest.WHITE_BOX::getIntxVMFlag);
        VmFlagTest.runTest(FLAG_DEBUG_NAME, VmFlagTest.WHITE_BOX::getIntxVMFlag);
    }
}

