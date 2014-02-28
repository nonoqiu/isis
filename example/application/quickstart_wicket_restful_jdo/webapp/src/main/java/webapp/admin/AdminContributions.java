/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package webapp.admin;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Bulk.AppliesTo;
import org.apache.isis.applib.annotation.NotContributed.As;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.objectstore.jdo.applib.service.DomainChangeJdoAbstract;
import org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo;
import org.apache.isis.objectstore.jdo.applib.service.audit.AuditingServiceJdoRepository;
import org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo;
import org.apache.isis.objectstore.jdo.applib.service.command.CommandServiceJdoRepository;

import services.ClockService;

public class AdminContributions extends AbstractService {

    /**
     * Depending on which services are available, returns either a list of {@link CommandJdo command}s that have 
     * caused a change in the domain object or a list of {@link AuditEntryJdo audit entries} capturing the 'effect' 
     * of that change.
     * 
     * <p>
     * If {@link CommandJdo command}s are returned, then the corresponding {@link AuditEntryJdo audit entries} are
     * available from each command.
     */
    @NotInServiceMenu
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="30")
    public List<? extends DomainChangeJdoAbstract> recentChanges (
            final Object targetDomainObject,
            final @Optional @Named("From") LocalDate from,
            final @Optional @Named("To") LocalDate to) {
        final Bookmark targetBookmark = bookmarkService.bookmarkFor(targetDomainObject);
        final List<DomainChangeJdoAbstract> changes = Lists.newArrayList();
        if(commandServiceRepository != null) {
            changes.addAll(commandServiceRepository.findByTargetAndFromAndTo(targetBookmark, from, to));
        } 
        changes.addAll(auditingServiceRepository.findByTargetAndFromAndTo(targetBookmark, from, to));
        Collections.sort(changes, DomainChangeJdoAbstract.compareByTimestampDescThenTypeDesc());
        return changes;
    }
    public boolean hideRecentChanges(final Object targetDomainObject, final LocalDate from, final LocalDate to) {
        return targetDomainObject instanceof ViewModel || auditingServiceRepository == null || bookmarkService == null;
    }
    public LocalDate default1RecentChanges() {
        return clockService.now().minusDays(7);
    }
    public LocalDate default2RecentChanges() {
        return clockService.now();
    }


    @NotInServiceMenu
    @NotContributed(As.ASSOCIATION) // ie contributed as action
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="32")
    @Bulk(AppliesTo.BULK_ONLY)
    public List<? extends DomainChangeJdoAbstract> recentChanges(final Object targetDomainObject) {
        return recentChanges(targetDomainObject, default1RecentChanges(), default2RecentChanges());
    }
    
    // //////////////////////////////////////

    
    @javax.inject.Inject
    private CommandServiceJdoRepository commandServiceRepository;
    
    @javax.inject.Inject
    private AuditingServiceJdoRepository auditingServiceRepository;
    
    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private ClockService clockService;
    
}

