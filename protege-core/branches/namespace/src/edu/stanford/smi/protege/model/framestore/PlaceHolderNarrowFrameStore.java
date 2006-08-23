package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;

/**
 * This is a placeholder with no functionality.  It is used as the root of a tree of 
 * NarrowFrameStore objects.
 * @author tredmond
 *
 */
public class PlaceHolderNarrowFrameStore implements NarrowFrameStore {

	public String getName() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void setName(String name) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public NarrowFrameStore getDelegate() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public int getFrameCount() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public int getClsCount() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public int getSlotCount() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public int getFacetCount() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public int getSimpleInstanceCount() {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Set getFrames() {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Frame getFrame(FrameID id) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public List getValues(Frame frame, Slot slot, Facet facet,
			boolean isTemplate) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public int getValuesCount(Frame frame, Slot slot, Facet facet,
			boolean isTemplate) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void addValues(Frame frame, Slot slot, Facet facet,
			boolean isTemplate, Collection values) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void moveValue(Frame frame, Slot slot, Facet facet,
			boolean isTemplate, int from, int to) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void removeValue(Frame frame, Slot slot, Facet facet,
			boolean isTemplate, Object value) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void setValues(Frame frame, Slot slot, Facet facet,
			boolean isTemplate, Collection values) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Set getFrames(Slot slot, Facet facet, boolean isTemplate,
			Object value) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate,
			String value, int maxMatches) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Set getReferences(Object value) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Set getMatchingReferences(String value, int maxMatches) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Set executeQuery(Query query) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void deleteFrame(Frame frame) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void close() {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Set getClosure(Frame frame, Slot slot, Facet facet,
			boolean isTemplate) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void replaceFrame(Frame frame) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public boolean beginTransaction(String name) {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public boolean commitTransaction() {

		throw new UnsupportedOperationException("Not implemented yet");
	}

	public boolean rollbackTransaction() {

		throw new UnsupportedOperationException("Not implemented yet");
	}

}
