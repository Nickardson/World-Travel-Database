package edu.seminolestate.gratzer.wtd.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A bean which can be CRUD'ed
 * @author Taylor
 * @date 2016-02-13
 * @param <E> The same class of this bean
 */
public interface IBean<E> {
	/**
	 * Should take properties from the bean and insert to the table
	 * @param connection The connection
	 * @return This, for chaining
	 */
	public E create(Connection connection) throws SQLException;
	
	/**
	 * Should read from the database and put properties to the bean
	 * @param connection The connection
	 * @return This, for chaining
	 */
	public E read(Connection connection) throws SQLException;
	
	/**
	 * Should take properties from the bean and update to the table
	 * @param connection The connection
	 * @return This, for chaining
	 */
	public E update(Connection connection) throws SQLException;
	
	/**
	 * Should take properties from the bean and delete from the table
	 * @param connection The connection
	 * @return This, for chaining
	 */
	public E delete(Connection connection) throws SQLException;
	
	/**
	 * Should read the current row from the given ResultSet into this object's properties
	 * @param rs The ResultSet to read from
	 * @return This, for chaining
	 * @throws SQLException
	 */
	public E readResultSet(ResultSet rs) throws SQLException;
	
	/**
	 * This Methuselah of a method processes a PreparedStatement query and returns a list of processed IBeans of the given type.
	 * @param clazz The Class of the IBean.
	 * @param query The query to execute.
	 * @return A list of type clazz containing the things 
	 * @throws SQLException 
	 * @example
	 * for (User u : IBean.executeQuery(User.class, connection.prepareStatement("SELECT * FROM users"))) {
	 *     System.out.println(u);
	 * }
	 */
	public static <T extends IBean<?>> List<T> executeQuery(Class<T> clazz, PreparedStatement query) throws SQLException {
		ResultSet rs = query.executeQuery();
		List<T> list = new ArrayList<>();
		
		while (rs.next()) {
			try {
				T bean = clazz.newInstance();
				bean.readResultSet(rs);
				list.add(bean);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		rs.close();
		query.close();
		
		return list;
	}
}
