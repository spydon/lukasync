package lukasync;

import java.io.Serializable;

public class QueryBuilder implements Serializable {
	private static final long serialVersionUID = 1L;
	private String query = "";
	private String select;
	private String from;
	private String where;
	private String groupBy;
	private String orderBy;

	
	public QueryBuilder(String select, String from, String where, String groupBy, String orderBy) {
		this.select = select;
		this.from = from;
		this.where = where;
		this.groupBy = groupBy;
		this.orderBy = orderBy;
	}
	
	public String setSelect(String select) {
		this.select = select;
		return getQuery();
	}
	
	public String setFrom(String from) {
		this.from = from;
		return getQuery();
	}
	
	public String appendFrom(String from) {
		if(from.equals("")) {
			this.from = from;
		} else {
			this.from = this.from + " " + from;
		}
		return getQuery();		
	}
	
	public String setWhere(String where) {
		this.where = where;
		return getQuery();
	}
	
	public String appendWhere(String where) {
		if(where.equals("")) {
			this.where = where;
		} else {
			this.where = this.where + " AND " + where;
		}
		return getQuery();
	}
	
	public String setGroupBy(String groupBy) {
		this.groupBy = groupBy;
		return getQuery();
	}
	
	public String setOrderBy(String orderBy) {
		this.orderBy = orderBy;
		return getQuery();
	}
	
	public String getQuery() {
		query = "SELECT " + select;
		query = query.concat(from.equals("") ? "" : " FROM " + from);
		query = query.concat(where.equals("") ? "" : " WHERE " + where);
		query = query.concat(groupBy.equals("") ? "" : " GROUP BY " + groupBy);
		query = query.concat(orderBy.equals("") ? "" : " ORDER BY " + orderBy);
		return query;
	}
}
