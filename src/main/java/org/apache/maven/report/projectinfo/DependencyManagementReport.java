package org.apache.maven.report.projectinfo;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.List;
import java.util.Locale;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.report.projectinfo.dependencies.ManagementDependencies;
import org.apache.maven.report.projectinfo.dependencies.RepositoryUtils;
import org.apache.maven.report.projectinfo.dependencies.renderer.DependencyManagementRenderer;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;


/**
 * Generates the Project Dependency Management report.
 *
 * @author Nick Stolwijk
 * @version $Id$
 * @since 2.1
 * @goal dependency-management
 * @requiresDependencyResolution test
 */
public class DependencyManagementReport
    extends AbstractProjectInfoReport
{
    // ----------------------------------------------------------------------
    // Mojo components
    // ----------------------------------------------------------------------

    /**
     * Maven Project Builder component.
     *
     * @component
     */
    private MavenProjectBuilder mavenProjectBuilder;

    /**
     * Maven Artifact Factory component.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * Wagon manager component.
     *
     * @since 2.3
     * @component
     */
    private WagonManager wagonManager;

    /**
     * Repository metadata component.
     *
     * @since 2.3
     * @component
     */
    private RepositoryMetadataManager repositoryMetadataManager;

    // ----------------------------------------------------------------------
    // Mojo parameters
    // ----------------------------------------------------------------------

    /**
     * Remote repositories used for the project.
     *
     * @since 2.1
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    private List<ArtifactRepository> remoteRepositories;

    /**
     * The current user system settings for use in Maven.
     *
     * @since 2.3
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /**
     * Lazy instantiation for management dependencies.
     */
    private ManagementDependencies managementDependencies;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void executeReport( Locale locale )
    {
        RepositoryUtils repoUtils =
            new RepositoryUtils( getLog(), wagonManager, settings,
                                 mavenProjectBuilder, factory, resolver, project.getRemoteArtifactRepositories(),
                                 project.getPluginArtifactRepositories(), localRepository,
                                 repositoryMetadataManager );

        DependencyManagementRenderer r =
            new DependencyManagementRenderer( getSink(), locale, i18n, getLog(), getManagementDependencies(),
                                              artifactFactory, mavenProjectBuilder, remoteRepositories,
                                              localRepository, repoUtils );
        r.render();
    }

    /** {@inheritDoc} */
    public String getOutputName()
    {
        return "dependency-management";
    }

    protected String getI18Nsection()
    {
        return "dependencyManagement";
    }

    /** {@inheritDoc} */
    public boolean canGenerateReport()
    {
        return getManagementDependencies().hasDependencies();
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    private ManagementDependencies getManagementDependencies()
    {
        if ( managementDependencies != null )
        {
            return managementDependencies;
        }

        if ( project.getDependencyManagement() == null )
        {
            managementDependencies = new ManagementDependencies( null );
        }
        else
        {
            managementDependencies = new ManagementDependencies( project.getDependencyManagement().getDependencies() );
        }

        return managementDependencies;
    }
}