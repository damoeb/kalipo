package org.kalipo.domain;

/**
 * Created by damoeb on 06.02.15.
 */
public interface Anonymizable<T> {
    T anonymized();

    String getThreadId();
}
