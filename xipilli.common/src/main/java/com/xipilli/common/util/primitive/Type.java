package com.xipilli.common.util.primitive;

import com.xipilli.common.util.StringUtil;


/**
 * Type primitive for entity name application throughout the application.
 */
public class Type {

    private final Class<?> clazz;
    private String original;
    private String lower;
    private String camel;

    public Type(Class<?> type) {
        this.clazz = type;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String get() {
        if (original == null) {
            original = clazz.getSimpleName();
        }
        return original;
    }

    public String getLower() {
        if (lower == null) {
            lower = get().toLowerCase();
        }
        return lower;
    }

    public String getCamel() {
        if (camel == null) {
            camel = StringUtil.toCamel(get());
        }
        return camel;
    }

    @Override
    public String toString() {
        return new StringBuilder("[Type=").append(get()).append("]").toString();
    }

}
