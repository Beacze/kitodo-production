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

package org.kitodo.services.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.exceptions.DocStructHasNoTypeException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.api.validation.metadata.MetadataValidationInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.ConfigProject;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.UghHelperException;
import org.kitodo.helper.Helper;
import org.kitodo.helper.UghHelper;
import org.kitodo.helper.metadata.LegacyDocStructHelperInterface;
import org.kitodo.helper.metadata.LegacyLogicalDocStructTypeHelper;
import org.kitodo.helper.metadata.LegacyMetadataHelper;
import org.kitodo.helper.metadata.LegacyMetadataTypeHelper;
import org.kitodo.helper.metadata.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.helper.metadata.LegacyPrefsHelper;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.kitodo.services.ServiceManager;

public class MetadataValidationService {

    private List<LegacyDocStructHelperInterface> docStructsOhneSeiten;
    private Process process;
    private boolean autoSave = false;
    private static final Logger logger = LogManager.getLogger(MetadataValidationService.class);
    private static final String VALIDATE_METADATA = "validate.metadata";

    /**
     * Validate.
     *
     * @param process
     *            object
     * @return boolean
     */
    public boolean validate(Process process) {
        LegacyPrefsHelper prefs = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        LegacyMetsModsDigitalDocumentHelper gdzfile;
        try {
            gdzfile = ServiceManager.getProcessService().readMetadataFile(process);
        } catch (PreferencesException | IOException | ReadException | RuntimeException e) {
            Helper.setErrorMessage("metadataReadError", new Object[] {process.getTitle() }, logger, e);
            return false;
        }
        return validate(gdzfile, prefs, process);
    }

    /**
     * Validate.
     *
     * @param gdzfile
     *            Fileformat object
     * @param prefs
     *            Prefs object
     * @param process
     *            object
     * @return boolean
     */
    public boolean validate(LegacyMetsModsDigitalDocumentHelper gdzfile, LegacyPrefsHelper prefs, Process process) {
        String metadataLanguage = ServiceManager.getUserService().getAuthenticatedUser().getMetadataLanguage();
        this.process = process;
        boolean result = true;

        LegacyMetsModsDigitalDocumentHelper dd;
        try {
            dd = gdzfile.getDigitalDocument();
        } catch (RuntimeException e) {
            Helper.setErrorMessage(Helper.getTranslation("metadataDigitalDocumentError") + process.getTitle(), logger,
                e);
            return false;
        }

        LegacyDocStructHelperInterface logical = dd.getLogicalDocStruct();
        throw new UnsupportedOperationException("Dead code pending removal");
    }

    private boolean isStringListIncorrect(List<String> strings, String messageTitle) {
        boolean incorrect = false;
        if (Objects.nonNull(strings)) {
            for (String string : strings) {
                Helper.setErrorMessage(process.getTitle() + ": " + Helper.getTranslation(messageTitle), string);
            }
            incorrect = true;
        }
        return incorrect;
    }

    private boolean isMetadataValueReplaced(LegacyDocStructHelperInterface docStruct, LegacyMetadataHelper metadata,
            String metadataLanguage) {

        if (!metadata.getValue().replaceAll(ConfigCore
                .getParameterOrDefaultValue(ParameterCore.VALIDATE_IDENTIFIER_REGEX), "").equals("")) {
            throw new UnsupportedOperationException("Dead code pending removal");
        }
        return false;
    }

    private boolean isValidPathImageFiles(LegacyDocStructHelperInterface phys, LegacyPrefsHelper myPrefs) {
        try {
            LegacyMetadataTypeHelper mdt = UghHelper.getMetadataType(myPrefs, "pathimagefiles");
            List<? extends LegacyMetadataHelper> allMetadata = phys.getAllMetadataByType(mdt);
            if (Objects.nonNull(allMetadata) && !allMetadata.isEmpty()) {
                return true;
            } else {
                Helper.setErrorMessage(this.process.getTitle(), "Can not verify, image path is not set");
                return false;
            }
        } catch (UghHelperException e) {
            Helper.setErrorMessage(this.process.getTitle() + ": " + "Verify aborted, error: ", e.getMessage(), logger,
                e);
            return false;
        }
    }

