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

package org.apache.druid.query;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.apache.druid.query.QueryContexts.Vectorize;
import org.apache.druid.segment.QueryableIndexStorageAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * A user configuration holder for all query types.
 * Any query-specific configurations should go to their own configuration.
 *
 * @see org.apache.druid.query.groupby.GroupByQueryConfig
 * @see org.apache.druid.query.search.SearchQueryConfig
 * @see org.apache.druid.query.topn.TopNQueryConfig
 * @see org.apache.druid.query.metadata.SegmentMetadataQueryConfig
 * @see org.apache.druid.query.scan.ScanQueryConfig
 */
public class QueryConfig
{
  private Map<String, Object> configs = new HashMap<>();

  @JsonAnyGetter
  public Map<String, Object> getConfigs() {
    return configs;
  }

  public void setConfigs(Map<String, Object> configs) {
    this.configs = configs;
  }

  @JsonAnySetter
  public void setConfig(final String name, final Object value) {
    this.configs.put(name, value);
  }
}
