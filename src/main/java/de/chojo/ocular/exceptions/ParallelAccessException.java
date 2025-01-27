/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular.exceptions;

/**
 * This exception is thrown to indicate an illegal attempt to access a resource
 * or perform an operation that is not allowed due to concurrent or parallel access constraints.
 *
 * <p>For example, it can be thrown by the {@code lock(Key<?> key)} method in the
 * {@code KeyLocks} class when an attempt is made to acquire a lock on a {@code Key}
 * that is already locked by another process or thread.
 */
public class ParallelAccessException extends RuntimeException {
}
