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

package org.kitodo.dataeditor.ruleset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.dataeditor.ruleset.xml.AcquisitionStage;
import org.kitodo.dataeditor.ruleset.xml.Division;
import org.kitodo.dataeditor.ruleset.xml.Key;
import org.kitodo.dataeditor.ruleset.xml.Namespace;
import org.kitodo.dataeditor.ruleset.xml.Ruleset;

/**
 * This class provides the functionality of the rule set.
 */
public class RulesetManagement implements RulesetManagementInterface {
    /**
     * A logger can be used to keep a log.
     */
    private static final Logger logger = LogManager.getLogger(RulesetManagement.class);

    /**
     * The ruleset.
     */
    private Ruleset ruleset;

    /**
     * Returns the acquisition levels defined in this rule set. This function
     * was not parallelized to repeatedly serve JSF in the same order when the
     * function is called repeatedly.
     *
     * @return all acquisition levels showing up
     */
    @Override
    public Collection<String> getAcquisitionStages() {
        List<AcquisitionStage> acquisitionStages = ruleset.getAcquisitionStages();
        List<String> acquisitionStageNames = new ArrayList<>(acquisitionStages.size());
        for (AcquisitionStage acquisitionStage : acquisitionStages) {
            acquisitionStageNames.add(acquisitionStage.getName());
        }
        return acquisitionStageNames;
    }

    /**
     * Returns a translated list of divisions available in the rule set. The map
     * maps from ID to label.
     *
     * @return the list of divisions
     */
    @Override
    public Map<String, String> getStructuralElements(List<LanguageRange> priorityList) {
        return ruleset.getDivisions(priorityList, true);
    }

    /**
     * Opens a view on a division of the rule set.
     *
     * @param divisionId
     *            the division in view
     * @param acquisitionStage
     *            the current acquisition level
     * @param priorityList
     *            the wish list of the user regarding its preferred human
     *            languages
     * @return a view on a division
     */
    @Override
    public StructuralElementViewInterface getStructuralElementView(String divisionId, String acquisitionStage,
            List<LanguageRange> priorityList) {

        Optional<Division> division = ruleset.getDivision(divisionId);
        UniversalDivision universalDivision = division.isPresent() ? new UniversalDivision(ruleset, division.get())
                : new UniversalDivision(ruleset, divisionId);
        return new DivisionView(ruleset, universalDivision, acquisitionStage, priorityList);
    }

    /**
     * Loads a ruleset from a file.
     *
     * @param rulesetFile
     *            file to load
     * @throws IOException
     *             if something goes wrong when reading
     */
    @Override
    public void load(File rulesetFile) throws IOException {
        this.ruleset = read(Ruleset.class, rulesetFile);
        initializeNamespaces(ruleset.getKeys(), rulesetFile.getParentFile());
    }

    /**
     * Initializes the elements of namespaces if there is a corresponding file.
     *
     * @param keys
     *            the keys of the rule set (are processed recursively)
     * @param home
     *            the ruleset directory
     * @throws IOException
     *             if I/O fails
     */
    private void initializeNamespaces(List<Key> keys, File home) throws IOException {
        for (Key key : keys) {
            if (key.getNamespace().isPresent()) {
                String namespaceURI = key.getNamespace().get();
                File file = new File(home, namespaceURI.replaceFirst("^.*?/([^/]*?)[#/]?$", "$1").concat(".xml"));
                if (file.isFile()) {
                    try {
                        Namespace namespace = read(Namespace.class, file);
                        if (namespace.isAbout(namespaceURI)) {
                            key.setOptions(namespace.getOptions());
                        } else {
                            logger.debug(
                                "The file {} for the namespace {} declares an inappropriate namespace. (Check about.)",
                                file, namespaceURI);
                        }
                    } catch (IOException e) {
                        logger.debug("The file {} for the namespace {} cannot be parsed: {}", file, namespaceURI,
                            e.getMessage());
                    }
                } else {
                    logger.debug("The file {} for the namespace {} was not found or is unreadable.", file,
                        namespaceURI);
                }
            }
            // is applied recursively to the sub-elements
            initializeNamespaces(key.getKeys(), home);
        }
    }

    /**
     * Reads an object from a file. For this purpose, a marshal eliminator of
     * Java XML bindings is created, which eliminates the class of marshals and
     * creates it as a Java object. As an error, an I/O exception is thrown out
     * directly. Other marshal eliminator exceptions are packed in I/O
     * exceptions, so the interface is independent of the parser used.
     *
     * @param objectClass
     *            class of object to read
     * @param inputFile
     *            file to read from
     * @return the read object
     * @throws IOException
     *             if I/O fails
     */
    @SuppressWarnings("unchecked")
    private <T> T read(Class<T> objectClass, File inputFile) throws IOException {
        try {
            Unmarshaller reader = JAXBContext.newInstance(objectClass).createUnmarshaller();
            return (T) reader.unmarshal(inputFile);
        } catch (JAXBException e) {
            /*
             * If the parser ran on an IOException, we can throw it out
             * directly, because that allows the method signature.
             */
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                /*
                 * Conversely, parser exceptions must be wrapped in an
                 * IOException because the method signature does not allow the
                 * parser exceptions.
                 */
                throw new IOException(e.getMessage(), e);
            }
        }
    }
}
