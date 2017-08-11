package com.xipilli.persistence.dao.util;


import java.util.Date;
import java.util.List;

import javax.persistence.NonUniqueResultException;

/**
 * Toolbox of helper methods useful to DAO package.
 */
public final class DAOUtil {

    public static final Date now() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * Resolves an expected unique result from a results list.
     *
     * @param <X>
     * @param results
     * @return the sole result of type <X> or null if no results existed
     * @throws NonUniqueResultException if more than a single result were available.
     */
    public static final <X> X resolveUnique(List<X> results) throws NonUniqueResultException {
        if (results.size() == 1) {
            return results.get(0);
        } else if (results.size() == 0) {
            return null;
        } else {
            throw new NonUniqueResultException("More than 1 result were found!  findUnique calls when there can be more than a unique result are NOT supported.");
        }
    }

}