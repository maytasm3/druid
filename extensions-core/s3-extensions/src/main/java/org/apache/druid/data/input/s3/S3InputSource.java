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

package org.apache.druid.data.input.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import org.apache.druid.data.input.InputEntity;
import org.apache.druid.data.input.InputFileAttribute;
import org.apache.druid.data.input.InputSplit;
import org.apache.druid.data.input.SplitHintSpec;
import org.apache.druid.data.input.impl.CloudObjectInputSource;
import org.apache.druid.data.input.impl.CloudObjectLocation;
import org.apache.druid.data.input.impl.SplittableInputSource;
import org.apache.druid.storage.s3.S3InputDataConfig;
import org.apache.druid.storage.s3.S3StorageDruidModule;
import org.apache.druid.storage.s3.S3Utils;
import org.apache.druid.storage.s3.ServerSideEncryptingAmazonS3;
import org.apache.druid.utils.Streams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class S3InputSource extends CloudObjectInputSource
{
  // We lazily initialize s3ClientSupplier to avoid costly s3 operation when we only need S3InputSource for stored information
  // (such as for task logs) and not for ingestion. (This cost only applies for new s3ClientSupplier created with
  // s3InputSourceProperties given).
  private final Supplier<ServerSideEncryptingAmazonS3> s3ClientSupplier;
  @JsonProperty("properties")
  private final S3InputSourceProperties s3InputSourceProperties;
  private final S3InputDataConfig inputDataConfig;

  @JsonCreator
  public S3InputSource(
      @JacksonInject ServerSideEncryptingAmazonS3 s3Client,
      @JacksonInject ServerSideEncryptingAmazonS3.Builder serverSideEncryptingAmazonS3Builder,
      @JacksonInject S3InputDataConfig inputDataConfig,
      @JsonProperty("uris") @Nullable List<URI> uris,
      @JsonProperty("prefixes") @Nullable List<URI> prefixes,
      @JsonProperty("objects") @Nullable List<CloudObjectLocation> objects,
      @JsonProperty("properties") @Nullable S3InputSourceProperties s3InputSourceProperties
  )
  {
    super(S3StorageDruidModule.SCHEME, uris, prefixes, objects);
    this.inputDataConfig = Preconditions.checkNotNull(inputDataConfig, "S3DataSegmentPusherConfig");
    Preconditions.checkNotNull(s3Client, "s3Client");
    this.s3InputSourceProperties = s3InputSourceProperties;
    this.s3ClientSupplier = Suppliers.memoize(
        () -> {
          if (serverSideEncryptingAmazonS3Builder != null && s3InputSourceProperties != null) {
            if (s3InputSourceProperties.isCredentialsConfigured()) {
              AWSStaticCredentialsProvider credentials = new AWSStaticCredentialsProvider(
                  new BasicAWSCredentials(
                    s3InputSourceProperties.getAccessKeyId().getPassword(),
                    s3InputSourceProperties.getSecretAccessKey().getPassword()
                  )
              );
              serverSideEncryptingAmazonS3Builder.getAmazonS3ClientBuilder().withCredentials(credentials);
            }
            return serverSideEncryptingAmazonS3Builder.build();
          } else {
            return s3Client;
          }
        }
    );
  }

  @Nullable
  @JsonProperty("properties")
  public S3InputSourceProperties getS3InputSourceProperties()
  {
    return s3InputSourceProperties;
  }

  @Override
  protected InputEntity createEntity(CloudObjectLocation location)
  {
    return new S3Entity(s3ClientSupplier.get(), location);
  }

  @Override
  protected Stream<InputSplit<List<CloudObjectLocation>>> getPrefixesSplitStream(@Nonnull SplitHintSpec splitHintSpec)
  {
    final Iterator<List<S3ObjectSummary>> splitIterator = splitHintSpec.split(
        getIterableObjectsFromPrefixes().iterator(),
        object -> new InputFileAttribute(object.getSize())
    );

    return Streams.sequentialStreamFrom(splitIterator)
                  .map(objects -> objects.stream()
                                         .map(S3Utils::summaryToCloudObjectLocation)
                                         .collect(Collectors.toList()))
                  .map(InputSplit::new);
  }

  @Override
  public SplittableInputSource<List<CloudObjectLocation>> withSplit(InputSplit<List<CloudObjectLocation>> split)
  {
    return new S3InputSource(
        s3ClientSupplier.get(),
        null,
        inputDataConfig,
        null,
        null,
        split.get(),
        getS3InputSourceProperties()
    );
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(super.hashCode(), s3InputSourceProperties);
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    S3InputSource that = (S3InputSource) o;
    return Objects.equals(s3InputSourceProperties, that.s3InputSourceProperties);
  }

  @Override
  public String toString()
  {
    return "S3InputSource{" +
           "uris=" + getUris() +
           ", prefixes=" + getPrefixes() +
           ", objects=" + getObjects() +
           ", s3InputSourceProperties=" + getS3InputSourceProperties() +
           '}';
  }

  private Iterable<S3ObjectSummary> getIterableObjectsFromPrefixes()
  {
    return () -> S3Utils.objectSummaryIterator(s3ClientSupplier.get(), getPrefixes(), inputDataConfig.getMaxListingLength());
  }
}
