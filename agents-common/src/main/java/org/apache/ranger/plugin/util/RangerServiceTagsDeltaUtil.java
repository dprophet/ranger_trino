/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ranger.plugin.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ranger.plugin.model.RangerServiceResource;
import org.apache.ranger.plugin.model.RangerTag;
import org.apache.ranger.plugin.model.RangerTagDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RangerServiceTagsDeltaUtil {

    private static final Log LOG = LogFactory.getLog(RangerServiceTagsDeltaUtil.class);

    private static final Log PERF_TAGS_DELTA_LOG = RangerPerfTracer.getPerfLogger("tags.delta");

    /*
    It should be possible to call applyDelta() multiple times with serviceTags and delta resulting from previous call to applyDelta()
    The end result should be same if called once or multiple times.
     */
    static public ServiceTags applyDelta(ServiceTags serviceTags, ServiceTags delta) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RangerServiceTagsDeltaUtil.applyDelta()");
        }
        RangerPerfTracer perf = null;

        if(RangerPerfTracer.isPerfTraceEnabled(PERF_TAGS_DELTA_LOG)) {
            perf = RangerPerfTracer.getPerfTracer(PERF_TAGS_DELTA_LOG, "RangerServiceTagsDeltaUtil.applyDelta()");
        }
        if (serviceTags != null && !serviceTags.getIsDelta() && delta != null && delta.getIsDelta()) {
            serviceTags.setServiceName(delta.getServiceName());
            serviceTags.setTagVersion(delta.getTagVersion());

            // merge
            Map<Long, RangerTag>        tags             = serviceTags.getTags();
            List<RangerServiceResource> serviceResources = serviceTags.getServiceResources();
            Map<Long, List<Long>>       resourceToTagIds = serviceTags.getResourceToTagIds();
            boolean                     isAnyMaterialChange = false;

            for (Map.Entry<Long, RangerTag> tagEntry : delta.getTags().entrySet()) {
                if (StringUtils.isEmpty(tagEntry.getValue().getType())) {
                    if (null != tags.remove(tagEntry.getKey())) {
                        isAnyMaterialChange = true;
                    }
                } else {
                    tags.put(tagEntry.getKey(), tagEntry.getValue());
                }
            }

            // This could be expensive - compute time is M x N ( M - number of old tagged service resources, N - number of changed service resources)

            Map<Long, Long>             deletedServiceResources  = new HashMap<>();
            List<RangerServiceResource> addedServiceResources    = new ArrayList<>();

            for (RangerServiceResource serviceResource : delta.getServiceResources()) {

                boolean                         found = false;
                Iterator<RangerServiceResource> iter  = serviceResources.iterator();

                while (iter.hasNext()) {
                    RangerServiceResource existingResource = iter.next();

                    if (serviceResource.getId().equals(existingResource.getId())) {
                        if (!StringUtils.isEmpty(serviceResource.getResourceSignature())) {
                            if (!serviceResource.getResourceSignature().equals(existingResource.getResourceSignature())) { // ServiceResource changed
                                iter.remove();
                                existingResource.setResourceSignature(null);
                                addedServiceResources.add(existingResource);
                                break;
                            }
                        } else {
                            iter.remove();
                            deletedServiceResources.put(serviceResource.getId(), serviceResource.getId());
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (StringUtils.isNotEmpty(serviceResource.getResourceSignature())) {
                        serviceResources.add(serviceResource);
                        isAnyMaterialChange = true;
                    }
                }
            }

            for (Long deletedServiceResourceId : deletedServiceResources.keySet()) {
                resourceToTagIds.remove(deletedServiceResourceId);
            }

            resourceToTagIds.putAll(delta.getResourceToTagIds());

            // Ensure that any modified service-resources are at head of list of service-resources in delta
            // So that in in setServiceTags(), they get cleaned out first, and service-resource with new spec gets added

            addedServiceResources.addAll(delta.getServiceResources());
            delta.setServiceResources(addedServiceResources);

            if (!isAnyMaterialChange) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No material change may have occurred because of applying this delta");
                }
            }

        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot apply deltas to service-tags as one of preconditions is violated. Returning received serviceTags without applying delta!!");
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RangerServiceTagsDeltaUtil.applyDelta()");
        }

        RangerPerfTracer.log(perf);

        return serviceTags;
    }

    public static void pruneUnusedAttributes(ServiceTags serviceTags) {
        if (serviceTags != null) {
            serviceTags.setTagUpdateTime(null);

            for (Map.Entry<Long, RangerTagDef> entry : serviceTags.getTagDefinitions().entrySet()) {
                RangerTagDef tagDef = entry.getValue();
                tagDef.setCreatedBy(null);
                tagDef.setCreateTime(null);
                tagDef.setUpdatedBy(null);
                tagDef.setUpdateTime(null);
                tagDef.setGuid(null);
            }

            for (Map.Entry<Long, RangerTag> entry : serviceTags.getTags().entrySet()) {
                RangerTag tag = entry.getValue();
                tag.setCreatedBy(null);
                tag.setCreateTime(null);
                tag.setUpdatedBy(null);
                tag.setUpdateTime(null);
                tag.setGuid(null);
            }

            for (RangerServiceResource serviceResource : serviceTags.getServiceResources()) {
                serviceResource.setCreatedBy(null);
                serviceResource.setCreateTime(null);
                serviceResource.setUpdatedBy(null);
                serviceResource.setUpdateTime(null);
                serviceResource.setGuid(null);

                serviceResource.setServiceName(null);
            }
        }
    }
}
