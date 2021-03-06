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

package org.kitodo;

import java.util.Objects;

import org.joda.time.LocalDate;
import org.kitodo.production.model.bibliography.course.Block;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.Issue;

public class NewspaperCourse {
    private static Course course;

    /**
     * Returns a course of appearance.
     *
     * @return a course of appearance
     */
    public static Course getCourse() {
        if (Objects.isNull(course)) {
            course = createCourse();
        }
        return course;
    }

    private static Course createCourse() {
        Course course = new Course();
        addFirstBlock(course);
        // addSecondBlock(course); // currently locked to speed up the test
        // addThirdBlock(course);
        // addFourthBlock(course);
        // addFifthBlock(course);
        // addSixthBlock(course);
        // addSeventhBlock(course);
        return course;
    }

    private static void addFirstBlock(Course course) {
        Block block = new Block(course);
        block.setPublicationPeriod(new LocalDate(1703, 8, 8), new LocalDate(1703, 12, 31));
        Issue issue = new Issue(course);
        issue.addMonday();
        issue.addThursday();
        issue.addAddition(new LocalDate(1703, 8, 8));
        issue.addExclusion(new LocalDate(1703, 8, 9));
        issue.addExclusion(new LocalDate(1703, 8, 16));
        issue.addAddition(new LocalDate(1703, 8, 17));
        issue.addExclusion(new LocalDate(1703, 8, 30));
        issue.addAddition(new LocalDate(1703, 8, 31));
        issue.addAddition(new LocalDate(1703, 9, 6));
        block.addIssue(issue);
        course.add(block);
    }

    private static void addSecondBlock(Course course) {
        Block block = new Block(course);
        block.setPublicationPeriod(new LocalDate(1704, 12, 31), new LocalDate(1713, 7, 22));
        Issue issue = new Issue(course);
        issue.addWednesday();
        issue.addSaturday();
        issue.addAddition(new LocalDate(1705, 1, 27));
        issue.addExclusion(new LocalDate(1705, 1, 28));
        issue.addAddition(new LocalDate(1705, 4, 8));
        issue.addExclusion(new LocalDate(1705, 4, 7));
        issue.addAddition(new LocalDate(1705, 11, 10));
        issue.addExclusion(new LocalDate(1705, 11, 11));
        issue.addAddition(new LocalDate(1706, 3, 16));
        issue.addExclusion(new LocalDate(1706, 3, 17));
        issue.addExclusion(new LocalDate(1706, 5, 1));
        issue.addAddition(new LocalDate(1706, 5, 2));
        issue.addExclusion(new LocalDate(1707, 7, 27));
        issue.addAddition(new LocalDate(1707, 7, 29));
        issue.addExclusion(new LocalDate(1708, 9, 19));
        issue.addAddition(new LocalDate(1708, 9, 21));
        issue.addExclusion(new LocalDate(1709, 5, 25));
        issue.addExclusion(new LocalDate(1711, 5, 23));
        issue.addExclusion(new LocalDate(1711, 5, 27));
        issue.addExclusion(new LocalDate(1711, 5, 30));
        issue.addExclusion(new LocalDate(1711, 6, 24));
        issue.addExclusion(new LocalDate(1711, 6, 27));
        issue.addExclusion(new LocalDate(1712, 6, 11));
        issue.addAddition(new LocalDate(1712, 6, 14));
        issue.addExclusion(new LocalDate(1712, 9, 10));
        issue.addExclusion(new LocalDate(1712, 9, 14));
        issue.addExclusion(new LocalDate(1712, 12, 31));
        issue.addExclusion(new LocalDate(1713, 1, 21));
        issue.addAddition(new LocalDate(1713, 1, 22));
        issue.addExclusion(new LocalDate(1713, 2, 22));
        issue.addExclusion(new LocalDate(1713, 2, 25));
        issue.addExclusion(new LocalDate(1713, 4, 12));
        issue.addAddition(new LocalDate(1713, 4, 13));
        block.addIssue(issue);
        course.add(block);
    }

    private static void addThirdBlock(Course course) {
        Block block = new Block(course);
        block.setPublicationPeriod(new LocalDate(1713, 9, 13), new LocalDate(1714, 2, 17));
        Issue issue = new Issue(course);
        issue.addWednesday();
        issue.addSaturday();
        issue.addAddition(new LocalDate(1713, 10, 17));
        issue.addExclusion(new LocalDate(1713, 10, 18));
        block.addIssue(issue);
        course.add(block);
    }

    private static void addFourthBlock(Course course) {
        Block block = new Block(course);
        block.setPublicationPeriod(new LocalDate(1714, 4, 28), new LocalDate(1714, 10, 19));
        Issue issue = new Issue(course);
        issue.addAddition(new LocalDate(1714, 4, 28));
        issue.addAddition(new LocalDate(1714, 5, 2));
        issue.addAddition(new LocalDate(1714, 5, 19));
        issue.addAddition(new LocalDate(1714, 5, 23));
        issue.addAddition(new LocalDate(1714, 6, 2));
        issue.addAddition(new LocalDate(1714, 6, 6));
        issue.addAddition(new LocalDate(1714, 6, 9));
        issue.addAddition(new LocalDate(1714, 6, 23));
        issue.addAddition(new LocalDate(1714, 6, 27));
        issue.addAddition(new LocalDate(1714, 6, 30));
        issue.addAddition(new LocalDate(1714, 7, 4));
        issue.addAddition(new LocalDate(1714, 7, 11));
        issue.addAddition(new LocalDate(1714, 7, 14));
        issue.addAddition(new LocalDate(1714, 7, 21));
        issue.addAddition(new LocalDate(1714, 8, 22));
        issue.addAddition(new LocalDate(1714, 8, 25));
        issue.addAddition(new LocalDate(1714, 9, 1));
        issue.addAddition(new LocalDate(1714, 9, 4));
        issue.addAddition(new LocalDate(1714, 9, 8));
        issue.addAddition(new LocalDate(1714, 10, 19));
        block.addIssue(issue);
        course.add(block);
    }

    private static void addFifthBlock(Course course) {
        Block block = new Block(course);
        block.setPublicationPeriod(new LocalDate(1715, 6, 1), new LocalDate(1715, 7, 13));
        Issue issue = new Issue(course);
        issue.addWednesday();
        issue.addSaturday();
        issue.addExclusion(new LocalDate(1715, 6, 8));
        issue.addExclusion(new LocalDate(1715, 6, 15));
        issue.addExclusion(new LocalDate(1715, 6, 26));
        issue.addExclusion(new LocalDate(1715, 6, 29));
        block.addIssue(issue);
        course.add(block);
    }

    private static void addSixthBlock(Course course) {
        Block block = new Block(course);
        block.setPublicationPeriod(new LocalDate(1716, 8, 8), new LocalDate(1716, 12, 26));
        Issue issue = new Issue(course);
        issue.addWednesday();
        issue.addSaturday();
        issue.addExclusion(new LocalDate(1715, 8, 22));
        block.addIssue(issue);
        course.add(block);
    }

    private static void addSeventhBlock(Course course) {
        Block block = new Block(course);
        block.setPublicationPeriod(new LocalDate(1719, 1, 1), new LocalDate(1720, 12, 31));
        Issue issue = new Issue(course);
        issue.addWednesday();
        issue.addSaturday();
        issue.addExclusion(new LocalDate(1719, 9, 9));
        block.addIssue(issue);
        course.add(block);
    }
}
