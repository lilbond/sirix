package org.sirix.index.name.xml;

import org.sirix.api.PageTrx;
import org.sirix.index.IndexDef;
import org.sirix.index.name.NameIndexBuilderFactory;
import org.sirix.index.name.NameIndexListenerFactory;
import org.sirix.node.interfaces.DataRecord;
import org.sirix.page.UnorderedKeyValuePage;

public final class XmlNameIndexImpl implements XmlNameIndex {

  private final NameIndexBuilderFactory nameIndexBuilderFactory;

  private final NameIndexListenerFactory nameIndexListenerFactory;

  public XmlNameIndexImpl() {
    nameIndexBuilderFactory = new NameIndexBuilderFactory();
    nameIndexListenerFactory = new NameIndexListenerFactory();
  }

  @Override
  public XmlNameIndexBuilder createBuilder(final PageTrx<Long, DataRecord, UnorderedKeyValuePage> pageWriteTrx,
      final IndexDef indexDef) {
    final var nameIndexBuilderDelegate = nameIndexBuilderFactory.create(pageWriteTrx, indexDef);
    return new XmlNameIndexBuilder(nameIndexBuilderDelegate);
  }

  @Override
  public XmlNameIndexListener createListener(final PageTrx<Long, DataRecord, UnorderedKeyValuePage> pageWriteTrx,
      final IndexDef indexDef) {
    final var nameIndexListenerDelegate = nameIndexListenerFactory.create(pageWriteTrx, indexDef);
    return new XmlNameIndexListener(nameIndexListenerDelegate);
  }
}
