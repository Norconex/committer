/* Copyright 2010-2014 Norconex Inc.
 * 
 * This file is part of Norconex Committer.
 * 
 * Norconex Committer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Committer is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Committer. If not, see <http://www.gnu.org/licenses/>.
 */
package com.norconex.committer;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.norconex.committer.core.impl.FileSystemCommitter;
import com.norconex.commons.lang.config.ConfigurationUtil;


/**
 * Tests the {@link FileSystemCommitter}.
 * @author Pascal Essiembre
 */
@SuppressWarnings({"nls"})
public class FileSystemCommitterTest {

    private File tempFile;
    
    @Before
    public void setUp() throws IOException {
        tempFile = File.createTempFile("FileSystemCommitterTest", ".xml");
    }

    @After
    public void tearDown() throws Exception {
        tempFile.delete();
    }

    @Test
    public void testWriteRead() throws IOException {
        FileSystemCommitter outCommitter = new FileSystemCommitter();
        outCommitter.setDirectory("C:\\FakeTestDirectory\\");
        System.out.println("Writing/Reading this: " + outCommitter);
        ConfigurationUtil.assertWriteRead(outCommitter);
    }

}
