package com.xipilli.persistence.dao.util.hql;

import java.util.Iterator;
import java.util.List;

import com.xipilli.persistence.dao.util.hql.HQLBuilder.HQLBuilderHelper;
import com.xipilli.persistence.model.AbstractPersistentEntity;

/**
 * Essentially wraps a string template to manage it for our HQL needs.
 */
public final class HQLTemplate {

    private final String entityTypeSimpleName;
    private final List<StringBuilder> buffBuff;

    /*package protected*/
    <T extends AbstractPersistentEntity> HQLTemplate(String entityTypeSimpleName, List<StringBuilder> buffBuff) {
        this.entityTypeSimpleName = entityTypeSimpleName;
        this.buffBuff = buffBuff;
    }

    /**
     * Renders the hql statement string by applying the provided args to the template.
     *
     * @param args to be applied to the template
     * @return the hql string
     */
    public String render(Object... args) {
        if (args.length != buffBuff.size()-1) {
            throw new IllegalArgumentException(String.format("Not the correct number of args.  The template has %s placeholders and %s args were passed in.", (buffBuff.size()-1), args.length));
        }

        //stub the statement with the from-entity clause
        StringBuilder buff = HQLBuilderHelper.prependFromClauseFor(entityTypeSimpleName);

        //apply the first buffer
        Iterator<StringBuilder> it = buffBuff.iterator();
        buff.append(it.next());

        //apply the rest with placeholders at the seam
        for (int i = 0; it.hasNext(); i++) {
            buff.append(args[i]);
            buff.append(it.next());
        }

        return buff.toString();
    }

    public String renderCnt(Object... args) {
        return HQLBuilderHelper.prependCountClauseFor(render(args)).toString();
    }
}