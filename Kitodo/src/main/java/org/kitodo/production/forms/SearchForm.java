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

package org.kitodo.production.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.enums.FilterString;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("SearchForm")
@RequestScoped
public class SearchForm {

    /**
     * Logger instance.
     */
    private static final Logger logger = LogManager.getLogger(SearchForm.class);

    private List<String> projects = new ArrayList<>(); // proj:
    private String project = "";

    private List<String> processPropertyTitles = new ArrayList<>(); // processeig:
    private String processPropertyTitle = "";
    private String processPropertyValue = "";

    private List<String> masterpiecePropertyTitles = new ArrayList<>(); // werk:
    private String masterpiecePropertyTitle = "";
    private String masterpiecePropertyValue = "";

    private List<String> templatePropertyTitles = new ArrayList<>();// vorl:
    private String templatePropertyTitle = "";
    private String templatePropertyValue = "";

    private List<String> stepTitles = new ArrayList<>(); // step:
    private List<TaskStatus> stepstatus = new ArrayList<>();
    private String status = "";
    private String stepname = "";

    private List<User> user = new ArrayList<>();
    private String stepdonetitle = "";
    private String stepdoneuser = "";

    private String idin = "";
    private String processTitle = ""; // proc:

    private String projectOperand = "";
    private String processOperand = "";
    private String processPropertyOperand = "";
    private String masterpiecePropertyOperand = "";
    private String templatePropertyOperand = "";
    private String stepOperand = "";

    private ProcessForm processForm;
    private CurrentTaskForm taskForm;

    /**
     * Constructor with inject process form.
     *
     * @param processForm
     *            managed bean
     */
    @Inject
    public SearchForm(ProcessForm processForm, CurrentTaskForm taskForm) {
        initStepStatus();
        initProjects();
        initMasterpiecePropertyTitles();
        initTemplatePropertyTitles();
        initProcessPropertyTitles();
        initStepTitles();
        initUserList();
        this.processForm = processForm;
        this.taskForm = taskForm;
    }

