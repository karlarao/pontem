/*
 * Copyright 2018 Google LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.pontem;

import static org.junit.Assert.assertEquals;

import com.google.api.services.dataflow.model.JobMetrics;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Util}. */
@RunWith(JUnit4.class)
public class UtilTest {

  @Test
  public void testGetOutputPath() {
    assertEquals("foo/bar/", Util.getFormattedOutputPath("foo/bar"));
    assertEquals("foo/bar/", Util.getFormattedOutputPath("foo/bar/"));
  }

  @Test
  public void testGetGcsBucketNameFromDatabaseBackupLocation() throws Exception {
    assertEquals(
        "Bucket name parsing failed",
        "cloud-spanner-backup-test",
        Util.getGcsBucketNameFromDatabaseBackupLocation(
            "gs://cloud-spanner-backup-test/multi-backup"));

    assertEquals(
        "Bucket name parsing failed",
        "bucketName",
        Util.getGcsBucketNameFromDatabaseBackupLocation("gs://bucketName/multi-backup/djskd"));

    assertEquals(
        "Bucket name parsing failed",
        "bucketName2",
        Util.getGcsBucketNameFromDatabaseBackupLocation("gs://bucketName2/"));
  }

  @Test(expected = Exception.class)
  public void testGetGcsBucketNameFromDatabaseBackupLocation_invalidScheme() throws Exception {
    Util.getGcsBucketNameFromDatabaseBackupLocation("cs://cloud-spanner-backup-test/multi-backup");
  }

  @Test
  public void testGetGcsFolderPathFromDatabaseBackupLocation() throws Exception {
    assertEquals(
        "Folder path parsing failed",
        "/multi-backup/djskd/",
        Util.getGcsFolderPathFromDatabaseBackupLocation("gs://bucketName/multi-backup/djskd"));
    assertEquals(
        "Folder path parsing failed",
        "/multi-backup/",
        Util.getGcsFolderPathFromDatabaseBackupLocation("gs://bucketName/multi-backup/"));
    assertEquals(
        "Folder path parsing failed",
        "/",
        Util.getGcsFolderPathFromDatabaseBackupLocation("gs://bucketName"));
  }

  @Test(expected = Exception.class)
  public void testGetGcsFolderPathFromDatabaseBackupLocation_invalid() throws Exception {
    Util.getGcsFolderPathFromDatabaseBackupLocation("cs://bucketName/multi-backup/djskd");
  }

  @Test
  public void testConvertTablenamesIntoSet() throws Exception {
    assertEquals(
        "Parsing table names into Set failed",
        ImmutableSet.of("tableName1", "TableName2", "table_name_3"),
        Util.convertTablenamesIntoSet("tableName1\nTableName2\ntable_name_3"));

    assertEquals(
        "Parsing table names into Set failed",
        ImmutableSet.of("MyName1", "my-name-2B", "table_name_3"),
        Util.convertTablenamesIntoSet("MyName1\r\nmy-name-2B\r\ntable_name_3"));
  }

  @Test
  public void testConvertTableMetadataContentsToMap() throws Exception {
    assertEquals(
        "Parsing table names and num rows into Map failed",
        ImmutableMap.of(
            "AlbumPromotions", 1L, "seven_words", 7L, "two_hundred_million_words", 200000000L),
        Util.convertTableMetadataContentsToMap(
            "AlbumPromotions,1\nseven_words,7\ntwo_hundred_million_words,200000000"));

    assertEquals(
        "Parsing table names and num rows into Map failed",
        ImmutableMap.of(
            "MyTable100", 100L, "THEseven_words", 79L, "two_hundred_million_words", 200000000L),
        Util.convertTableMetadataContentsToMap(
            "MyTable100,100\nTHEseven_words,79\ntwo_hundred_million_words,200000000"));
  }

  @Test(expected = Exception.class)
  public void testConvertTableMetadataContentsToMap_invalidFormat() throws Exception {
    Util.convertTableMetadataContentsToMap(
        "AlbumPromotions\nseven_words,7\ntwo_hundred_million_words,200000000");
  }

  @Test
  public void testGetTableRowCountsFromJobMetrics() throws Exception {
    Map<String, Long> expectedParsedJobMetricsMap =
        ImmutableMap.of(
            "THEseven_words", 7L, "MyTable100", 100L, "two_hundred_million_words", 200000000L);
    JobMetrics jobMetrics = TestHelper.getJobMetrics(expectedParsedJobMetricsMap);

    Map<String, Long> actualParsedJobMetricsMap = Util.getTableRowCountsFromJobMetrics(jobMetrics);
    assertEquals(
        "Parsing table names and num rows into Map failed",
        expectedParsedJobMetricsMap,
        actualParsedJobMetricsMap);
  }
}
