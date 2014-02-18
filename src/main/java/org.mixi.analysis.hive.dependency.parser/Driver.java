package org.mixi.analysis.hive.dependency.parser;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;

import java.io.*;

/**
 * Created by hikaru.ojima on 2014/02/17.
 */
public class Driver {
    private static Logger logger = new Logger();

    public static Dependency processFile(String path) {
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
            logger.error("File: " + path + " could not be read.", e);
        }

        return processLine(qsb.toString());
    }

    public static Dependency processLine(String line) {
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
    public static Dependency processCmd(String cmd) {
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
                ParseDriver driver = new ParseDriver();
                ASTNode tree;

                tree = driver.parse(cmd);
                tree = ParseUtils.findRootNonNullToken(tree);
                return TreeParser.parse(tree);

            } catch (ParseException e) {
                logger.error("ParseFailed: " + cmd, e);
                return Dependency.NULL;
            }
        }
    }

    public static void run(String[] args) throws ParseException {
    }

    public static void main(String[] args) throws ParseException {
        run(args);
    }

    private static String getFirstCmd(String cmd, int length) {
        return cmd.substring(length).trim();
    }
}
