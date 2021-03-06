/* Copyright 2014 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.onionoo.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.torproject.onionoo.docs.DateTimeHelper;
import org.torproject.onionoo.docs.DocumentStore;
import org.torproject.onionoo.docs.DocumentStoreFactory;
import org.torproject.onionoo.docs.NodeStatus;
import org.torproject.onionoo.docs.SummaryDocument;
import org.torproject.onionoo.util.FormattingUtils;

public class SummaryDocumentWriter implements DocumentWriter {

  private final static Logger log = LoggerFactory.getLogger(
      SummaryDocumentWriter.class);

  private DocumentStore documentStore;

  public SummaryDocumentWriter() {
    this.documentStore = DocumentStoreFactory.getDocumentStore();
  }

  private int writtenDocuments = 0, deletedDocuments = 0;

  public void writeDocuments() {
    long relaysLastValidAfterMillis = -1L,
        bridgesLastPublishedMillis = -1L;
    for (String fingerprint : this.documentStore.list(NodeStatus.class)) {
      NodeStatus nodeStatus = this.documentStore.retrieve(
          NodeStatus.class, true, fingerprint);
      if (nodeStatus != null) {
        if (nodeStatus.isRelay()) {
          relaysLastValidAfterMillis = Math.max(
              relaysLastValidAfterMillis, nodeStatus.getLastSeenMillis());
        } else {
          bridgesLastPublishedMillis = Math.max(
              bridgesLastPublishedMillis, nodeStatus.getLastSeenMillis());
        }
      }
    }
    long cutoff = Math.max(relaysLastValidAfterMillis,
        bridgesLastPublishedMillis) - DateTimeHelper.ONE_WEEK;
    for (String fingerprint : this.documentStore.list(NodeStatus.class)) {
      NodeStatus nodeStatus = this.documentStore.retrieve(
          NodeStatus.class,
          true, fingerprint);
      if (nodeStatus == null) {
        continue;
      }
      if (nodeStatus.getLastSeenMillis() < cutoff) {
        if (this.documentStore.remove(SummaryDocument.class,
            fingerprint)) {
          this.deletedDocuments++;
        }
        continue;
      }
      boolean isRelay = nodeStatus.isRelay();
      String nickname = nodeStatus.getNickname();
      List<String> addresses = new ArrayList<String>();
      addresses.add(nodeStatus.getAddress());
      for (String orAddress : nodeStatus.getOrAddresses()) {
        if (!addresses.contains(orAddress)) {
          addresses.add(orAddress);
        }
      }
      for (String exitAddress : nodeStatus.getExitAddresses()) {
        if (!addresses.contains(exitAddress)) {
          addresses.add(exitAddress);
        }
      }
      long lastSeenMillis = nodeStatus.getLastSeenMillis();
      SortedSet<String> relayFlags = nodeStatus.getRelayFlags();
      boolean running = relayFlags.contains("Running") && (isRelay ?
          lastSeenMillis == relaysLastValidAfterMillis :
          lastSeenMillis == bridgesLastPublishedMillis);
      long consensusWeight = nodeStatus.getConsensusWeight();
      String countryCode = nodeStatus.getCountryCode();
      long firstSeenMillis = nodeStatus.getFirstSeenMillis();
      String aSNumber = nodeStatus.getASNumber();
      String contact = nodeStatus.getContact();
      SortedSet<String> declaredFamily = nodeStatus.getDeclaredFamily();
      SortedSet<String> effectiveFamily = nodeStatus.getEffectiveFamily();
      SummaryDocument summaryDocument = new SummaryDocument(isRelay,
          nickname, fingerprint, addresses, lastSeenMillis, running,
          relayFlags, consensusWeight, countryCode, firstSeenMillis,
          aSNumber, contact, declaredFamily, effectiveFamily);
      if (this.documentStore.store(summaryDocument, fingerprint)) {
        this.writtenDocuments++;
      };
    }
    log.info("Wrote summary document files");
  }

  public String getStatsString() {
    StringBuilder sb = new StringBuilder();
    sb.append("    " + FormattingUtils.formatDecimalNumber(
        this.writtenDocuments) + " summary document files written\n");
    sb.append("    " + FormattingUtils.formatDecimalNumber(
        this.deletedDocuments) + " summary document files deleted\n");
    return sb.toString();
  }
}
