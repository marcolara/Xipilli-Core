package com.xipilli.persistence.reveng.tmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.cfg.reveng.DelegatingReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategyUtil;
import org.hibernate.cfg.reveng.TableIdentifier;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;

/**
 * Use property name instead of return entity type.
 */
public class HibernateRevengStrategy extends DelegatingReverseEngineeringStrategy {

	public static final String					ID_SUFFIX				= "Id";
	public static final String					COMPOSITE_KEY_TEMPLATE	= "%s_%s";

	private static final Map<Table, Boolean>	M2M_RESULTS_CACHE		= new HashMap<Table, Boolean>();
	private static String						currTableName			= "";

	// hack in the logic for now
	public static final Set<String>				M2M_TABLES				= new HashSet<String>();
	{
		M2M_TABLES.add("SOCIAL_CHANNEL_SOCIAL_ENTRY");
		M2M_TABLES.add("TOPIC_SET_TOPIC");
	}

	public HibernateRevengStrategy(ReverseEngineeringStrategy arg0) {
		super(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String foreignKeyToEntityName(String keyName, TableIdentifier fromTable, List arg2, TableIdentifier toTable, List arg4, boolean uniqueReference) {
		List<Column> fromColumns = arg2;
		List<Column> toColumns = arg4;

		if (fromColumns.size() == 1) {
			String fromColumnName = fromColumns.get(0).getName();

			// resolve best descriptor when cols have same name
			if (toColumns.size() == 1 && fromColumnName.equals(toColumns.get(0).getName())) {
				// if fromTable has many cols that reference same entity, use
				// the fromColName because it has to be unique and is therefore
				// probably purposely distinguished enough
				if (!uniqueReference) {
					return Helper.scrapeIdSuffix(columnToPropertyName(fromTable, fromColumnName));
				}
				// otherwise, let the super resolve it
				return super.foreignKeyToEntityName(keyName, fromTable, fromColumns, toTable, toColumns, uniqueReference);
				// return "_unq"+uniqueReference+"_fr"+ fromTable.getName()
				// +"_to"+
				// toTable.getName()+"_"+super.foreignKeyToEntityName(keyName,
				// fromTable, fromColumns, toTable, toColumns, uniqueReference);
			}

			// otherwise use columnToPropertyName, which checks for manual
			// reveng property name overrides.
			return Helper.scrapeIdSuffix(columnToPropertyName(fromTable, fromColumnName));
		}

		return super.foreignKeyToEntityName(keyName, fromTable, fromColumns, toTable, toColumns, uniqueReference);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String foreignKeyToCollectionName(String keyName, TableIdentifier fromTable, List arg2, TableIdentifier toTable, List arg4, boolean uniqueReference) {
		List<Column> fromColumns = arg2;
		List<Column> toColumns = arg4;

		// Find the class name for elements of the collection and turn it into a
		// pluralized property
		String baseName = "_base_"
				+ ReverseEngineeringStrategyUtil.toUpperCamelCase(ReverseEngineeringStrategyUtil.simplePluralize(this.tableToClassName(fromTable)));

		// If unique, use the baseName. If not, tack on the property at the
		// other end of the foreign
		// key.
		if (uniqueReference) {
			return baseName;
		} else {
			return new StringBuilder(baseName).append("As").append(Helper.capitalizeFirst(foreignKeyToEntityName(keyName, fromTable, fromColumns, toTable, toColumns, uniqueReference))).toString();
		}
	}

	/**
	 * Makes m2m detection more liberal. Return true for
	 * super.isManyToManyTable() or where:
	 *
	 * 1) Table name = ENTITY_A_ENTITY_B 2) ENTITY_A and ENTITY_B are the exact
	 * names of the m2m entity tables 3) Table has at least 2 FKs that reference
	 * cols on ENTITY_A and ENTITY_B
	 *
	 * Note: Order of FKs, Table name composition, etc., doesn't matter.
	 *
	 * @param table
	 *            the table to analyze
	 *
	 * @return true for matching tables
	 */
	@Override
	public boolean isManyToManyTable(Table table) {
		if (M2M_TABLES.contains(table.getName())) {
			return true;
		}
		return super.isManyToManyTable(table);
	}

	public boolean _isManyToManyTable(Table table) {
		Boolean cacheValue = M2M_RESULTS_CACHE.get(table);
		if (cacheValue != null) {
			// BUFF.append(String.format("cacheResult = %s;",
			// cacheValue.toString()));
			return Boolean.TRUE.equals(cacheValue);
		}

		if (super.isManyToManyTable(table)) {
			return cacheAndReturn(table, true);
		}

		if (table.getColumnSpan() >= 3 && table.getName().indexOf('_') != -1) {
			// analyze the fks
			List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();
			{
				ForeignKey fk = null;
				for (Iterator<ForeignKey> it = table.getForeignKeyIterator(); it.hasNext();) {
					fk = it.next();
					foreignKeys.add(fk);
				}
			}

			// find 2 fks whose entities make up the composite key (aka the
			// table name)
			String compositeKey = table.getName();
			aLoop : for (ForeignKey fkA : foreignKeys) {
				int colSpanA = fkA.getColumnSpan();
				if (colSpanA > 1) {
					// BUFF.append("colSpanA > 1;");
					continue aLoop;
				}
				String fkEntityNameA = fkA.getReferencedTable().getName();
				bLoop : for (ForeignKey fkB : foreignKeys) {
					if (fkB.getColumnSpan() > 1) {
						// BUFF.append("colSpanB > 1;");
						continue bLoop;
					}
					String fkEntityNameB = fkB.getReferencedTable().getName();
					if (Helper.isCompositeMatch(compositeKey, fkEntityNameA, fkEntityNameB)
							|| Helper.isCompositeMatch(compositeKey, fkEntityNameB, fkEntityNameA)) {
						// BUFF.append("isManyToMany = true!;");
						// throw new RuntimeException(BUFF.toString());
						return cacheAndReturn(table, true);
					}
				}
			}
		}

		// BUFF.append("not m2m;");

		return cacheAndReturn(table, false);
	}

	private static boolean cacheAndReturn(Table table, boolean result) {
		M2M_RESULTS_CACHE.put(table, new Boolean(result));
		return result;
	}

	private static class Helper {

		private static String capitalizeFirst(String string) {
			if (string.length() > 0) {
				return new StringBuilder(string.substring(0, 1).toUpperCase()).append(string.substring(1)).toString();
			}
			return string;
		}

		private static String scrapeIdSuffix(String string) {
			if (string.length() > ID_SUFFIX.length() && string.endsWith(ID_SUFFIX)) {
				return string.substring(0, string.length() - ID_SUFFIX.length());
			}
			return string;
		}

		private static boolean isCompositeMatch(String compositeKey, String nameA, String nameB) {
			// BUFF.append(String.format("comparing %s to %s;", compositeKey,
			// String.format(COMPOSITE_KEY_TEMPLATE, nameA, nameB)));
			return compositeKey.equals(String.format(COMPOSITE_KEY_TEMPLATE, nameA, nameB));
		}
	}

	@Override
	public String tableToClassName(TableIdentifier tableIdentifier) {
		// catch the table we're currently working on.
		currTableName = tableIdentifier.getName();
		return super.tableToClassName(tableIdentifier);
	}

	@Override
	public boolean isOneToOne(ForeignKey foreignKey) {
		boolean isTrulyOneToOne = super.isOneToOne(foreignKey);

		if (isTrulyOneToOne) {
			/**
			 * we need the mapping to be {many-to-one unique="true"} on the
			 * table that owns the fk because hibernate doesn't handle the orm
			 * well otherwise and if we hacked it such that both sides were
			 * handled many-to-one then the one-side's entity would have an
			 * inaccurate Set property.
			 */
			return !foreignKey.getTable().getName().equals(currTableName);
		}

		return isTrulyOneToOne;
	}
}