    private void checkDocStructsOhneSeiten(LegacyDocStructHelperInterface docStruct) {
        if (docStruct.getAllToReferences().isEmpty() && docStruct.getDocStructType().getAnchorClass() == null) {
            this.docStructsOhneSeiten.add(docStruct);
        }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (docStruct.getAllChildren() != null) {
            for (LegacyDocStructHelperInterface child : docStruct.getAllChildren()) {
                checkDocStructsOhneSeiten(child);
            }
        }
    }

    private List<String> checkSeitenOhneDocstructs(LegacyMetsModsDigitalDocumentHelper inRdf) throws PreferencesException {
        List<String> result = new ArrayList<>();
        LegacyDocStructHelperInterface boundBook = inRdf.getDigitalDocument().getPhysicalDocStruct();
        // if boundBook is null
        if (boundBook == null || boundBook.getAllChildren() == null) {
            return result;
        }

        /* alle Seiten durchlaufen und prüfen ob References existieren */
        for (LegacyDocStructHelperInterface docStruct : boundBook.getAllChildren()) {
            throw new UnsupportedOperationException("Dead code pending removal");
        }
        return result;
    }

    private String collectLogicalAndPhysicalStructure(LegacyDocStructHelperInterface docStruct) {
        String physical = "";
        String logical = "";

        for (LegacyMetadataHelper metadata : docStruct.getAllMetadata()) {
            if (metadata.getMetadataType().getName().equals("logicalPageNumber")) {
                logical = " (" + metadata.getValue() + ")";
            }
            if (metadata.getMetadataType().getName().equals("physPageNumber")) {
                physical = metadata.getValue();
            }
        }

        return physical + logical;
    }

    private List<String> checkMandatoryValues(LegacyDocStructHelperInterface docStruct, ArrayList<String> list, String language) {
        LegacyLogicalDocStructTypeHelper dst = docStruct.getDocStructType();
        List<LegacyMetadataTypeHelper> allMDTypes = dst.getAllMetadataTypes();
        for (LegacyMetadataTypeHelper mdt : allMDTypes) {
            String number = dst.getNumberOfMetadataType(mdt);
            List<? extends LegacyMetadataHelper> ll = docStruct.getAllMetadataByType(mdt);
            int real = ll.size();
            // if (ll.size() > 0) {

            if ((number.equals("1m") || number.equals("+")) && real == 1
                    && (ll.get(0).getValue() == null || ll.get(0).getValue().equals(""))) {

                throw new UnsupportedOperationException("Dead code pending removal");
            }
            // check types
            if (number.equals("1m") && real != 1) {
                throw new UnsupportedOperationException("Dead code pending removal");
            }
            if (number.equals("1o") && real > 1) {
                throw new UnsupportedOperationException("Dead code pending removal");
            }
            if (number.equals("+") && real == 0) {
                throw new UnsupportedOperationException("Dead code pending removal");
            }
        }
        // }
        /* alle Kinder des aktuellen DocStructs durchlaufen */
        if (docStruct.getAllChildren() != null) {
            for (LegacyDocStructHelperInterface child : docStruct.getAllChildren()) {
                checkMandatoryValues(child, list, language);
            }
        }
        return list;
    }

