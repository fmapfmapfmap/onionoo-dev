/* Copyright 2011--2014 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.onionoo.updater;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.onionoo.docs.BandwidthStatus;
import org.torproject.onionoo.docs.DocumentStore;
import org.torproject.onionoo.docs.DocumentStoreFactory;

public class BandwidthStatusUpdater implements DescriptorListener,
    StatusUpdater {

  private DescriptorSource descriptorSource;

  private DocumentStore documentStore;

  public BandwidthStatusUpdater() {
    this.descriptorSource = DescriptorSourceFactory.getDescriptorSource();
    this.documentStore = DocumentStoreFactory.getDocumentStore();
    this.registerDescriptorListeners();
  }

  private void registerDescriptorListeners() {
    this.descriptorSource.registerDescriptorListener(this,
        DescriptorType.RELAY_EXTRA_INFOS);
    this.descriptorSource.registerDescriptorListener(this,
        DescriptorType.BRIDGE_EXTRA_INFOS);
  }

  public void processDescriptor(Descriptor descriptor, boolean relay) {
    if (descriptor instanceof ExtraInfoDescriptor) {
      this.parseDescriptor((ExtraInfoDescriptor) descriptor);
    }
  }

  public void updateStatuses() {
    /* Status files are already updated while processing descriptors. */
  }

  private void parseDescriptor(ExtraInfoDescriptor descriptor) {
    String fingerprint = descriptor.getFingerprint();
    BandwidthStatus bandwidthStatus = this.documentStore.retrieve(
        BandwidthStatus.class, true, fingerprint);
    if (bandwidthStatus == null) {
      bandwidthStatus = new BandwidthStatus();
    }
    if (descriptor.getWriteHistory() != null) {
      bandwidthStatus.addToWriteHistory(descriptor.getWriteHistory());
    }
    if (descriptor.getReadHistory() != null) {
      bandwidthStatus.addToReadHistory(descriptor.getReadHistory());
    }
    if (bandwidthStatus.isDirty()) {
      bandwidthStatus.compressHistory();
      this.documentStore.store(bandwidthStatus, fingerprint);
      bandwidthStatus.clearDirty();
    }
  }

  public String getStatsString() {
    /* TODO Add statistics string. */
    return null;
  }
}

