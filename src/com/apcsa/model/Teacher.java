package com.apcsa.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.apcsa.model.User;

public class Teacher extends User {
	private int teacherId;
    private int departmentId;
    private String firstName;
    private String lastName;
    private String departmentName;
    
    public Teacher(User user, ResultSet rs) throws SQLException {
    	super(user);
    	
    	this.teacherId = rs.getInt("teacher_id");
    	this.departmentId = rs.getInt("department_id");
    	this.firstName = rs.getString("first_name");
    	this.lastName = rs.getString("last_name");
    	this.departmentName = rs.getString("department_id");
	}
	
    /**
     * Creates an instance of the Teacher class.
     * 
     * @param rs
     * @throws SQLException
     */

    public Teacher(ResultSet rs) throws SQLException {
        super(-1, "teacher", null, null, null);
        
        this.teacherId = rs.getInt("teacher_id");
        this.departmentId = rs.getInt("department_id");
        this.firstName = rs.getString("first_name");
        this.lastName = rs.getString("last_name");
        this.departmentName = rs.getString("title");
    }
    
}