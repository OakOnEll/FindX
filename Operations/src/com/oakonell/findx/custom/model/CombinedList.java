package com.oakonell.findx.custom.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class CombinedList<E> implements List<E> {
	private List<E>[] lists;

	public CombinedList(List<E>... lists) {
		this.lists = lists;
	}

	@Override
	public E get(int index) {
		int offset = index;
		for (List<E> each : lists) {
			int size = each.size();
			if (offset < size) {
				return each.get(offset);
			}
			offset -= size;
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		for (List<E> each : lists) {
			each.clear();
		}
	}

	@Override
	public boolean isEmpty() {
		for (List<E> each : lists) {
			if (!each.isEmpty())
				return false;
		}
		return true;
	}

	@Override
	public int size() {
		int size = 0;
		for (List<E> each : lists) {
			size += each.size();
		}
		return size;
	}

	@Override
	public void add(int location, E object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(E object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int location, Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(Object object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator(int location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int location, E object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> subList(int start, int end) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		throw new UnsupportedOperationException();
	}
}
