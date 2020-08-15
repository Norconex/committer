/* Copyright 2019-2020 Norconex Inc.
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
package com.norconex.committer.core3.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.HashCodeExclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.committer.core3.AbstractCommitter;
import com.norconex.committer.core3.CommitterException;
import com.norconex.committer.core3.DeleteRequest;
import com.norconex.committer.core3.UpsertRequest;
import com.norconex.commons.lang.SLF4JUtil;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.text.TextMatcher;
import com.norconex.commons.lang.xml.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;

/**
 * <p>
 * <b>WARNING: Not intended for production use.</b>
 * </p>
 * <p>
 * A Committer that logs all data associated with every document, added or
 * removed, to the application logs, or the console (STDOUT/STDERR). Default
 * uses application logger with INFO log level.
 * </p>
 * <p>
 * This Committer can be useful for troubleshooting.  Given how much
 * information this could represent, it is recommended
 * you do not use in a production environment. At a minimum, if you are
 * logging to file, make sure to rotate/clean the logs regularly.
 * </p>
 *
 * {@nx.xml.usage
 * <committer class="com.norconex.committer.core3.impl.LogCommitter">
 *   <logLevel>[TRACE|DEBUG|INFO|WARN|ERROR|STDOUT|STDERR]</logLevel>
 *   <fieldMatcher {@nx.include com.norconex.commons.lang.text.TextMatcher#matchAttributes}>
 *     (Expression matching fields to log. Default logs all.)
 *   </fieldMatcher>
 *   <ignoreContent>[false|true]</ignoreContent>
 *   {@nx.include com.norconex.committer.core3.AbstractCommitter@nx.xml.usage}
 * </committer>
 * }
 *
 * @author Pascal Essiembre
 * @since 3.0.0
 */
@SuppressWarnings("javadoc")
public class LogCommitter extends AbstractCommitter
        implements IXMLConfigurable  {

    private static final Logger LOG =
            LoggerFactory.getLogger(LogCommitter.class);

    private static final int LOG_TIME_BATCH_SIZE = 100;

    private long addCount = 0;
    private long removeCount = 0;

    @ToStringExclude
    @HashCodeExclude
    @EqualsExclude
    private final StopWatch watch = new StopWatch();

    private boolean ignoreContent;
    private final TextMatcher fieldMatcher = new TextMatcher();
    private String logLevel;

    public boolean isIgnoreContent() {
        return ignoreContent;
    }
    public void setIgnoreContent(boolean ignoreContent) {
        this.ignoreContent = ignoreContent;
    }

    public TextMatcher getFieldMatcher() {
        return fieldMatcher;
    }
    public void setFieldMatcher(TextMatcher fieldMatcher) {
        this.fieldMatcher.copyFrom(fieldMatcher);
    }

    public String getLogLevel() {
        return logLevel;
    }
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    protected void doInit() throws CommitterException {
        watch.reset();
        watch.start();
    }
    @Override
    protected void doUpsert(UpsertRequest upsertRequest)
            throws CommitterException {
        StringBuilder b = new StringBuilder();
        b.append("\n=== DOCUMENT UPSERTED ================================\n");

        stringifyRefAndMeta(
                b, upsertRequest.getReference(), upsertRequest.getMetadata());

        if (!ignoreContent) {
            b.append("\n--- Content ---------------------------------------\n");
            try {
                b.append(IOUtils.toString(
                        upsertRequest.getContent(), UTF_8)).append('\n');
            } catch (IOException e) {
                b.append(ExceptionUtils.getStackTrace(e));
            }
        }
        log(b.toString());

        addCount++;
        if (addCount % LOG_TIME_BATCH_SIZE == 0) {
            LOG.info("{} upsert logged in: {}", addCount, watch);
        }
    }
    @Override
    protected void doDelete(DeleteRequest deleteRequest)
            throws CommitterException {
        StringBuilder b = new StringBuilder();
        b.append("\n=== DOCUMENT DELETED =================================\n");
        stringifyRefAndMeta(
                b, deleteRequest.getReference(), deleteRequest.getMetadata());
        log(b.toString());

        removeCount++;
        if (removeCount % LOG_TIME_BATCH_SIZE == 0) {
            LOG.info("{} delete logged in {}", removeCount, watch);
        }
    }
    @Override
    protected void doClose() throws CommitterException {
        watch.stop();
        LOG.info("{} additions committed.", addCount);
        LOG.info("{} deletions committed.", removeCount);
        LOG.info("Total elapsed time: {}", watch);
    }

    @Override
    protected void doClean() throws CommitterException {
        // NOOP
    }

    private void stringifyRefAndMeta(
            StringBuilder b, String reference, Properties metadata) {
        b.append("REFERENCE = ").append(reference).append('\n');
        if (metadata != null) {
            b.append("\n--- Metadata: -------------------------------------\n");
            for (Entry<String, List<String>> en : metadata.entrySet()) {
                if (fieldMatcher.getPattern() == null
                        || fieldMatcher.matches(en.getKey())) {
                    for (String val : en.getValue()) {
                        b.append(en.getKey()).append(" = ")
                                .append(val).append('\n');
                    }
                }

            }
        }
    }

    private void log(String txt) {
        String lvl = Optional.ofNullable(logLevel).orElse("INFO").toUpperCase();
        if ("STDERR".equals(lvl)) {
            System.err.println(txt);
        } else if ("STDOUT".equals(lvl)) {
            System.out.println(txt);
        } else {
            SLF4JUtil.log(LOG, lvl, txt);
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
    @Override
    public void loadCommitterFromXML(XML xml) {
        setLogLevel(xml.getString("logLevel", logLevel));
        setIgnoreContent(xml.getBoolean("ignoreContent", ignoreContent));
        fieldMatcher.loadFromXML(xml.getXML("fieldMatcher"));
    }
    @Override
    public void saveCommitterToXML(XML xml) {
        xml.addElement("logLevel", logLevel);
        xml.addElement("ignoreContent", ignoreContent);
        fieldMatcher.saveToXML(xml.addElement("fieldMatcher"));
    }
}
