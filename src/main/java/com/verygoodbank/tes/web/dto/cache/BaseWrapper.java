package com.verygoodbank.tes.web.dto.cache;

import java.io.Serializable;

/**
 * BaseWrapper is an interface that represents a basic wrapper structure.
 * It extends {@link Serializable}, ensuring that its implementers can be serialized.
 * This interface is typically used for objects that need to be wrapped with additional metadata
 * or functionality while maintaining the ability to be serialized, commonly for caching purposes.
 */
public interface BaseWrapper extends Serializable {}
