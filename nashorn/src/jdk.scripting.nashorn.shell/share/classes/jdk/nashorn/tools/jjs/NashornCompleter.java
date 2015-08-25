/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package jdk.nashorn.tools.jjs;

import java.util.List;
import java.util.regex.Pattern;
import jdk.internal.jline.console.completer.Completer;
import jdk.nashorn.api.tree.AssignmentTree;
import jdk.nashorn.api.tree.BinaryTree;
import jdk.nashorn.api.tree.CompilationUnitTree;
import jdk.nashorn.api.tree.CompoundAssignmentTree;
import jdk.nashorn.api.tree.ConditionalExpressionTree;
import jdk.nashorn.api.tree.ExpressionTree;
import jdk.nashorn.api.tree.ExpressionStatementTree;
import jdk.nashorn.api.tree.FunctionCallTree;
import jdk.nashorn.api.tree.IdentifierTree;
import jdk.nashorn.api.tree.InstanceOfTree;
import jdk.nashorn.api.tree.MemberSelectTree;
import jdk.nashorn.api.tree.NewTree;
import jdk.nashorn.api.tree.SimpleTreeVisitorES5_1;
import jdk.nashorn.api.tree.Tree;
import jdk.nashorn.api.tree.UnaryTree;
import jdk.nashorn.api.tree.Parser;
import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.tools.PartialParser;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ScriptRuntime;

// A simple source completer for nashorn
final class NashornCompleter implements Completer {
    private final Context context;
    private final Global global;
    private final PartialParser partialParser;
    private final Parser parser;

    NashornCompleter(final Context context, final Global global, final PartialParser partialParser) {
        this.context = context;
        this.global = global;
        this.partialParser = partialParser;
        this.parser = Parser.create();
    }

    // Pattern to match a unfinished member selection expression. object part and "."
    // but property name missing pattern.
    private static final Pattern SELECT_PROP_MISSING = Pattern.compile(".*\\.\\s*");

    @Override
    public int complete(final String test, final int cursor, final List<CharSequence> result) {
        // check that cursor is at the end of test string. Do not complete in the middle!
        if (cursor != test.length()) {
            return cursor;
        }

        // get the start of the last expression embedded in the given code
        // using the partial parsing support - so that we can complete expressions
        // inside statements, function call argument lists, array index etc.
        final int exprStart = partialParser.getLastExpressionStart(context, test);
        if (exprStart == -1) {
            return cursor;
        }


        // extract the last expression string
        final String exprStr = test.substring(exprStart);

        // do we have an incomplete member selection expression that misses property name?
        final boolean endsWithDot = SELECT_PROP_MISSING.matcher(exprStr).matches();

        // If this is an incomplete member selection, then it is not legal code.
        // Make it legal by adding a random property name "x" to it.
        final String completeExpr = endsWithDot? exprStr + "x" : exprStr;

        final ExpressionTree topExpr = getTopLevelExpression(parser, completeExpr);
        if (topExpr == null) {
            // did not parse to be a top level expression, no suggestions!
            return cursor;
        }


        // Find 'right most' expression of the top level expression
        final Tree rightMostExpr = getRightMostExpression(topExpr);
        if (rightMostExpr instanceof MemberSelectTree) {
            return completeMemberSelect(exprStr, cursor, result, (MemberSelectTree)rightMostExpr, endsWithDot);
        } else if (rightMostExpr instanceof IdentifierTree) {
            return completeIdentifier(exprStr, cursor, result, (IdentifierTree)rightMostExpr);
        } else {
            // expression that we cannot handle for completion
            return cursor;
        }
    }

    private int completeMemberSelect(final String exprStr, final int cursor, final List<CharSequence> result,
                final MemberSelectTree select, final boolean endsWithDot) {
        final ExpressionTree objExpr = select.getExpression();
        final String objExprCode = exprStr.substring((int)objExpr.getStartPosition(), (int)objExpr.getEndPosition());

        // try to evaluate the object expression part as a script
        Object obj = null;
        try {
            obj = context.eval(global, objExprCode, global, "<suggestions>");
        } catch (Exception ignored) {
            // throw the exception - this is during tab-completion
        }

        if (obj != null && obj != ScriptRuntime.UNDEFINED) {
            if (endsWithDot) {
                // no user specified "prefix". List all properties of the object
                result.addAll(PropertiesHelper.getProperties(obj));
                return cursor;
            } else {
                // list of properties matching the user specified prefix
                final String prefix = select.getIdentifier();
                result.addAll(PropertiesHelper.getProperties(obj, prefix));
                return cursor - prefix.length();
            }
        }

        return cursor;
    }

    private int completeIdentifier(final String test, final int cursor, final List<CharSequence> result,
                final IdentifierTree ident) {
        final String name = ident.getName();
        result.addAll(PropertiesHelper.getProperties(global, name));
        return cursor - name.length();
    }

    // returns ExpressionTree if the given code parses to a top level expression.
    // Or else returns null.
    private ExpressionTree getTopLevelExpression(final Parser parser, final String code) {
        try {
            final CompilationUnitTree cut = parser.parse("<code>", code, null);
            final List<? extends Tree> stats = cut.getSourceElements();
            if (stats.size() == 1) {
                final Tree stat = stats.get(0);
                if (stat instanceof ExpressionStatementTree) {
                    return ((ExpressionStatementTree)stat).getExpression();
                }
            }
        } catch (final NashornException ignored) {
            // ignore any parser error. This is for completion anyway!
            // And user will get that error later when the expression is evaluated.
        }

        return null;
    }

    private Tree getRightMostExpression(final ExpressionTree expr) {
        return expr.accept(new SimpleTreeVisitorES5_1<Tree, Void>() {
            @Override
            public Tree visitAssignment(final AssignmentTree at, final Void v) {
                return getRightMostExpression(at.getExpression());
            }

            @Override
            public Tree visitCompoundAssignment(final CompoundAssignmentTree cat, final Void v) {
                return getRightMostExpression(cat.getExpression());
            }

            @Override
            public Tree visitConditionalExpression(final ConditionalExpressionTree cet, final Void v) {
                return getRightMostExpression(cet.getFalseExpression());
            }

            @Override
            public Tree visitBinary(final BinaryTree bt, final Void v) {
                return getRightMostExpression(bt.getRightOperand());
            }

            @Override
            public Tree visitIdentifier(final IdentifierTree ident, final Void v) {
                return ident;
            }


            @Override
            public Tree visitInstanceOf(final InstanceOfTree it, final Void v) {
                return it.getType();
            }


            @Override
            public Tree visitMemberSelect(final MemberSelectTree select, final Void v) {
                return select;
            }

            @Override
            public Tree visitNew(final NewTree nt, final Void v) {
                final ExpressionTree call = nt.getConstructorExpression();
                if (call instanceof FunctionCallTree) {
                    final ExpressionTree func = ((FunctionCallTree)call).getFunctionSelect();
                    // Is this "new Foo" or "new obj.Foo" with no user arguments?
                    // If so, we may be able to do completion of constructor name.
                    if (func.getEndPosition() == nt.getEndPosition()) {
                        return func;
                    }
                }
                return null;
            }

            @Override
            public Tree visitUnary(final UnaryTree ut, final Void v) {
                return getRightMostExpression(ut.getExpression());
            }
        }, null);
    }
}
