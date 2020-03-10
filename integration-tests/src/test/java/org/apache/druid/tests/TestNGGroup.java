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

package org.apache.druid.tests;

/**
 * These groups are used by continuous integration to split the integration tests into multiple jobs. Any tests that
 * are not annotated with a group will still be run by an "other" integration test continuous integration job.
 */
public class TestNGGroup
{
  public static final String BATCH_INDEX = "batch-index";
  public static final String HADOOP_INDEX = "hadoop-index";
  public static final String KAFKA_INDEX = "kafka-index";
  public static final String OTHER_INDEX = "other-index";
  public static final String PERFECT_ROLLUP_PARALLEL_BATCH_INDEX = "perfect-rollup-parallel-batch-index";
  public static final String QUERY = "query";
  public static final String REALTIME_INDEX = "realtime-index";
  public static final String SECURITY = "security";
  // This group is not part of CI. To run this group, s3 configs/credentials for your s3 must be provided in a file.
  // The path of the file must then we pass to mvn with -Doverride.config.path=<PATH_TO_FILE>.
  // See integration-tests/docker/environment-configs/override-examples/s3 for env var to provide.
  public static final String S3_DEEP_STORAGE = "s3-deep-storage";
  // This group is not part of CI. To run this group, gcs configs/credentials for your gcs must be provided in a file.
  // The path of the file must then we pass to mvn with -Doverride.config.path=<PATH_TO_FILE>.
  // See integration-tests/docker/environment-configs/override-examples/gcs for env var to provide.
  public static final String GCS_DEEP_STORAGE = "gcs-deep-storage";
  // This group is not part of CI. To run this group, azure configs/credentials for your azure must be provided in a file.
  // The path of the file must then we pass to mvn with -Doverride.config.path=<PATH_TO_FILE>.
  // See integration-tests/docker/environment-configs/override-examples/azures for env var to provide.
  public static final String AZURE_DEEP_STORAGE = "azure-deep-storage";
}