    /**
     * individuelle konfigurierbare projektspezifische Validierung der
     * Metadaten.
     */
    private List<String> checkConfiguredValidationValues(LegacyDocStructHelperInterface docStruct, ArrayList<String> errorList,
            LegacyPrefsHelper prefs, String language) {
        // open configuration and read the validation details
        ConfigProject cp;
        try {
            cp = new ConfigProject(this.process.getProject().getTitle());
        } catch (IOException e) {
            Helper.setErrorMessage(process.getTitle(), logger, e);
            return errorList;
        }
        int count = cp.getParamList(VALIDATE_METADATA).size();
        for (int i = 0; i < count; i++) {
            // evaluate attributes
            String propMetadatatype = cp.getParamString(VALIDATE_METADATA + "(" + i + ")[@metadata]");
            String propDoctype = cp.getParamString(VALIDATE_METADATA + "(" + i + ")[@docstruct]");
            String propStartswith = cp.getParamString(VALIDATE_METADATA + "(" + i + ")[@startswith]");
            String propEndswith = cp.getParamString(VALIDATE_METADATA + "(" + i + ")[@endswith]");
            String propCreateElementFrom = cp.getParamString(VALIDATE_METADATA + "(" + i + ")[@createelementfrom]");
            LegacyMetadataTypeHelper mdt = null;
            try {
                mdt = UghHelper.getMetadataType(prefs, propMetadatatype);
            } catch (UghHelperException e) {
                Helper.setErrorMessage("[" + this.process.getTitle() + "] " + "Metadatatype does not exist: ",
                    propMetadatatype, logger, e);
            }
            /*
             * wenn das Metadatum des FirstChilds überprüfen werden soll, dann
             * dieses jetzt (sofern vorhanden) übernehmen
             */
            if (propDoctype != null && propDoctype.equals("firstchild")) {
                if (Objects.nonNull(docStruct.getAllChildren()) && !docStruct.getAllChildren().isEmpty()) {
                    docStruct = docStruct.getAllChildren().get(0);
                } else {
                    continue;
                }
            }

            /*
             * wenn der MetadatenTyp existiert, dann jetzt die nötige Aktion
             * überprüfen
             */
            if (mdt != null) {
                /* ein CreatorsAllOrigin soll erzeugt werden */
                if (propCreateElementFrom != null) {
                    List<LegacyMetadataTypeHelper> listOfFromMdts = prepareMetadataTypes(prefs, propCreateElementFrom);
                    if (!listOfFromMdts.isEmpty()) {
                        checkCreateElementFrom(listOfFromMdts, docStruct, mdt, language);
                    }
                } else {
                    checkStartsEndsWith(errorList, propStartswith, propEndswith, docStruct, mdt, language);
                }
            }
        }
        return errorList;
    }

