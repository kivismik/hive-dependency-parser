package org.mixi.analysis.hive.dependency.parser;

import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;

import java.util.ArrayList;

/**
 * Created by hikaru.ojima on 2014/02/18.
 */
public class TreeParser {
    private static String currentDB;

    public static Dependency parse(ASTNode tree) {
        Dependency result = new Dependency();

        switch (tree.getToken().getType()) {
            case HiveParser.TOK_SWITCHDATABASE: {
                currentDB = tree.getChild(0).getText();
                return result;
            }
            case HiveParser.TOK_UNION:
            case HiveParser.TOK_QUERY: {
                parseRoot(tree, result);
                return result;
            }

            case HiveParser.TOK_LOAD: {
                ASTNode tbl = (ASTNode) tree.getChild(1);
                result.addDestination(parseTableRef(tbl));
                return result;
            }

            default: {
                return result;
            }
        }
    }

    private static void parseRoot(ASTNode tree, Dependency result) {
        switch (tree.getToken().getType()) {
            case HiveParser.TOK_UNION: {
                parseUnion(tree, result);
                break;
            }
            case HiveParser.TOK_QUERY: {
                parseQuery(tree, result);
                break;
            }
        }
    }

    private static void parseUnion(ASTNode tree, Dependency result) {
        ASTNode child;
        for (Node c : tree.getChildren()) {
            child = (ASTNode) c;
            switch (child.getToken().getType()) {
                case HiveParser.TOK_QUERY: {
                    parseQuery(child, result);
                }
                case HiveParser.TOK_UNION: {
                    parseUnion(child, result);
                }
            }
        }
    }

    private static void parseQuery(ASTNode tree, Dependency result) {
        ASTNode from = (ASTNode) tree.getChild(0);
        parseFrom(from, result);

        for (int i = 1; i < tree.getChildren().size(); i++) {
            parseInsert((ASTNode)tree.getChild(i), result);
        }
    }

    private static void parseInsert(ASTNode tree, Dependency result) {
        ASTNode child = (ASTNode) tree.getChild(0).getChild(0);

        if (child.getToken().getType() == HiveParser.TOK_TAB) {
            result.addDestination(parseTableRef(child));
        }
    }

    private static void parseFrom(ASTNode tree, Dependency result) {
        ASTNode child = (ASTNode) tree.getChild(0);

        switch (child.getToken().getType()) {
            case HiveParser.TOK_JOIN:
            case HiveParser.TOK_LEFTOUTERJOIN:
            case HiveParser.TOK_RIGHTOUTERJOIN:
            case HiveParser.TOK_FULLOUTERJOIN:
            case HiveParser.TOK_LEFTSEMIJOIN: {
                parseJoin(child, result);
                break;
            }

            case HiveParser.TOK_UNIQUEJOIN: {
                break;
            }

            default: {
                parseJoinOperand(child, result);
                break;
            }
        }
    }

    private static void parseJoin(ASTNode tree, Dependency result) {
        ASTNode child;
        for (Node c : tree.getChildren()) {
            child = (ASTNode) c;
            switch (child.getToken().getType()) {
                case HiveParser.TOK_JOIN:
                case HiveParser.TOK_LEFTOUTERJOIN:
                case HiveParser.TOK_RIGHTOUTERJOIN:
                case HiveParser.TOK_FULLOUTERJOIN:
                case HiveParser.TOK_LEFTSEMIJOIN: {
                    parseJoin(child, result);
                    break;
                }
                default: {
                    parseJoinOperand(child, result);
                    break;
                }
            }
        }
    }

    private static void parseJoinOperand(ASTNode tree, Dependency result) {
        switch (tree.getToken().getType()) {
            case HiveParser.TOK_TABREF: {
                result.addSource(parseTableRef(tree));
                break;
            }
            case HiveParser.TOK_SUBQUERY: {
                ASTNode query = (ASTNode) tree.getChild(0);
                parseRoot(query, result);
                break;
            }
        }
    }

    private static String parseTableRef(ASTNode tree) {
        ASTNode tbl = (ASTNode) tree.getChild(0);
        String db, table;

        if (tbl.getChildren().size() == 2) {
            db    = tbl.getChild(0).getText();
            table = tbl.getChild(1).getText();
        } else {
            db    = currentDB;
            table = tbl.getChild(0).getText();
        }

        return db + "." + table;
    }
}
