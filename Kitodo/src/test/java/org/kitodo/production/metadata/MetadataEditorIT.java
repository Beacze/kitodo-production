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

package org.kitodo.production.metadata;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.file.FileService;

public class MetadataEditorIT {
    private static FileService fileService = new FileService();
    private static final ProcessService processService = ServiceManager.getProcessService();

    private static final String firstProcess = "First process";

    /**
     * Is running before the class runs.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertProcessesForHierarchyTests();
        MockDatabase.setUpAwaitility();
        fileService.createDirectory(URI.create(""), "1");
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        if (System.getProperty("java.class.path").contains("eclipse")) {
            while (Objects.isNull(processService.findByTitle(firstProcess))) {
                Thread.sleep(50);
            }
        } else {
            await().untilTrue(new AtomicBoolean(Objects.nonNull(processService.findByTitle(firstProcess))));
        }
    }

    /**
     * Is running after the class has run.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        fileService.delete(URI.create("1"));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldAddLink() throws Exception {
        File metaXmlFile = new File("src/test/resources/metadata/4/meta.xml");
        List<String> metaXmlContentBefore = FileUtils.readLines(metaXmlFile, StandardCharsets.UTF_8);

        MetadataEditor.addLink(ServiceManager.getProcessService().getById(4), "0", 7);

        assertTrue("The link was not added correctly!",
            isInternalMetsLink(FileUtils.readLines(metaXmlFile, StandardCharsets.UTF_8).get(35), 7));

        FileUtils.writeLines(metaXmlFile, StandardCharsets.UTF_8.toString(), metaXmlContentBefore);
        FileUtils.deleteQuietly(new File("src/test/resources/metadata/4/meta.xml.1"));
    }

    private boolean isInternalMetsLink(String lineOfMets, int recordNumber) {
        // Order of <mptr> attributes varies
        boolean isInternalMetsLink = lineOfMets.contains("mptr ") && lineOfMets.contains("LOCTYPE=\"OTHER\"")
                && lineOfMets.contains("OTHERLOCTYPE=\"Kitodo.Production\"")
                && lineOfMets.contains("href=\"database://?process.id=" + recordNumber + "\"");
        return isInternalMetsLink;
    }
}
