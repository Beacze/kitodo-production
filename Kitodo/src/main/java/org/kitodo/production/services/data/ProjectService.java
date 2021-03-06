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

package org.kitodo.production.services.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProjectDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.ProjectType;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ClientDTO;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.dto.TemplateDTO;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.ClientSearchService;
import org.primefaces.model.SortOrder;

public class ProjectService extends ClientSearchService<Project, ProjectDTO, ProjectDAO> {

    private static volatile ProjectService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private ProjectService() {
        super(new ProjectDAO(), new ProjectType(), new Indexer<>(Project.class), new Searcher(Project.class),
                ProjectTypeField.CLIENT_ID.getKey());
    }

    /**
     * Return singleton variable of type ProjectService.
     *
     * @return unique instance of ProcessService
     */
    public static ProjectService getInstance() {
        ProjectService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ProjectService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new ProjectService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Method saves processes and templates related to modified project.
     *
     * @param project
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Project project)
            throws CustomResponseException, DataException, IOException {
        manageProcessesDependenciesForIndex(project);
        manageTemplatesDependenciesForIndex(project);
    }

    /**
     * Management of processes for project object.
     *
     * @param project
     *            object
     */
    private void manageProcessesDependenciesForIndex(Project project)
            throws CustomResponseException, DataException, IOException {
        if (project.getIndexAction() == IndexAction.DELETE) {
            for (Process process : project.getProcesses()) {
                ServiceManager.getProcessService().removeFromIndex(process, false);
            }
        } else {
            for (Process process : project.getProcesses()) {
                ServiceManager.getProcessService().saveToIndex(process, false);
            }
        }
    }

    /**
     * Management of templates for project object.
     *
     * @param project
     *            object
     */
    private void manageTemplatesDependenciesForIndex(Project project)
            throws CustomResponseException, DataException, IOException {
        if (project.getIndexAction() == IndexAction.DELETE) {
            for (Template template : project.getTemplates()) {
                template.getProjects().remove(project);
            }
        }

        for (Template template : project.getTemplates()) {
            ServiceManager.getTemplateService().saveToIndex(template, false);
        }
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Project");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Project WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countDocuments(getProjectsForCurrentUserQuery());
    }

