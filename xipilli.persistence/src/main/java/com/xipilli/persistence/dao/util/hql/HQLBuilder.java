package com.xipilli.persistence.dao.util.hql;

import java.util.ArrayList;
import java.util.List;

import com.xipilli.persistence.dao.IBaseDAO;
import com.xipilli.persistence.model.AbstractPersistentEntity;

/**
 * Builds HQL strings.  Uses fluent methods with progressive string arguments to make the
 * statement's construction like writing a sentence.
 */
public final class HQLBuilder {
    public static final String COUNT_ = "select count(*) ";
    public static final String FROM_ = "from ";
    public static final String ORDER_BY_ = "order by ";
    public static final String STATUS_IS_NOT_INACTIVE = AbstractPersistentEntity.STATUS + " != '" + IBaseDAO.STATUS_INACTIVE + "' ";

    private static final String STATIC_PLACEHOLDER = ";%"; //something should never exist in hql
    private static final String STATIC_PLACEHOLDER_REGEX = String.format("\\Q%s\\E", STATIC_PLACEHOLDER);

    private static final String PLACEHOLDER = ";#"; //same as above but slightly diff
    private static final String PLACEHOLDER_REGEX = String.format("\\Q%s\\E", PLACEHOLDER);

    private boolean hasStaticPlaceholder = false;
    private boolean hasPlaceholder = false;

    private final StringBuilder buff;

    public static HQLBuilder fromEntity(String progressiveHqlSegment) {
        return new HQLBuilder(progressiveHqlSegment);
    }

    private HQLBuilder(String hqlSegment) {
        buff = new StringBuilder(hqlSegment);
    }

    public HQLBuilder statusIsNotInactive() {
        return statusIsNotInactive("");
    }

    public HQLBuilder statusIsNotInactive(String progressiveHqlSegment) {
        buff.append(STATUS_IS_NOT_INACTIVE);
        buff.append(progressiveHqlSegment);
        return this;
    }

    public HQLBuilder orderBy(String progressiveHqlSegment) {
        buff.append(ORDER_BY_);
        buff.append(progressiveHqlSegment);
        return this;
    }



    public HQLBuilder staticPlaceholder(String progressiveHqlSegment) {
        hasStaticPlaceholder = true;
        buff.append(STATIC_PLACEHOLDER);
        buff.append(progressiveHqlSegment);
        return this;
    }

    public HQLBuilder staticPlaceholder() {
        return staticPlaceholder("");
    }

    public HQLBuilder placeholder(String progressiveHqlSegment) {
        hasPlaceholder = true;
        buff.append(PLACEHOLDER);
        buff.append(progressiveHqlSegment);
        return this;
    }

    public HQLBuilder placeholder() {
        return placeholder("");
    }

    /**
     * Directly build and render the template to hql.  Assumes the statement is entirely static.
     * This method facilitates the use of this class for its fluent shortcut methods for common
     * HQL phrases.
     *
     * @param entityType
     * @param staticArgs
     * @return the hql string
     */
    public <T extends AbstractPersistentEntity> String buildFor(Class<T> entityType, Object... staticArgs) {
        if (hasPlaceholder) {
            throw new IllegalStateException("Can only build on HQL with static placeholders, not runtime placeholders.  See toHQLTemplateFor() method.");
        }
        String staticString = HQLBuilderHelper.prependFromClauseFor(entityType).append(buff).toString();
        if (hasStaticPlaceholder) {
            staticString = replaceStaticPlaceholders(staticString, staticArgs);
        }
        //if no runtime placeholders, don't need the template.
        return staticString;
    }

    /**
     * Builds the HQL and sets it into a template for runtime placeholder management.
     *
     * @param entityType
     * @param staticArgs
     * @return the HQLTemplate
     */
    public <T extends AbstractPersistentEntity> HQLTemplate buildHQLTemplateFor(Class<T> entityType, Object... staticArgs) {
        //render for static placeholders
        String staticString = buff.toString();
        if (hasStaticPlaceholder) {
            staticString = replaceStaticPlaceholders(staticString, staticArgs);
        }

        //split into a series of builders for the template
        List<StringBuilder> buffBuff = new ArrayList<StringBuilder>();
        if (hasPlaceholder) {
            String[] byPlaceholder = staticString.split(PLACEHOLDER_REGEX);
            for (String s : byPlaceholder) {
                buffBuff.add(new StringBuilder(s));
            }
        } else {
            buffBuff.add(new StringBuilder(staticString));
        }

        return new HQLTemplate(entityType.getSimpleName(), buffBuff);
    }

    private String replaceStaticPlaceholders(String staticString, Object... staticArgs) {
        try {
            for (int i = 0; staticString.indexOf(STATIC_PLACEHOLDER) != -1; i++) {
                staticString = staticString.replaceFirst(STATIC_PLACEHOLDER_REGEX, staticArgs[i].toString());
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Inaccurate number of static placeholder arguments.");
        }
        return staticString;
    }

    /* package protected */
    static class HQLBuilderHelper {
	public static <T extends AbstractPersistentEntity> StringBuilder prependFromClauseFor(Class<T> entityType) {
		return prependFromClauseFor(entityType.getSimpleName());
	}

	public static StringBuilder prependFromClauseFor(String entityTypeSimpleName) {
		return new StringBuilder(FROM_).append(entityTypeSimpleName);
	}

	public static <T extends AbstractPersistentEntity> StringBuilder prependCountClauseFor(Class<T> entityType) {
		return prependCountClauseFor(entityType.getSimpleName());
	}

	public static StringBuilder prependCountClauseFor(String entityTypeSimpleName) {
		return new StringBuilder(COUNT_).append(entityTypeSimpleName);
	}
    }
}