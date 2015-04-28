package dbcache.cache.impl;

import java.io.Serializable;

import dbcache.cache.CacheUnit.ValueWrapper;

@SuppressWarnings("serial") 
public class NullHolder implements ValueWrapper, Serializable {

	@Override
	public Object get() {
		return null;
	}


	@Override
	public boolean equals(Object o) {
		return o == null || o == this;
	}

	@Override
	public int hashCode() {
		return 0;
	}

}