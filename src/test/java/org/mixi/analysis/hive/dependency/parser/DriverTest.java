package org.mixi.analysis.hive.dependency.parser;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by hikaru.ojima on 2014/02/17.
 */
public class DriverTest extends TestCase {
    public void testMultipleSelect() {
        File file = FileUtils.getFile("src", "test", "resources", "test_multiple_select.sql");
        Dependency dependency = Driver.processFile(file.getPath());

        ArrayList<String> src = dependency.getSources();
        assertEquals(4, src.size());
        assertTrue(src.contains("p.a"));
        assertTrue(src.contains("p.b"));
        assertTrue(src.contains("p.c"));
        assertTrue(src.contains("p.d"));
    }
    public void testInsertSelect() {
        File file = FileUtils.getFile("src", "test", "resources", "test_insert_select.sql");
        Dependency dependency = Driver.processFile(file.getPath());

        ArrayList<String> src = dependency.getSources();
        ArrayList<String> dst = dependency.getDestinations();
        assertEquals(2, src.size());
        assertTrue(src.contains("p.a"));
        assertTrue(src.contains("p.b"));

        assertEquals(2, dst.size());
        assertTrue(dst.contains("q.a"));
        assertTrue(dst.contains("q.b"));
    }
    public void testUnion() {
        File file = FileUtils.getFile("src", "test", "resources", "test_union.sql");
        Dependency dependency = Driver.processFile(file.getPath());

        ArrayList<String> src = dependency.getSources();
        assertEquals(4, src.size());
        assertTrue(src.contains("p.a"));
        assertTrue(src.contains("p.b"));
        assertTrue(src.contains("p.c"));
        assertTrue(src.contains("p.d"));
    }
    public void testJoin() {
        File file = FileUtils.getFile("src", "test", "resources", "test_join.sql");
        Dependency dependency = Driver.processFile(file.getPath());

        ArrayList<String> src = dependency.getSources();
        assertEquals(4, src.size());
        assertTrue(src.contains("p.a"));
        assertTrue(src.contains("p.b"));
        assertTrue(src.contains("p.c"));
        assertTrue(src.contains("p.d"));
    }
    public void testLoad() {
        File file = FileUtils.getFile("src", "test", "resources", "test_load.sql");
        Dependency dependency = Driver.processFile(file.getPath());

        ArrayList<String> dst = dependency.getDestinations();
        assertEquals(1, dst.size());
        assertTrue(dst.contains("p.a"));

    }
}
