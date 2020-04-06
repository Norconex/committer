/* Copyright 2010-2020 Norconex Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.norconex.committer.core.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.committer.core.ICommitter;
import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.io.CachedStreamFactory;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.xml.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;

/**
 * <p>
 * This committer allows you to define and use many committers as one.
 * Every committing requests will be dispatched and handled by all nested
 * committers defined (in the order they were added).
 * </p>
 * <h3>XML configuration usage:</h3>
 * <pre>
 *  &lt;committer class="com.norconex.committer.core.impl.MultiCommitter"&gt;
 *      &lt;committer class="(committer class)"&gt;
 *          (Commmitter-specific configuration here)
 *      &lt;/committer&gt;
 *      &lt;committer class="(committer class)"&gt;
 *          (Commmitter-specific configuration here)
 *      &lt;/committer&gt;
 *      ...
 *  &lt;/committer&gt;
 * </pre>
 *
 * <h4>Usage example:</h4>
 * <p>
 * The following will commit files in two different locations on the filesystem.
 * </p>
 * <pre>
 *  &lt;committer class="com.norconex.committer.core.impl.MultiCommitter"&gt;
 *      &lt;committer class="com.norconex.committer.core.impl.FileSystemCommitter"&gt;
 *          &lt;directory&gt;/export/path1/&lt;/directory&gt;
 *      &lt;/committer&gt;
 *      &lt;committer class="com.norconex.committer.core.impl.FileSystemCommitter"&gt;
 *          &lt;directory&gt;/export/path2/&lt;/directory&gt;
 *      &lt;/committer&gt;
 *  &lt;/committer&gt;
 * </pre>
 * @author Pascal Essiembre
 * @since 1.2.0
 * @deprecated Since 3.0.0.
 */
@Deprecated
public class MultiCommitter implements ICommitter, IXMLConfigurable {

    private final List<ICommitter> committers = new ArrayList<>();

    /**
     * Constructor.
     */
    public MultiCommitter() {
        super();
    }
    /**
     * Constructor.
     * @param committers a list of committers
     */
    public MultiCommitter(List<ICommitter> committers) {
        this.committers.addAll(committers);
    }

    /**
     * Adds one or more committers.
     * @param committer committers
     */
    public void addCommitter(ICommitter... committer) {
        this.committers.addAll(Arrays.asList(committer));
    }
    /**
     * Removes one or more committers.
     * @param committer committers
     */
    public void removeCommitter(ICommitter... committer) {
        this.committers.removeAll(Arrays.asList(committer));
    }
    /**
     * Gets nested committers.
     * @return committers
     */
    public List<ICommitter> getCommitters() {
        return new ArrayList<>(committers);
    }

    @Override
    public void add(
            String reference, InputStream content, Properties metadata) {

        CachedInputStream cachedInputStream;
        if (content instanceof CachedInputStream) {
            cachedInputStream = (CachedInputStream) content;
        } else {
            CachedStreamFactory factory = new CachedStreamFactory(
                    (int) FileUtils.ONE_MB, (int) FileUtils.ONE_MB);
            cachedInputStream = factory.newInputStream(content);
        }

        for (int i = 0; i < committers.size(); i++) {
            ICommitter committer = committers.get(i);
            committer.add(reference, cachedInputStream, metadata);
            cachedInputStream.rewind();
        }
    }

    @Override
    public void remove(String reference, Properties metadata) {
        for (int i = 0; i < committers.size(); i++) {
            ICommitter committer = committers.get(i);
            committer.remove(reference, metadata);
        }
    }

    @Override
    public void commit() {
        for (ICommitter committer : committers) {
            committer.commit();
        }
    }

    @Override
    public void loadFromXML(XML xml) {
        for (XML xmlCommitter : xml.getXMLList("committer")) {
            addCommitter((ICommitter) xmlCommitter.toObject());
        }
    }

    @Override
    public void saveToXML(XML xml) {
        for (ICommitter committer : committers) {
            xml.addElement("committer", committer);
        }
    }

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(
                this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
