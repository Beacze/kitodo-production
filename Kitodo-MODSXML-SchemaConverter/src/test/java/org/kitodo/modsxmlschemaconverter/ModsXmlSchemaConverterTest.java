/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.modsxmlschemaconverter;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ModsXmlSchemaConverterTest {

    private static ModsXMLSchemaConverter converter = new ModsXMLSchemaConverter();
    private static final String TEST_FILE_PATH = "src/test/resources/modsXmlTestRecord.xml";

    @Test
    public void shouldConvertToInternalFormat() throws IOException, ParserConfigurationException, SAXException {

        DataRecord testRecord = new DataRecord();
        testRecord.setMetadataFormat(MetadataFormat.MODS);
        testRecord.setFileFormat(FileFormat.XML);

        DataRecord internalFormatRecord;

        try (InputStream inputStream = Files.newInputStream(Paths.get(TEST_FILE_PATH))) {
            testRecord.setOriginalData(IOUtils.toString(inputStream, Charset.defaultCharset()));
            internalFormatRecord = converter.convert(testRecord, MetadataFormat.KITODO, FileFormat.XML, null);
        }

        Assert.assertNotNull("Conversion result is empty!", internalFormatRecord);
        Assert.assertEquals("Conversion result has wrong MetadataFormat!",
                internalFormatRecord.getMetadataFormat(), MetadataFormat.KITODO);
        Assert.assertEquals("Conversion result has wrong FileFormat!",
                internalFormatRecord.getFileFormat(), FileFormat.XML);
        Assert.assertThat("Wrong class of original data object!",
                internalFormatRecord.getOriginalData(), instanceOf(String.class));
        Document resultDocument = parseInputStreamToDocument((String) internalFormatRecord.getOriginalData());
        NodeList metadataNodes = resultDocument.getElementsByTagName("kitodo:metadata");

        String title = "";
        String catalogId = "";
        String year = "";
        for (int i = 0; i < metadataNodes.getLength(); i++) {
            Element element = (Element) metadataNodes.item(i);
            switch (element.getAttribute("name")) {
                case "CatalogIDDigital":
                    catalogId = element.getTextContent();
                    break;
                case "TitleDocMain":
                    title = element.getTextContent();
                    break;
                case "PublicationYear":
                    year = element.getTextContent();
                    break;
                default:
                    // ignore other elements
                    break;
            }
        }

        Assert.assertEquals("Title after conversion is wrong!", title, "Test-Title");
        Assert.assertEquals("Catalog ID after conversion is wrong!", catalogId, "67890");
        Assert.assertEquals("PublicationYear after conversion is wrong!", year, "1999-12-31");
    }

    private Document parseInputStreamToDocument(String inputString) throws ParserConfigurationException,
            IOException, SAXException {
        try (InputStream inputStream = new ByteArrayInputStream(inputString.getBytes())) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            return documentBuilderFactory.newDocumentBuilder().parse(inputStream);
        }
    }
}
