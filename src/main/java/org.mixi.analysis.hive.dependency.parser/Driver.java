package org.mixi.analysis.hive.dependency.parser;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore;
import org.apache.hadoop.hive.ql.Context;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;
import org.codehaus.jettison.json.JSONException;

import java.io.*;

/**
 * Created by hikaru.ojima on 2014/02/17.
 */
public class Driver {
    private static Logger logger = new Logger();

    public static Dependency processFile(String path) throws IOException {
        File file = new File(path);
        String line;
        StringBuilder qsb = new StringBuilder();

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                // Skipping through comments
                if (! line.startsWith("--")) {
                    qsb.append(line + "\n");
                }
            }
        } catch (Exception e) {
            logger.error("File: " + path + " could not be rtead.", e);
        }

        return processLine(qsb.toString());
    }

    public static Dependency processLine(String line) throws IOException {
        Dependency n, ret = new Dependency();

        String command = "";
        for (String oneCmd : line.split(";")) {

            if (StringUtils.endsWith(oneCmd, "\\")) {
                command += StringUtils.chop(oneCmd) + ";";
                continue;
            } else {
                command += oneCmd;
            }
            if (StringUtils.isBlank(command)) {
                continue;
            }

            n = processCmd(command);
            ret.merge(n);

            command = "";
        }

        return ret;
    }
    public static Dependency processCmd(String cmd) throws IOException {
        String cmd_trimmed = cmd.trim();
        String[] tokens = cmd_trimmed.split("\\s");

        if (tokens[0].equalsIgnoreCase("source")) {
            String cmd_1 = getFirstCmd(cmd_trimmed, tokens[0].length());

            File sourceFile = new File(cmd_1);
            if (! sourceFile.isFile()){
                logger.error("File: "+ cmd_1 + " is not a file.");
                return Dependency.NULL;
            } else {
                return processFile(cmd_1);
            }
        } else {
            try {
                Configuration cfg = new org.apache.hadoop.conf.Configuration();
                if (cfg.get("_hive.hdfs.session.path") == null) {
                    cfg.set("_hive.hdfs.session.path", "/test");
                }
                if (cfg.get("_hive.local.session.path") == null) {
                    cfg.set("_hive.local.session.path", "/tmp");
                }
                Context ctx = new org.apache.hadoop.hive.ql.Context(cfg);
                ParseDriver driver = new ParseDriver();
                ASTNode tree;

                tree = driver.parse(cmd, ctx);
                tree = ParseUtils.findRootNonNullToken(tree);
                return TreeParser.parse(tree);

            } catch (ParseException e) {
                logger.error("ParseFailed: " + cmd, e);
                return Dependency.NULL;
            }
        }
    }

    public static Dependency run(String path) throws ParseException, IOException {
        return processFile(path);
    }

    public static void main(String[] args) throws ParseException, JSONException, IOException {
        Dependency d = run(args[0]);
        System.out.println(d.toJson());
    }

    private static String getFirstCmd(String cmd, int length) {
        return cmd.substring(length).trim();
    }
}
