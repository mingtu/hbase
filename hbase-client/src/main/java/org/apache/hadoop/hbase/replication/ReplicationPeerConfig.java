/**
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

package org.apache.hadoop.hbase.replication;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.hadoop.hbase.TableName;
import org.apache.yetus.audience.InterfaceAudience;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * A configuration for the replication peer cluster.
 */
@InterfaceAudience.Public
public class ReplicationPeerConfig {

  private String clusterKey;
  private String replicationEndpointImpl;
  private final Map<byte[], byte[]> peerData;
  private final Map<String, String> configuration;
  private Map<TableName, ? extends Collection<String>> tableCFsMap = null;
  private Set<String> namespaces = null;
  private long bandwidth = 0;
  // Default value is true, means replicate all user tables to peer cluster.
  private boolean replicateAllUserTables = true;
  private Map<TableName, ? extends Collection<String>> excludeTableCFsMap = null;
  private Set<String> excludeNamespaces = null;

  public ReplicationPeerConfig() {
    this.peerData = new TreeMap<>(Bytes.BYTES_COMPARATOR);
    this.configuration = new HashMap<>(0);
  }

  /**
   * Set the clusterKey which is the concatenation of the slave cluster's:
   *          hbase.zookeeper.quorum:hbase.zookeeper.property.clientPort:zookeeper.znode.parent
   */
  public ReplicationPeerConfig setClusterKey(String clusterKey) {
    this.clusterKey = clusterKey;
    return this;
  }

  /**
   * Sets the ReplicationEndpoint plugin class for this peer.
   * @param replicationEndpointImpl a class implementing ReplicationEndpoint
   */
  public ReplicationPeerConfig setReplicationEndpointImpl(String replicationEndpointImpl) {
    this.replicationEndpointImpl = replicationEndpointImpl;
    return this;
  }

  public String getClusterKey() {
    return clusterKey;
  }

  public String getReplicationEndpointImpl() {
    return replicationEndpointImpl;
  }

  public Map<byte[], byte[]> getPeerData() {
    return peerData;
  }

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public Map<TableName, List<String>> getTableCFsMap() {
    return (Map<TableName, List<String>>) tableCFsMap;
  }

  public ReplicationPeerConfig setTableCFsMap(Map<TableName,
                                              ? extends Collection<String>> tableCFsMap) {
    this.tableCFsMap = tableCFsMap;
    return this;
  }

  public Set<String> getNamespaces() {
    return this.namespaces;
  }

  public ReplicationPeerConfig setNamespaces(Set<String> namespaces) {
    this.namespaces = namespaces;
    return this;
  }

  public long getBandwidth() {
    return this.bandwidth;
  }

  public ReplicationPeerConfig setBandwidth(long bandwidth) {
    this.bandwidth = bandwidth;
    return this;
  }

  public boolean replicateAllUserTables() {
    return this.replicateAllUserTables;
  }

  public ReplicationPeerConfig setReplicateAllUserTables(boolean replicateAllUserTables) {
    this.replicateAllUserTables = replicateAllUserTables;
    return this;
  }

  public Map<TableName, List<String>> getExcludeTableCFsMap() {
    return (Map<TableName, List<String>>) excludeTableCFsMap;
  }

  public ReplicationPeerConfig setExcludeTableCFsMap(Map<TableName,
                                              ? extends Collection<String>> tableCFsMap) {
    this.excludeTableCFsMap = tableCFsMap;
    return this;
  }

  public Set<String> getExcludeNamespaces() {
    return this.excludeNamespaces;
  }

  public ReplicationPeerConfig setExcludeNamespaces(Set<String> namespaces) {
    this.excludeNamespaces = namespaces;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("clusterKey=").append(clusterKey).append(",");
    builder.append("replicationEndpointImpl=").append(replicationEndpointImpl).append(",");
    builder.append("replicateAllUserTables=").append(replicateAllUserTables).append(",");
    if (replicateAllUserTables) {
      if (excludeNamespaces != null) {
        builder.append("excludeNamespaces=").append(excludeNamespaces.toString()).append(",");
      }
      if (excludeTableCFsMap != null) {
        builder.append("excludeTableCFsMap=").append(excludeTableCFsMap.toString()).append(",");
      }
    } else {
      if (namespaces != null) {
        builder.append("namespaces=").append(namespaces.toString()).append(",");
      }
      if (tableCFsMap != null) {
        builder.append("tableCFs=").append(tableCFsMap.toString()).append(",");
      }
    }
    builder.append("bandwidth=").append(bandwidth);
    return builder.toString();
  }

  /**
   * Decide whether the table need replicate to the peer cluster
   * @param table name of the table
   * @return true if the table need replicate to the peer cluster
   */
  public boolean needToReplicate(TableName table) {
    if (replicateAllUserTables) {
      if (excludeNamespaces != null && excludeNamespaces.contains(table.getNamespaceAsString())) {
        return false;
      }
      if (excludeTableCFsMap != null && excludeTableCFsMap.containsKey(table)) {
        return false;
      }
      return true;
    } else {
      if (namespaces != null && namespaces.contains(table.getNamespaceAsString())) {
        return true;
      }
      if (tableCFsMap != null && tableCFsMap.containsKey(table)) {
        return true;
      }
      return false;
    }
  }
}
