/*
 * Copyright 2014 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.services.refactoring.backend.server;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.junit.Test;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.uberfire.ext.metadata.backend.lucene.index.LuceneIndex;
import org.uberfire.ext.metadata.engine.Index;
import org.uberfire.ext.metadata.io.KObjectUtil;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.uberfire.ext.metadata.engine.MetaIndexEngine.*;

public class IndexFullTextTest extends BaseIndexingTest<TestPropertiesFileTypeDefinition> {

    @Test
    public void testIndexingFullText() throws IOException, InterruptedException {
        //Add test files
        loadProperties( "file1.properties",
                        basePath );
        loadProperties( "file2.properties",
                        basePath );

        Thread.sleep( 5000 ); //wait for events to be consumed from jgit -> (notify changes -> watcher -> index) -> lucene index

        final Index index = getConfig().getIndexManager().get( KObjectUtil.toKCluster( basePath.getFileSystem() ) );

        {
            final IndexSearcher searcher = ( (LuceneIndex) index ).nrtSearcher();
            final TopScoreDocCollector collector = TopScoreDocCollector.create( 10,
                                                                                true );
            searcher.search( new WildcardQuery( new Term( FULL_TEXT_FIELD,
                                                          "*file*" ) ),
                             collector );
            final ScoreDoc[] hits = collector.topDocs().scoreDocs;

            assertEquals( 2,
                          hits.length );
            ( (LuceneIndex) index ).nrtRelease( searcher );
        }

    }

    @Override
    protected TestIndexer getIndexer() {
        return new TestPropertiesFileIndexer();
    }

    @Override
    public Map<String, Analyzer> getAnalyzers() {
        return Collections.EMPTY_MAP;
    }

    @Override
    protected TestPropertiesFileTypeDefinition getResourceTypeDefinition() {
        return new TestPropertiesFileTypeDefinition();
    }

    @Override
    protected String getRepositoryName() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected KieProjectService getProjectService() {
        return mock( KieProjectService.class );
    }

}
