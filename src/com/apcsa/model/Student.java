package com.apcsa.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import com.apcsa.controller.Utils;
import com.apcsa.data.PowerSchool;
import com.apcsa.data.QueryUtils;
import com.apcsa.model.User;

public class Student extends User {

    private int studentId;
    private int classRank;
    private int gradeLevel;
    private int graduationYear;
    private double gpa;
    private String firstName;
    private String lastName;
    
    public Student(User user, ResultSet rs) throws SQLException {
    	super(user);
    	
    	this.studentId = rs.getInt("student_id");
    	this.classRank = rs.getInt("class_rank");
    	this.gradeLevel = rs.getInt("grade_level");
    	this.graduationYear = rs.getInt("graduation");
    	this.gpa = rs.getDouble("gpa");
    	this.firstName = rs.getString("first_name");
    	this.lastName = rs.getString("last_name");
    }
    
    public Student(ResultSet rs) throws SQLException {
    	super(-1, "student", null, null, null);
    	
    	this.studentId = rs.getInt("student_id");
    	this.classRank = rs.getInt("class_rank");
    	this.gradeLevel = rs.getInt("grade_level");
    	this.graduationYear = rs.getInt("graduation");
    	this.gpa = rs.getDouble("gpa");
    	this.firstName = rs.getString("first_name");
    	this.lastName = rs.getString("last_name");
    }

	public double getGpa() {
		return gpa;
	}
	
	public void viewCourseGrades() {
        System.out.print("\n");
        try (Connection conn = PowerSchool.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_COURSES_SQL);
            stmt.setInt(1, (int) this.getStudentId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String gradeInt;
                    gradeInt = String.valueOf((double) rs.getInt("grade"));
                    if (gradeInt.equals("0.0")) {
                        gradeInt = "--";
                    }
                    System.out.println(rs.getString("title") + " / " + gradeInt);
                }
            }
        } catch (SQLException e) {
            return;
        }
    }
	
	public void viewAssignmentGradesByCourse(Scanner in) {
        System.out.print("\n");
        ArrayList<String> course_nos = new ArrayList<String>();
        ArrayList<String> course_ids = new ArrayList<String>();

        int count = 1;
        int input = 0;
        int selection = 0;
        int markingPeriod = 0;
        String selectionString = "";
        boolean noAssignmentsInSelection = true;

        try (Connection conn = PowerSchool.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_NUMBERS_FOR_STUDENT);
            stmt.setInt(1, (int) this.getStudentId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("[" + count + "] " + rs.getString("course_no"));
                    count++;
                    course_nos.add(rs.getString("course_no"));
                    course_ids.add(rs.getString("course_id"));
                }
                System.out.print("\n::: ");
            } catch (SQLException e) {
                return;
            }
        } catch (SQLException e) {
        	return;
        }

        try {
            input = in.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("\nYour input was invalid. Please try again.");
        } finally {
            in.nextLine();
        }

        System.out.println("\n[1] MP1 Assignment.");
        System.out.println("[2] MP2 Assignment.");
        System.out.println("[3] MP3 Assignment.");
        System.out.println("[4] MP4 Assignment.");
        System.out.println("[5] Midterm Exam.");
        System.out.println("[6] Final Exam.");
        System.out.print("\n::: ");
        markingPeriod = Utils.getInt(in, -1);
        while(markingPeriod <= 0 || markingPeriod > 6) {
        	if(markingPeriod <= 0 || markingPeriod > 6) {
            	System.out.println("\nInvalid Selection.");
       	 	}
        	System.out.println("\nChoose a marking period or exam status.\n");
    		System.out.println("[1] MP1 assignment.");
            System.out.println("[2] MP2 assignment.");
            System.out.println("[3] MP3 assignment.");
            System.out.println("[4] MP4 assignment.");
            System.out.println("[5] Midterm exam.");
            System.out.println("[6] Final exam.");
            System.out.print("\n::: ");
            markingPeriod = Utils.getInt(in, -1);                 
        }
       
       

        try (Connection conn = PowerSchool.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM assignment_grades INNER JOIN assignments ON assignment_grades.assignment_id = assignments.assignment_id WHERE student_id = ? AND assignment_grades.course_id = ? AND assignments.marking_period = ?");
            stmt.setInt(1, (int) this.getStudentId());
            stmt.setString(2, course_ids.get(input - 1));
            stmt.setInt(3, markingPeriod);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.print("\n");
                int assignmentCount = 1;
                while (rs.next()) {
                    System.out.printf("%d. %s / %d (out of %d pts)\n", assignmentCount, rs.getString("title"), rs.getInt("points_earned"), rs.getInt("points_possible"));
                    noAssignmentsInSelection = false;

                    assignmentCount++;
                }
            }
            if (noAssignmentsInSelection) {
            	System.out.println("No assignments in selected course and term.");
            	
            }
        } catch (SQLException e) {
            return;
        }
        
    }

	
	public String getFirstName() {
		return firstName;
	}

	public void setClassRank(int i) {
		// TODO Auto-generated method stub
		
	}
	public double getStudentId() {
        return studentId;
    }
	
	public String getName() {
        return lastName + ", " + firstName;
    }
	
	public int getGraduationYear() {
		return graduationYear;
	}
	
	public int getClassRank() {
		return classRank;
	}
    
}