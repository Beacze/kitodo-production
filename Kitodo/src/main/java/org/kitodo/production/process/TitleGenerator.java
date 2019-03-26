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

package org.kitodo.production.process;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.AdditionalField;

public class TitleGenerator extends Generator {

    private static final String LIST_OF_CREATORS = "ListOfCreators";
    private static final String TITLE_DOC_MAIN = "TitleDocMain";

    /**
     * Constructor for TitleGenerator.
     *
     * @param atstsl
     *            field used for title generation
     * @param additionalFields
     *            fields used for title generation
     */
    public TitleGenerator(String atstsl, List<AdditionalField> additionalFields) {
        super(atstsl, additionalFields);
    }

    /**
     * Generate title for process.
     *
     * @param titleDefinition
     *            definition for title to generation
     * @param genericFields
     *            Map of Strings
     * @return String
     */
    public String generateTitle(String titleDefinition, Map<String, String> genericFields)
            throws ProcessGenerationException {
        String currentAuthors = getCurrentValue(LIST_OF_CREATORS);
        String currentTitle = getCurrentValue(TITLE_DOC_MAIN);

        StringBuilder newTitle = new StringBuilder();

        StringTokenizer tokenizer = new StringTokenizer(titleDefinition, "+");
        // parse the band title
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            // if the string begins with ' and ends with ' then take over the content
            if (token.startsWith("'") && token.endsWith("'")) {
                newTitle.append(token, 1, token.length() - 1);
            } else if (token.startsWith("#")) {
                // resolve strings beginning with # from generic fields
                if (Objects.nonNull(genericFields)) {
                    String genericValue = genericFields.get(token);
                    if (Objects.nonNull(genericValue)) {
                        newTitle.append(genericValue);
                    }
                }
            } else {
                newTitle.append(evaluateAdditionalFields(currentTitle, currentAuthors, token));
            }
        }

        if (newTitle.toString().endsWith("_")) {
            newTitle.setLength(newTitle.length() - 1);
        }
        // remove non-ascii characters for the sake of TIFF header limits
        return newTitle.toString().replaceAll("[^\\p{ASCII}]", "");
    }

    /**
     * Create Atstsl.
     *
     * @param title
     *            String
     * @param author
     *            String
     * @return String
     */
    public static String createAtstsl(String title, String author) {
        StringBuilder result = new StringBuilder(8);
        if (Objects.nonNull(author) && !author.trim().isEmpty()) {
            result.append(getPartString(author, 4));
            result.append(getPartString(title, 4));
        } else {
            StringTokenizer titleWords = new StringTokenizer(title);
            int wordNo = 1;
            while (titleWords.hasMoreTokens() && wordNo < 5) {
                String word = titleWords.nextToken();
                switch (wordNo) {
                    case 1:
                        result.append(getPartString(word, 4));
                        break;
                    case 2:
                    case 3:
                        result.append(getPartString(word, 2));
                        break;
                    case 4:
                        result.append(getPartString(word, 1));
                        break;
                    default:
                        break;
                }
                wordNo++;
            }
        }
        return result.toString().replaceAll("[\\W]", ""); // delete umlauts etc.
    }

    private String getCurrentValue(String metadataTag) {
        int counter = 0;
        for (AdditionalField field : this.additionalFields) {
            if (field.getAutogenerated() && field.getValue().isEmpty()) {
                field.setValue(String.valueOf(System.currentTimeMillis() + counter));
                counter++;
            }

            String metadata = field.getMetadata();
            if (Objects.nonNull(metadata) && metadata.equals(metadataTag)) {
                return field.getValue();
            }
        }
        return "";
    }

    private String evaluateAdditionalFields(String currentTitle, String currentAuthors, String token)
            throws ProcessGenerationException {
        StringBuilder newTitle = new StringBuilder();

        for (AdditionalField additionalField : this.additionalFields) {
            String title = additionalField.getTitle();
            String value = additionalField.getValue();
            boolean showDependingOnDoctype = additionalField.getShowDependingOnDoctype();
            /*
             * if it is the ATS or TSL field, then use the calculated atstsl if it does not
             * already exist
             */
            if (("ATS".equals(title) || "TSL".equals(title)) && showDependingOnDoctype && StringUtils.isEmpty(value)) {
                if (StringUtils.isEmpty(this.atstsl)) {
                    this.atstsl = createAtstsl(currentTitle, currentAuthors);
                }
                additionalField.setValue(this.atstsl);
                value = additionalField.getValue();
            }

            // add the content to the title
            if (title.equals(token) && showDependingOnDoctype && Objects.nonNull(value)) {
                newTitle.append(calculateProcessTitleCheck(title, value));
            }
        }
        return newTitle.toString();
    }

    private static String getPartString(String word, int length) {
        return word.length() > length ? word.substring(0, length) : word;
    }
}
