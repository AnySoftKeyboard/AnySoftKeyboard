package com.anysoftkeyboard.utils;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.StringWriter;

@RunWith(AnySoftKeyboardTestRunner.class)
public class XmlWriterTest {

    @Test
    public void testHappyPath() throws IOException {
        StringWriter writer = new StringWriter();
        XmlWriter xmlwriter = new XmlWriter(writer, true, 0, true);
        xmlwriter.writeEntity("person");
        xmlwriter.writeAttribute("name", "fred");
        xmlwriter.writeAttribute("age", "12");
        xmlwriter.writeEntity("phone");
        xmlwriter.writeText("4254343");
        xmlwriter.endEntity();
        xmlwriter.writeEntity("bob");
        xmlwriter.endEntity();
        xmlwriter.endEntity();
        xmlwriter.close();
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<person name=\"fred\" age=\"12\">\n" +
                "    <phone>4254343</phone>\n" +
                "    <bob/>\n" +
                "</person>\n", writer.getBuffer().toString());
    }

    @Test
    public void testHappyPath2() throws IOException {
        StringWriter writer = new StringWriter();
        XmlWriter xmlwriter = new XmlWriter(writer, true, 1, false);
        xmlwriter.writeEntity("person").writeAttribute("name", "fred").writeAttribute("age", "12").writeEntity("phone").writeText("4254343").endEntity().writeEntity("bob").endEntity().endEntity();
        xmlwriter.close();
        Assert.assertEquals("    <person name=\"fred\" age=\"12\">\n" +
                "        <phone>4254343</phone>\n" +
                "        <bob/>\n" +
                "    </person>\n", writer.getBuffer().toString());
    }

}