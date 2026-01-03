package net.bitbylogic.kardia.util;

import com.google.common.annotations.Beta;

/**
 * Utility class that invokes methods when called.
 *
 * @param <T> The parameter for the methods.
 */
@Beta
public interface Callback<T> {

    void info(T t);

    void error(T t);
}