    /**
     * Initialise drop down list of master piece property titles.
     */
    private void initMasterpiecePropertyTitles() {
        List<String> workpiecePropertiesTitlesDistinct = new ArrayList<>();
        try {
            workpiecePropertiesTitlesDistinct = ServiceManager.getPropertyService()
                    .findWorkpiecePropertiesTitlesDistinct();
        } catch (DataException | DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        this.masterpiecePropertyTitles = workpiecePropertiesTitlesDistinct;
    }

    /**
     * Initialise drop down list of projects.
     */
    private void initProjects() {
        List<ProjectDTO> projectsSortedByTitle = Collections.emptyList();
        try {
            projectsSortedByTitle = ServiceManager.getProjectService().findAllProjectsForCurrentUser();
        } catch (DataException e) {
            Helper.setErrorMessage("errorInitializingProjects", logger, e);
        }

        for (ProjectDTO projectSortedByTitle : projectsSortedByTitle) {
            this.projects.add(projectSortedByTitle.getTitle());
        }
    }

    /**
     * Initialise drop down list of process property titles.
     */
    private void initProcessPropertyTitles() {
        List<String> processPropertiesTitlesDistinct = new ArrayList<>();
        try {
            processPropertiesTitlesDistinct = ServiceManager.getPropertyService().findProcessPropertiesTitlesDistinct();
        } catch (DataException | DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        this.processPropertyTitles = processPropertiesTitlesDistinct;
    }

    /**
     * Initialise drop down list of step status.
     */
    private void initStepStatus() {
        this.stepstatus.addAll(Arrays.asList(TaskStatus.values()));
    }

    /**
     * Initialise drop down list of task titles.
     */
    private void initStepTitles() {
        List<String> taskTitles = new ArrayList<>();
        try {
            taskTitles = ServiceManager.getTaskService().findTaskTitlesDistinct();
        } catch (DataException | DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        this.stepTitles = taskTitles;
    }

    /**
     * Initialise drop down list of template property titles.
     */
    private void initTemplatePropertyTitles() {
        List<String> templatePropertiesTitlesDistinct = new ArrayList<>();
        try {
            templatePropertiesTitlesDistinct = ServiceManager.getPropertyService()
                    .findTemplatePropertiesTitlesDistinct();
        } catch (DataException | DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        this.templatePropertyTitles = templatePropertiesTitlesDistinct;
    }

    /**
     * Initialise drop down list of user list.
     */
    private void initUserList() {
        try {
            this.user.addAll(ServiceManager.getUserService().getAllActiveUsersSortedByNameAndSurname());
        } catch (RuntimeException e) {
            logger.warn("RuntimeException caught. List of users could be empty!");
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("activeUsers") }, logger, e);
        }
    }

    public List<String> getProjects() {
        return this.projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    public List<String> getMasterpiecePropertyTitles() {
        return this.masterpiecePropertyTitles;
    }

    public void setMasterpiecePropertyTitles(List<String> masterpiecePropertyTitles) {
        this.masterpiecePropertyTitles = masterpiecePropertyTitles;
    }

    public List<String> getTemplatePropertyTitles() {
        return this.templatePropertyTitles;
    }

    public void setTemplatePropertyTitles(List<String> templatePropertyTitles) {
        this.templatePropertyTitles = templatePropertyTitles;
    }

    public List<String> getProcessPropertyTitles() {
        return this.processPropertyTitles;
    }

    public void setProcessPropertyTitles(List<String> processPropertyTitles) {
        this.processPropertyTitles = processPropertyTitles;
    }

    public List<String> getStepTitles() {
        return this.stepTitles;
    }

    public void setStepTitles(List<String> stepTitles) {
        this.stepTitles = stepTitles;
    }

    public List<TaskStatus> getStepstatus() {
        return this.stepstatus;
    }

    public void setStepstatus(List<TaskStatus> stepstatus) {
        this.stepstatus = stepstatus;
    }

    public String getStepdonetitle() {
        return this.stepdonetitle;
    }

    public void setStepdonetitle(String stepdonetitle) {
        this.stepdonetitle = stepdonetitle;
    }

    public String getStepdoneuser() {
        return this.stepdoneuser;
    }

    public void setStepdoneuser(String stepdoneuser) {
        this.stepdoneuser = stepdoneuser;
    }

    public String getIdin() {
        return this.idin;
    }

    public void setIdin(String idin) {
        this.idin = idin;
    }

    public String getProject() {
        return this.project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProcessTitle() {
        return this.processTitle;
    }

    public void setProcessTitle(String processTitle) {
        this.processTitle = processTitle;
    }

    public String getProcessPropertyTitle() {
        return this.processPropertyTitle;
    }

    public void setProcessPropertyTitle(String processPropertyTitle) {
        this.processPropertyTitle = processPropertyTitle;
    }

    public String getProcessPropertyValue() {
        return this.processPropertyValue;
    }

    public void setProcessPropertyValue(String processPropertyValue) {
        this.processPropertyValue = processPropertyValue;
    }

    public String getMasterpiecePropertyTitle() {
        return this.masterpiecePropertyTitle;
    }

    public void setMasterpiecePropertyTitle(String masterpiecePropertyTitle) {
        this.masterpiecePropertyTitle = masterpiecePropertyTitle;
    }

    public String getMasterpiecePropertyValue() {
        return this.masterpiecePropertyValue;
    }

    public void setMasterpiecePropertyValue(String masterpiecePropertyValue) {
        this.masterpiecePropertyValue = masterpiecePropertyValue;
    }

    public String getTemplatePropertyTitle() {
        return this.templatePropertyTitle;
    }

    public void setTemplatePropertyTitle(String templatePropertyTitle) {
        this.templatePropertyTitle = templatePropertyTitle;
    }

    public String getTemplatePropertyValue() {
        return this.templatePropertyValue;
    }

    public void setTemplatePropertyValue(String templatePropertyValue) {
        this.templatePropertyValue = templatePropertyValue;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStepname() {
        return this.stepname;
    }

    public void setStepname(String stepname) {
        this.stepname = stepname;
    }

    public List<User> getUser() {
        return this.user;
    }

    public void setUser(List<User> user) {
        this.user = user;
    }

    /**
     * Filter processes.
     *
     * @return filter as java.lang.String
     */
    public String filterProcesses() {
        processForm.changeFilter(createFilter());
        return processForm.processListPath;
    }

    /**
     * Filter tasks.
     *
     * @return filter as java.lang.String
     */
    public String filterTasks() {
        taskForm.changeFilter(createFilter());
        return taskForm.getTaskListPath();
    }

    private String createFilter() {
        String search = "";
        if (!this.processTitle.isEmpty()) {
            search += "\"" + this.processOperand + this.processTitle + "\" ";
        }
        if (!this.idin.isEmpty()) {
            search += "\"" + FilterString.ID.getFilterEnglish() + this.idin + "\" ";
        }
        if (!this.project.isEmpty()) {
            search += "\"" + this.projectOperand + FilterString.PROJECT.getFilterEnglish() + this.project + "\" ";
        }
        search += createSearchProperty(this.processPropertyTitle, this.processPropertyValue,
                this.processPropertyOperand, FilterString.PROCESSPROPERTY);
        search += createSearchProperty(this.masterpiecePropertyTitle, this.masterpiecePropertyValue,
                this.masterpiecePropertyOperand, FilterString.WORKPIECE);
        search += createSearchProperty(this.templatePropertyTitle, this.templatePropertyValue,
                this.templatePropertyOperand, FilterString.TEMPLATE);
        if (!this.stepname.isEmpty()) {
            search += "\"" + this.stepOperand + this.status + ":" + this.stepname + "\" ";
        }
        if (!this.stepdonetitle.isEmpty() && !this.stepdoneuser.isEmpty()
                && ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.WITH_USER_STEP_DONE_SEARCH)) {
            search += "\"" + FilterString.TASKDONEUSER.getFilterEnglish() + this.stepdoneuser + "\" \""
                    + FilterString.TASKDONETITLE.getFilterEnglish() + this.stepdonetitle + "\" ";
        }
        return search;
    }

    private String createSearchProperty(String title, String value, String operand, FilterString filterString) {
        if (Objects.nonNull(value) && !value.isEmpty()) {
            if (Objects.nonNull(title) && !title.isEmpty()) {
                return "\"" + operand + filterString.getFilterEnglish() + title + ":" + value + "\" ";
            } else {
                return "\"" + operand + filterString.getFilterEnglish() + value + "\" ";
            }
        }
        return "";
    }

    /**
     * Get operands.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getOperands() {
        List<SelectItem> answer = new ArrayList<>();
        SelectItem and = new SelectItem("", Helper.getTranslation("AND"));
        SelectItem not = new SelectItem("-", Helper.getTranslation("NOT"));
        answer.add(and);
        answer.add(not);
        return answer;
    }

    public String getProjectOperand() {
        return this.projectOperand;
    }

    public void setProjectOperand(String projectOperand) {
        this.projectOperand = projectOperand;
    }

    public String getProcessPropertyOperand() {
        return this.processPropertyOperand;
    }

    public void setProcessPropertyOperand(String processPropertyOperand) {
        this.processPropertyOperand = processPropertyOperand;
    }

    public String getMasterpiecePropertyOperand() {
        return this.masterpiecePropertyOperand;
    }

    public void setMasterpiecePropertyOperand(String masterpiecePropertyOperand) {
        this.masterpiecePropertyOperand = masterpiecePropertyOperand;
    }

    public String getTemplatePropertyOperand() {
        return this.templatePropertyOperand;
    }

    public void setTemplatePropertyOperand(String templatePropertyOperand) {
        this.templatePropertyOperand = templatePropertyOperand;
    }

    public String getStepOperand() {
        return this.stepOperand;
    }

    public void setStepOperand(String stepOperand) {
        this.stepOperand = stepOperand;
    }

    public String getProcessOperand() {
        return this.processOperand;
    }

    public void setProcessOperand(String processOperand) {
        this.processOperand = processOperand;
    }

}