    @Override
    public List<Project> getAllNotIndexed() {
        return getByQuery("FROM Project WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Project> getAllForSelectedClient() {
        return dao.getByQuery("SELECT p FROM Project AS p INNER JOIN p.client AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public List<ProjectDTO> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters)
            throws DataException {
        return findByQuery(getProjectsForCurrentUserQuery(), getSortBuilder(sortField, sortOrder), first, pageSize,
            false);
    }

    /**
     * Find all projects available to assign to the edited user. It will be
     * displayed in the addProjectsPopup.
     *
     * @param user
     *            user which is going to be edited
     * @return list of all matching projects
     */
    public List<ProjectDTO> findAllAvailableForAssignToUser(User user) throws DataException {
        return findAvailableForAssignToUser(user);
    }

    private List<ProjectDTO> findAvailableForAssignToUser(User user) throws DataException {

        BoolQueryBuilder query = new BoolQueryBuilder();
        for (Client client : user.getClients()) {
            query.should(createSimpleQuery(ProjectTypeField.CLIENT_ID.getKey(), client.getId(), true));
        }

        List<ProjectDTO> projectDTOS = findByQuery(query, true);
        List<ProjectDTO> alreadyAssigned = new ArrayList<>();
        for (Project project : user.getProjects()) {
            alreadyAssigned.addAll(projectDTOS.stream().filter(projectDTO -> projectDTO.getId().equals(project.getId()))
                    .collect(Collectors.toList()));
        }
        projectDTOS.removeAll(alreadyAssigned);
        return projectDTOS;
    }

    @Override
    public ProjectDTO convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(getIdFromJSONObject(jsonObject));
        projectDTO.setTitle(ProjectTypeField.TITLE.getStringValue(jsonObject));
        projectDTO.setStartDate(ProjectTypeField.START_DATE.getStringValue(jsonObject));
        projectDTO.setEndDate(ProjectTypeField.END_DATE.getStringValue(jsonObject));
        projectDTO.setFileFormatDmsExport(ProjectTypeField.FILE_FORMAT_DMS_EXPORT.getStringValue(jsonObject));
        projectDTO.setFileFormatInternal(ProjectTypeField.FILE_FORMAT_INTERNAL.getStringValue(jsonObject));
        projectDTO.setMetsRightsOwner(ProjectTypeField.METS_RIGTS_OWNER.getStringValue(jsonObject));
        projectDTO.setNumberOfPages(ProjectTypeField.NUMBER_OF_PAGES.getIntValue(jsonObject));
        projectDTO.setNumberOfVolumes(ProjectTypeField.NUMBER_OF_VOLUMES.getIntValue(jsonObject));
        projectDTO.setActive(ProjectTypeField.ACTIVE.getBooleanValue(jsonObject));
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(ProjectTypeField.CLIENT_ID.getIntValue(jsonObject));
        clientDTO.setName(ProjectTypeField.CLIENT_NAME.getStringValue(jsonObject));
        projectDTO.setClient(clientDTO);
        if (!related) {
            convertRelatedJSONObjects(jsonObject, projectDTO);
        } else {
            projectDTO.setTemplates(getTemplatesForProjectDTO(jsonObject));
        }
        return projectDTO;
    }

    private List<TemplateDTO> getTemplatesForProjectDTO(Map<String, Object> jsonObject) throws DataException {
        List<TemplateDTO> templateDTOS = new ArrayList<>();
        List<Map<String, Object>> jsonArray = ProjectTypeField.TEMPLATES.getJsonArray(jsonObject);

        for (Map<String, Object> singleObject : jsonArray) {
            TemplateDTO templateDTO = new TemplateDTO();
            templateDTO.setId(TemplateTypeField.ID.getIntValue(singleObject));
            templateDTO.setTitle(TemplateTypeField.TITLE.getStringValue(singleObject));
            templateDTOS.add(templateDTO);
        }
        return templateDTOS;
    }

    private void convertRelatedJSONObjects(Map<String, Object> jsonObject, ProjectDTO projectDTO) throws DataException {
        // TODO: not clear if project lists will need it
        projectDTO.setUsers(new ArrayList<>());
        projectDTO.setTemplates(convertRelatedJSONObjectToDTO(jsonObject, ProjectTypeField.TEMPLATES.getKey(),
            ServiceManager.getTemplateService()));
    }

    /**
     * Checks if a project can be actually used.
     *
     * @param project
     *            The project to check
     * @return true, if project is complete and can be used, false, if project is
     *         incomplete
     */
    public boolean isProjectComplete(Project project) {
        boolean projectsXmlExists = KitodoConfigFile.PROJECT_CONFIGURATION.exists();
        boolean digitalCollectionsXmlExists = KitodoConfigFile.DIGITAL_COLLECTIONS.exists();

        return Objects.nonNull(project.getTitle()) && Objects.nonNull(project.template)
                && project.getFileFormatDmsExport() != null && Objects.nonNull(project.getFileFormatInternal())
                && digitalCollectionsXmlExists && projectsXmlExists;
    }

    /**
     * Duplicate the project with the given ID 'itemId'.
     *
     * @return the duplicated Project
     */
    public Project duplicateProject(Project baseProject) {
        Project duplicatedProject = new Project();

        // Project _title_ should explicitly _not_ be duplicated!
        duplicatedProject.setClient(baseProject.getClient());
        duplicatedProject.setStartDate(baseProject.getStartDate());
        duplicatedProject.setEndDate(baseProject.getEndDate());
        duplicatedProject.setNumberOfPages(baseProject.getNumberOfPages());
        duplicatedProject.setNumberOfVolumes(baseProject.getNumberOfVolumes());

        duplicatedProject.setFileFormatInternal(baseProject.getFileFormatInternal());
        duplicatedProject.setFileFormatDmsExport(baseProject.getFileFormatDmsExport());

        duplicatedProject.setDmsImportRootPath(baseProject.getDmsImportRootPath());
        duplicatedProject.setDmsImportTimeOut(baseProject.getDmsImportTimeOut());
        duplicatedProject.setUseDmsImport(baseProject.isUseDmsImport());
        duplicatedProject.setDmsImportCreateProcessFolder(baseProject.isDmsImportCreateProcessFolder());

        duplicatedProject.setMetsRightsOwner(baseProject.getMetsRightsOwner());
        duplicatedProject.setMetsRightsOwnerLogo(baseProject.getMetsRightsOwnerLogo());
        duplicatedProject.setMetsRightsOwnerSite(baseProject.getMetsRightsOwnerSite());
        duplicatedProject.setMetsRightsOwnerMail(baseProject.getMetsRightsOwnerMail());

        duplicatedProject.setMetsDigiprovPresentation(baseProject.getMetsDigiprovPresentation());
        duplicatedProject.setMetsDigiprovReference(baseProject.getMetsDigiprovReference());

        duplicatedProject.setMetsPointerPath(baseProject.getMetsPointerPath());
        duplicatedProject.setMetsPurl(baseProject.getMetsPurl());
        duplicatedProject.setMetsContentIDs(baseProject.getMetsContentIDs());

        List<Folder> duplicatedFolders = new ArrayList<>();
        for (Folder folder : baseProject.getFolders()) {
            Folder duplicatedFolder = new Folder();
            duplicatedFolder.setMimeType(folder.getMimeType());
            duplicatedFolder.setFileGroup(folder.getFileGroup());
            duplicatedFolder.setUrlStructure(folder.getUrlStructure());
            duplicatedFolder.setPath(folder.getPath());

            duplicatedFolder.setProject(duplicatedProject);
            duplicatedFolder.setCopyFolder(folder.isCopyFolder());
            duplicatedFolder.setCreateFolder(folder.isCreateFolder());
            duplicatedFolder.setDerivative(folder.getDerivative().orElse(null));
            duplicatedFolder.setDpi(folder.getDpi().orElse(null));
            duplicatedFolder.setImageScale(folder.getImageScale().orElse(null));
            duplicatedFolder.setImageSize(folder.getImageSize().orElse(null));
            duplicatedFolder.setLinkingMode(folder.getLinkingMode());
            duplicatedFolders.add(duplicatedFolder);
        }
        duplicatedProject.setFolders(duplicatedFolders);

        return duplicatedProject;
    }

    /**
     * Get query for finding projects for current user.
     * 
     * @return query for finding projects for current user
     */
    public QueryBuilder getProjectsForCurrentUserQuery() {
        List<Project> projects = ServiceManager.getUserService().getAuthenticatedUser().getProjects();
        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
        for (Project project : projects) {
            idsQueryBuilder.addIds(project.getId().toString());
        }
        return idsQueryBuilder;
    }

    /**
     * Find all Projects for Current User.
     * @return A list of all Projects assigned tot he current user
     * @throws DataException when elasticsearch query is failing
     */
    public List<ProjectDTO> findAllProjectsForCurrentUser() throws DataException {
        return findByQuery(getProjectsForCurrentUserQuery(), false);
    }

    /**
     * Get all projects templates for given title and client id.
     *
     * @param title
     *            of Project
     * @param clientId
     *            id of client
     * @return list of all projects templates as Project objects
     */
    public List<Project> getProjectsWithTitleAndClient(String title, Integer clientId) {
        String query = "SELECT p FROM Project AS p INNER JOIN p.client AS c WITH c.id = :clientId WHERE p.title = :title";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientId", clientId);
        parameters.put("title", title);
        return getByQuery(query, parameters);
    }
}