    private List<LegacyMetadataTypeHelper> prepareMetadataTypes(LegacyPrefsHelper prefs, String propCreateElementFrom) {
        List<LegacyMetadataTypeHelper> metadataTypes = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(propCreateElementFrom, "|");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            try {
                LegacyMetadataTypeHelper emdete = UghHelper.getMetadataType(prefs, token);
                metadataTypes.add(emdete);
            } catch (UghHelperException e) {
                /*
                 * if the compilation does not exist for creatorsAllOrigin as
                 * the metadata type, fetch exception and do not elaborate on it
                 */
            }
        }
        return metadataTypes;
    }

    private void saveMetadataFile(LegacyMetsModsDigitalDocumentHelper gdzfile, Process process) {
        try {
            if (this.autoSave) {
                ServiceManager.getFileService().writeMetadataFile(gdzfile, process);
            }
        } catch (PreferencesException | WriteException | IOException | RuntimeException e) {
            Helper.setErrorMessage("Error while writing metadata: ", process.getTitle(), logger, e);
        }
    }

    /**
     * Create Element From - für alle Strukturelemente ein bestimmtes Metadatum
     * erzeugen, sofern dies an der jeweiligen Stelle erlaubt und noch nicht
     * vorhanden.
     */
    private void checkCreateElementFrom(List<LegacyMetadataTypeHelper> metadataTypes, LegacyDocStructHelperInterface docStruct,
            LegacyMetadataTypeHelper mdt, String language) {
        /*
         * existiert das zu erzeugende Metadatum schon, dann überspringen,
         * ansonsten alle Daten zusammensammeln und in das neue Element
         * schreiben
         */
        List<? extends LegacyMetadataHelper> createMetadata = docStruct.getAllMetadataByType(mdt);
        if (createMetadata == null || createMetadata.isEmpty()) {
            try {
                LegacyMetadataHelper createdElement = new LegacyMetadataHelper(mdt);
                String value = "";
                // go through all the metadata to append and append to the
                // element
                for (LegacyMetadataTypeHelper metadataType : metadataTypes) {
                    List<PersonInterface> fromElemente = docStruct.getAllPersons();
                    if (Objects.nonNull(fromElemente) && !fromElemente.isEmpty()) {
                        value = iterateOverPersons(fromElemente, docStruct, language, metadataType);
                    }
                }

                if (!value.isEmpty()) {
                    createdElement.setStringValue(value);
                    docStruct.addMetadata(createdElement);
                }
            } catch (DocStructHasNoTypeException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // go through all children
        List<LegacyDocStructHelperInterface> children = docStruct.getAllChildren();
        if (Objects.nonNull(children)) {
            for (LegacyDocStructHelperInterface child : children) {
                checkCreateElementFrom(metadataTypes, child, mdt, language);
            }
        }
    }

    private String iterateOverPersons(List<PersonInterface> persons, LegacyDocStructHelperInterface docStruct, String language,
            LegacyMetadataTypeHelper metadataType) {
        StringBuilder value = new StringBuilder();
        for (PersonInterface p : persons) {
            if (p.getRole() == null) {
                Helper.setErrorMessage(
                    "[" + this.process.getTitle() + " " + docStruct.getDocStructType().getNameByLanguage(language)
                            + "] " + Helper.getTranslation("metadataPersonWithoutRole"));
                break;
            } else {
                if (p.getRole().equals(metadataType.getName())) {
                    if (value.length() > 0) {
                        value.append("; ");
                    }
                    value.append(p.getLastName());
                    value.append(", ");
                    value.append(p.getFirstName());
                }
            }
        }
        return value.toString();
    }

    /**
     * Metadata should start or end with a certain string.
     *
     * @param errorList
     *            list of errors
     * @param propStartsWith
     *            check if starts with this String
     * @param propEndsWith
     *            check if ends with this String
     * @param myStruct
     *            DocStruct
     * @param mdt
     *            MetadataType
     * @param language
     *            as String
     */
    private void checkStartsEndsWith(List<String> errorList, String propStartsWith, String propEndsWith,
            LegacyDocStructHelperInterface myStruct, LegacyMetadataTypeHelper mdt, String language) {
        // starts with or ends with
        List<? extends LegacyMetadataHelper> allMetadataByType = myStruct.getAllMetadataByType(mdt);
        if (Objects.nonNull(allMetadataByType)) {
            for (LegacyMetadataHelper md : allMetadataByType) {
                /* prüfen, ob es mit korrekten Werten beginnt */
                if (propStartsWith != null) {
                    boolean isOk = false;
                    StringTokenizer tokenizer = new StringTokenizer(propStartsWith, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        if (md.getValue() != null && md.getValue().startsWith(tok)) {
                            isOk = true;
                        }
                    }
                    if (!isOk && !this.autoSave) {
                        throw new UnsupportedOperationException("Dead code pending removal");
                    }
                    if (!isOk && this.autoSave) {
                        md.setStringValue(new StringTokenizer(propStartsWith, "|").nextToken() + md.getValue());
                    }
                }
                /* prüfen, ob es mit korrekten Werten endet */
                if (propEndsWith != null) {
                    boolean isOk = false;
                    StringTokenizer tokenizer = new StringTokenizer(propEndsWith, "|");
                    while (tokenizer.hasMoreTokens()) {
                        String tok = tokenizer.nextToken();
                        if (md.getValue() != null && md.getValue().endsWith(tok)) {
                            isOk = true;
                        }
                    }
                    if (!isOk && !this.autoSave) {
                        throw new UnsupportedOperationException("Dead code pending removal");
                    }
                    if (!isOk && this.autoSave) {
                        md.setStringValue(md.getValue() + new StringTokenizer(propEndsWith, "|").nextToken());
                    }
                }
            }
        }
    }

    /**
     * Check if automatic save is allowed.
     *
     * @return true or false
     */
    public boolean isAutoSave() {
        return this.autoSave;
    }

    /**
     * Set if automatic save is allowed.
     *
     * @param autoSave
     *            true or false
     */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    private MetadataValidationInterface getValidationModule() {
        KitodoServiceLoader<MetadataValidationInterface> loader = new KitodoServiceLoader<>(
                MetadataValidationInterface.class);
        return loader.loadModule();
    }
}
