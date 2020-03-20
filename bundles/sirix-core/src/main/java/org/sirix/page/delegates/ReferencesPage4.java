/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: * Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer. * Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sirix.page.delegates;

import com.google.common.base.MoreObjects;
import org.sirix.api.PageTrx;
import org.sirix.node.interfaces.DataRecord;
import org.sirix.page.DeserializedReferencesPage4Tuple;
import org.sirix.page.PageReference;
import org.sirix.page.SerializationType;
import org.sirix.page.interfaces.KeyValuePage;
import org.sirix.page.interfaces.Page;
import org.sirix.settings.Constants;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to provide basic reference handling functionality.
 */
public final class ReferencesPage4 implements Page {

  /** Page reference 1. */
  private final List<PageReference> references;

  /** Page reference 4. */
  private final List<Short> offsets;

  /**
   * Constructor to initialize instance.
   */
  public ReferencesPage4() {
    references = new ArrayList<>(4);
    offsets = new ArrayList<>(4);
  }

  /**
   * Constructor to initialize instance.
   *
   * @param in input stream to read from
   * @param type the serialization type
   */
  public ReferencesPage4(final DataInput in, final SerializationType type) {
    final DeserializedReferencesPage4Tuple tuple = type.deserializeReferencesPage4(in);
    references = tuple.getReferences();
    offsets = tuple.getOffsets();
  }

  /**
   * Constructor to initialize instance.
   *
   * @param pageToClone committed page
   */
  public ReferencesPage4(final ReferencesPage4 pageToClone) {
    references = new ArrayList<>(4);
    offsets = new ArrayList<>(4);

    final var otherOffsets = pageToClone.getOffsets();

    for (int offset = 0, size = otherOffsets.size(); offset < size; offset++) {
      offsets.add(otherOffsets.get(offset));
      references.add(new PageReference().setKey(pageToClone.getReferences().get(offset).getKey()));
    }
  }

  public List<Short> getOffsets() {
    return offsets;
  }

  @Override
  public List<PageReference> getReferences() {
    return references;
  }

  /**
   * Get page reference of given offset.
   *
   * @param offset offset of page reference
   * @return {@link PageReference} at given offset
   */
  @Override
  public PageReference getReference(final @Nonnegative int offset) {
    for (int i = 0, count = offsets.size(); i < count; i++) {
      if (offsets.get(i) == offset) {
        return references.get(offset);
      }
    }

    if (offsets.size() < 4) {
      offsets.add((short) offset);
      final var newReference = new PageReference();
      references.add(newReference);
      return newReference;
    }

    return null;
  }

  @Override
  public boolean setReference(final int offset, final PageReference pageReference) {
    if (offsets.size() < 4) {
      offsets.add((short) offset);
      references.set(offsets.size(), pageReference);
      return false;
    }

    return true;
  }

  /**
   * Recursively call commit on all referenced pages.
   *
   * @param pageWriteTrx the page write transaction
   */
  @Override
  public final <K extends Comparable<? super K>, V extends DataRecord, S extends KeyValuePage<K, V>> void commit(
      @Nonnull final PageTrx<K, V, S> pageWriteTrx) {
    for (final PageReference reference : references) {
      if (reference.getLogKey() != Constants.NULL_ID_INT || reference.getPersistentLogKey() != Constants.NULL_ID_LONG) {
        pageWriteTrx.commit(reference);
      }
    }
  }

  /**
   * Serialize page references into output.
   *
   * @param out output stream
   * @param type the type to serialize (transaction intent log or the data file
   *        itself).
   */
  @Override
  public void serialize(final DataOutput out, final SerializationType type) {
    assert out != null;
    assert type != null;

    type.serializeReferencesPage4(out, references, offsets);
  }

  @Override
  public String toString() {
    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
    for (final int offset : offsets) {
      helper.add("offset", offset);
    }
    for (final PageReference ref : references) {
      helper.add("reference", ref);
    }
    return helper.toString();
  }
}
