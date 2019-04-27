package com.credibledoc.enricher.deriving;

import java.io.PrintWriter;
import java.util.List;

public interface Deriving {
    /**
     * @return The {@link Deriving#getPrintWriter()} field
     */
    PrintWriter getPrintWriter();

    /**
     * @return The {@link Deriving#getPrintWriter()} field
     */
    List<String> getCacheLines();
}
