package com.apcsa.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.data.QueryUtils;
import com.apcsa.model.Student;
import com.apcsa.model.Teacher;
import com.apcsa.model.User;

public class Application {

    private Scanner in;
    private User activeUser;
    
    enum RootAction { PASSWORD, DATABASE, LOGOUT, SHUTDOWN, INVALID }
    enum AdministratorAction { FACULTY, DEPARTMENT, STUDENTS, GRADE, COURSE, PASSWORD, LOGOUT, INVALID }
    enum TeacherAction { COURSE, ADD, DELETE, GRADE, PASSWORD, LOGOUT, INVALID }
    enum StudentAction { GRADES, COURSE, PASSWORD, LOGOUT, INVALID }
    
    /**
     * Creates an instance of the Application class, which is responsible for interacting
     * with the user via the command line interface.
     */

    public Application() {
        this.in = new Scanner(System.in);

        try {
            PowerSchool.initialize(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the PowerSchool application.
     */

    public void startup() {
        System.out.println("PowerSchool -- now for students, teachers, and school administrators!");

        // continuously prompt for login credentials and attempt to login

        while (true) {
            System.out.print("\nUsername: ");
            String username = in.next();

            System.out.print("Password: ");
            String password = in.next();

            // if login is successful, update generic user to administrator, teacher, or student
            try {
            if (login(username, password)) {
                activeUser = activeUser.isAdministrator()
                    ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
                    ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
                    ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
                    ? activeUser : null;

                if (isFirstLogin() && !activeUser.isRoot()) {
                    System.out.print("\nEnter a new password: ");
                    String newPassword = in.next();
                    activeUser.setPassword(newPassword);
                    String auth = activeUser.getPassword();
					try (Connection conn = PowerSchool.getConnection()){
						PowerSchool.updateAuth(conn, username, auth);
	                    System.out.println("\nYour password has been changed to " + newPassword);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
                }
                createAndShowUI();
            } else {
                System.out.println("\nInvalid username and/or password.");
            }
            }catch(Exception e){
            	shutdown(e);
            }
        }
    }
    
    /**
     * Displays an user type-specific menu with which the user
     * navigates and interacts with the application.
     */

    public void createAndShowUI() {
        System.out.println("\nHello, again, " + activeUser.getFirstName() + "!");
        
        if (activeUser.isRoot()) {
            showRootUI();
        } else if (activeUser.isAdministrator()) {
            showAdministratorUI();
        } else if(activeUser.isTeacher()){
        	showTeacherUI();
        }else if(activeUser.isStudent()){
        	showStudentUI();
        }else {
        	
        }
    }
    
    
    /*
     * Displays an interface for root users.
     */

    private void showRootUI() {
        while (activeUser != null) {
            switch (getRootMenuSelection()) {
                case PASSWORD: resetPassword(); break;
                case DATABASE: factoryReset(); break;
                case LOGOUT: logout(); break;
                case SHUTDOWN: shutdown(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    /*
     * Retrieves a root user's menu selection.
     * 
     * @return the menu selection
     */

    private RootAction getRootMenuSelection() {
        System.out.println();
        
        System.out.println("[1] Reset user password.");
        System.out.println("[2] Factory reset database.");
        System.out.println("[3] Logout.");
        System.out.println("[4] Shutdown.");
        System.out.print("\n::: ");
        
        switch (Utils.getInt(in, -1)) {
            case 1: return RootAction.PASSWORD;
            case 2: return RootAction.DATABASE;
            case 3: return RootAction.LOGOUT;
            case 4: return RootAction.SHUTDOWN;
            default: return RootAction.INVALID;
        }
     }
    
    private void viewFaculty() {        
        ArrayList<Teacher> teachers = PowerSchool.getTeachers();
        
        if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + teacher.getDepartmentName());
            } 
        }
    }
    
    
    private void viewFacultyByDepartment() {    	
    	 ArrayList<Teacher> teachers = PowerSchool.getTeachersByDepartment(getDepartmentSelection());
    	
    	if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + teacher.getDepartmentName());
            } 
        }
    }

    /*
     * Displays all students.
     */

    private void viewStudents() {
    	ArrayList<Student> students = PowerSchool.getStudents();
        
        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + student.getGraduationYear());
            } 
        }
    }
    
    private void viewStudentsByGrade() {
    	ArrayList<Student> students = Utils.updateRanks(PowerSchool.getStudentsByGrade(getGradeSelection()));
    	
    	if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + "#" + student.getClassRank());
            } 
        }
    }
    
    

	private void viewStudentsByCourse() {
		String courseNo = "";
		try {
		courseNo = getCourseSelection();
		}catch(SQLException e) {
			
		}
		ArrayList<Student> students = PowerSchool.getStudentsByCourse(courseNo);
    	
    	if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + fixGPA(student));
            } 
        }
	}
	
	private String fixGPA(Student student) {
		double GPA = student.getGpa();
		if(GPA == -1) {
			return "--";
		}else {
			return String.valueOf(GPA);
		}
	}
	
	private void changePassword() {
		System.out.print("\nEnter current password: ");
		String currentPassword = in.next();
		System.out.print("Enter a new password: ");
        String newPassword = in.next();
        if(activeUser.getPassword().equals(Utils.getHash(currentPassword))) {
        	activeUser.setPassword(newPassword);
            String auth = activeUser.getPassword();
    		try (Connection conn = PowerSchool.getConnection()){
    			PowerSchool.updateAuth(conn, activeUser.getUsername(), auth);
                System.out.println("\nYour password has been changed to " + newPassword);
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
        }else {
        	System.out.println("\nInvalid current password.");
        }
	}
	
	/*
	 * Retrieves the user's department selection.
	 * 
	 * @return the selected department
	 */

	private int getDepartmentSelection() {
	    int selection = -1;
	    System.out.println("\nChoose a department.");
	    
	    while (selection < 1 || selection > 6) {
	        System.out.println("\n[1] Computer Science.");
	        System.out.println("[2] English.");
	        System.out.println("[3] History.");
	        System.out.println("[4] Mathematics.");
	        System.out.println("[5] Physical Education.");
	        System.out.println("[6] Science.");
	        System.out.print("\n::: ");
	        
	        selection = Utils.getInt(in, -1);
	    }
	    
	    return selection;
	}
	
	/*
	 * Retrieves a user's grade selection.
	 * 
	 * @return the selected grade
	 */

	private int getGradeSelection() {
	    int selection = -1;
	    System.out.println("\nChoose a grade level.");
	    
	    while (selection < 1 || selection > 4) {
	        System.out.println("\n[1] Freshman.");
	        System.out.println("[2] Sophomore.");
	        System.out.println("[3] Junior.");
	        System.out.println("[4] Senior.");
	        System.out.print("\n::: ");
	        
	        selection = Utils.getInt(in, -1);
	    }
	    
	    return selection + 8;   // +8 because you want a value between 9 and 12
	}
	
	/*
	 * Retrieves a user's course selection.
	 * 
	 * @return the selected course
	 */

	private String getCourseSelection() throws SQLException {
	    boolean valid = false;
	    String courseNo = null;
	    
	    while (!valid) {
	        System.out.print("\nCourse No.: ");
	        courseNo = in.next();
	        
	        if (isValidCourse(courseNo)) {
	            valid = true;
	        } else {
	            System.out.println("\nCourse not found.");
	        }
	    }
	    
	    return courseNo;
	}

    private boolean isValidCourse(String courseId) {
    	boolean validCourse = false;
		for(int i=1; i <  PowerSchool.getNumberOfCourses(); i++) {
			if(PowerSchool.getCourseNumber(i).equals(courseId)) {
				validCourse = true;
			}
		}
		return validCourse;
	}

	/*
     * Allows a root user to reset another user's password.
     */
    
    private void resetPassword() {
    	System.out.print("\nUsername: ");
    	String username = in.next();
    	if (Utils.confirm(in, "\nAre you sure you want to reset the password for " + username + "?  (y/n) ")) {
            if (in != null) {
            	if(PowerSchool.resetPassword(username)) {
            		PowerSchool.resetLastLogin(username);
            		System.out.println("\nSuccessfully reset password for " + username + ".");
            	}else {
            		System.out.println("\nPassword reset failed");
            	}
            }
    	}
    }
    
    /*
     * Displays an interface for root users.
     */
        
    private void showAdministratorUI() {
        while (activeUser != null) {
            switch (getAdministratorMenuSelection()) {
                case FACULTY: viewFaculty(); break;
                case DEPARTMENT: viewFacultyByDepartment(); break;
                case STUDENTS: viewStudents(); break;
                case GRADE: viewStudentsByGrade(); break;
                case COURSE: viewStudentsByCourse(); break;
                case PASSWORD: changePassword(); break;
                case LOGOUT: logout(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    /*
     * Retrieves a root user's menu selection.
     * 
     * @return the menu selection
     */

    private AdministratorAction getAdministratorMenuSelection() {
        System.out.println();
        
        System.out.println("[1] View faculty.");
        System.out.println("[2] View faculty by department.");
        System.out.println("[3] View student enrollment.");
        System.out.println("[4] View student enrollment by grade.");
        System.out.println("[5] View student enrollment by course.");
        System.out.println("[6] Change password.");
        System.out.println("[7] Logout.");
        System.out.print("\n::: ");

        switch (Utils.getInt(in, -1)) {
            case 1: return AdministratorAction.FACULTY;
            case 2: return AdministratorAction.DEPARTMENT;
            case 3: return AdministratorAction.STUDENTS;
            case 4: return AdministratorAction.GRADE;
            case 5: return AdministratorAction.COURSE;
            case 6: return AdministratorAction.PASSWORD;
            case 7: return AdministratorAction.LOGOUT;
            default: return AdministratorAction.INVALID;
        }
    }
    
    private void showTeacherUI() {   	
        while (activeUser != null) {
            switch (getTeacherMenuSelection()) {
                case COURSE: viewEnrollmentByCourse(); break;
                case ADD: addAssignment(); break;
                case DELETE: deleteAssignment(); break;
                case GRADE: enterGrade(); break;
                case PASSWORD: changePassword(); break;
                case LOGOUT: logout(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    /*
     * Retrieves a teacher's menu selection.
     * 
     * @return the menu selection
     */

	private TeacherAction getTeacherMenuSelection() {
        System.out.println();
        
        System.out.println("[1] View enrollment by course.");
        System.out.println("[2] Add assignment.");
        System.out.println("[3] Delete assignment.");
        System.out.println("[4] Enter grade.");
        System.out.println("[5] Change password.");
        System.out.println("[6] Logout.");
        System.out.print("\n::: ");

        switch (Utils.getInt(in, -1)) {
            case 1: return TeacherAction.COURSE;
            case 2: return TeacherAction.ADD;
            case 3: return TeacherAction.DELETE;
            case 4: return TeacherAction.GRADE;
            case 5: return TeacherAction.PASSWORD;
            case 6: return TeacherAction.LOGOUT;
            default: return TeacherAction.INVALID;
        }
    }
	
	private void viewEnrollmentByCourse() {
		String courseNumber = getCourseSelectionTeacher();
		ArrayList<Student> students = PowerSchool.getStudentsByCourse(courseNumber);
    	
    	if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + fixGPA(student));
            } 
        }
		
	}
	
	private void addAssignment() {
		int courseId = getCourseId();
		int assignmentId = getAssignmentId();
		int markingPeriod = 0;
		int isMidterm = 0;
		int isFinal = 0;
		System.out.println("\nChoose a marking period or exam status.\n");
		System.out.println("[1] MP1 assignment.");
        System.out.println("[2] MP2 assignment.");
        System.out.println("[3] MP3 assignment.");
        System.out.println("[4] MP4 assignment.");
        System.out.println("[5] Midterm exam.");
        System.out.println("[6] Final exam.");
        System.out.print("\n::: ");
        int selection = Utils.getInt(in, -1);
        while(selection <= 0 || selection > 6) {
        	if(selection <= 0 || selection > 6) {
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
            selection = Utils.getInt(in, -1);                 
        }
        
        switch(selection){
        	case 1:
        		markingPeriod = 1;
        		break;
        	case 2:
        		markingPeriod = 2;
        		break;
        	case 3:
        		markingPeriod = 3;
        		break;
        	case 4:
        		markingPeriod = 4;
        		break;
        	case 5:
        		markingPeriod = 5;
        		isMidterm = 1;
        		break;
        	case 6:
        		markingPeriod = 6;
        		isFinal = 1;
        		break;
        }
        
        System.out.print("\nAssignment Title: ");
        String title = in.nextLine(); 
        int pointValue = 0;
        do {
        System.out.print("Point Value: ");
        pointValue = Utils.getInt(in, -1);  
        if(pointValue < 1 || pointValue > 101) {
        	System.out.println("\nPoint values must be between 1 and 100.\n");
        }
        }while(pointValue < 1 || pointValue > 101);
		if(Utils.confirm(in, "\nAre you sure you want to create this assignment? (y/n) ")){
			if(PowerSchool.addAssignment(courseId, assignmentId, markingPeriod, isMidterm, isFinal, title, pointValue) == 1) {
				System.out.println("\nSuccessfully created assignment.");
			}else {
				System.out.println("\nError creating assignment.");
			}	
		}		
	}

	private int getCourseId() {
		String courseNumber = getCourseSelectionTeacher();
		return PowerSchool.getCourseId(courseNumber);
	} 

	private int getAssignmentId() {		
		if(PowerSchool.getNumberOfAssignemnts() == 0) {
			return 1;
		}else {
			return PowerSchool.getlastAssignmentId() + 1;
		}
	}
	
	

	private void deleteAssignment() {
		int courseId = getCourseId();
		System.out.println("\nChoose a marking period or exam status.\n");
		System.out.println("[1] MP1 assignment.");
        System.out.println("[2] MP2 assignment.");
        System.out.println("[3] MP3 assignment.");
        System.out.println("[4] MP4 assignment.");
        System.out.println("[5] Midterm exam.");
        System.out.println("[6] Final exam.");
        System.out.print("\n::: ");
        int markingPeriod = Utils.getInt(in, -1);
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
		 ArrayList<String> assignments = PowerSchool.getAssignments(courseId, markingPeriod);
		 ArrayList<String> pointValues = PowerSchool.getPointValues(courseId, markingPeriod);
		 
		 System.out.println();
		 if(!assignments.isEmpty()) {
			 int assignmentSelection = -1;
		        while(assignmentSelection <= 0 || assignmentSelection > assignments.size()) {
		       	 int j = 1;
		            for (String i: assignments) {
		                System.out.println("["+ j++ + "] " + i + " (" + pointValues.get(j-2) + " pts)");
		            }
		       	 System.out.print("\n::: ");
		       	assignmentSelection = Utils.getInt(in, -1);
		       	 if(assignmentSelection <= 0 || assignmentSelection > assignments.size()) {
		       		 System.out.println("\nInvalid Selection.\n");
		       	 }
		        }
		        String title = assignments.get(assignmentSelection-1);
		        int assignemntId = PowerSchool.getAssignmentId(courseId, markingPeriod, title);
		        if(Utils.confirm(in, "\nAre you sure you want to create this assignment? (y/n) ")) {
		        	if(PowerSchool.deleteAssignment(courseId, markingPeriod, title) == 1 && PowerSchool.deleteAssignmentGrades(assignemntId, courseId) ==1 ) {
		        		System.out.println("\nSuccessfully deleted " + title + ".");		        		
		        	}else {
		        		System.out.println("\nError deleting assignment.");
		        	}
		        }
		 }else {
			 System.out.println("No assignments.");
		 }
        
	}
	
    private void enterGrade() {
        int courseId = getCourseId();
        String courseNo = PowerSchool.getCourseNumber(courseId);
        System.out.println("\nChoose a marking period or exam status.\n");
		System.out.println("[1] MP1 assignment.");
        System.out.println("[2] MP2 assignment.");
        System.out.println("[3] MP3 assignment.");
        System.out.println("[4] MP4 assignment.");
        System.out.println("[5] Midterm exam.");
        System.out.println("[6] Final exam.");
        System.out.print("\n::: ");
        int markingPeriod = Utils.getInt(in, -1);
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
        
        ArrayList<String> assignments = PowerSchool.getAssignments(courseId, markingPeriod);
		ArrayList<String> pointValues = PowerSchool.getPointValues(courseId, markingPeriod);
		ArrayList<String> assignmentids = PowerSchool.getAssignmentIds(courseId, markingPeriod);
		System.out.println(); 
		int assignmentSelection = -1;
		 if(!assignments.isEmpty()) {
		        while(assignmentSelection <= 0 || assignmentSelection > assignments.size()) {
		       	 int j = 1;
		            for (String i: assignments) {
		                System.out.println("["+ j++ + "] " + i + " (" + pointValues.get(j-2) + " pts)");
		            }
		       	 System.out.print("\n::: ");
		       	assignmentSelection = Utils.getInt(in, -1);
		       	 if(assignmentSelection <= 0 || assignmentSelection > assignments.size()) {
		       		 System.out.println("\nInvalid Selection.\n");
		       	 }
		        }
		        System.out.println("");
		        ArrayList<Student> students = PowerSchool.getStudentsByCourse(courseNo);
		    	
		    	if (students.isEmpty()) {
		            System.out.println("\nNo students to display.");
		        } else {
		        	System.out.println("Choose a student.");
		            System.out.println();
		            
		            int selectedStudent = -1;
		            do {
		            	int i = 1;
		            	for (Student student : students) {	            	
			                System.out.println("[" + i++ + "] " + student.getName());
			            }		            
				    	System.out.print("\n::: ");
				    	selectedStudent = Utils.getInt(in, -1);
				    	if(selectedStudent < 1 || selectedStudent > students.size()) {
				    		System.out.println("\nInvalid Selection.\n");
				    	}
		            }while(selectedStudent < 1 || selectedStudent > students.size());
		            System.out.println();
			    	ArrayList<String> availableStudents = PowerSchool.getStudentsByCourseWithoutObject(courseNo);
			    	String selectedStudentId = availableStudents.get(selectedStudent-1);
			    	int selectedStudentIdButItsActuallyAnInteger = Integer.parseInt(selectedStudentId);
			    	String title = assignments.get(assignmentSelection-1);
			    	String points = pointValues.get(assignmentSelection-1);
			    	String assignmentId = assignmentids.get(assignmentSelection-1);
		    	
			    	String assignmentDescription = "Assignment: " + title + " (" + points + " pts)";
			    	
			    	System.out.println(assignmentDescription);
			    	
			    	ArrayList<String> studentName = PowerSchool.getStudentById(selectedStudentIdButItsActuallyAnInteger);
			    	String studentLastName = studentName.get(1);
			    	String studentFirstName = studentName.get(0);
			    	
			    	System.out.println("Student: " + studentLastName + ", " + studentFirstName);
			    	
			    	
			    	ArrayList<String> grades = PowerSchool.getAssignmentGrade(assignmentId, selectedStudentIdButItsActuallyAnInteger);			    	
			    	
			    	if (grades.isEmpty()) {
			    		System.out.println("Current Grade: --"); 
			    	} else {
			    		String assignmentGrade = grades.get(0);
			    		System.out.println("Current Grade: " + assignmentGrade + "/" + points);
			    		grades.clear();
			    	} 
			    	
			    	System.out.print("\nNew Grade: ");
			    	
			    	int newGrade = Utils.getInt(in, -1);
			    	if (newGrade > Integer.parseInt(points) || newGrade < 0) {
			    		while(newGrade > Integer.parseInt(points) || newGrade < 0) {
			    			System.out.print("Please enter a valid grade: ");
			    			newGrade = Utils.getInt(in, -1);
			    		}
			    	}
			    	
			    	if(Utils.confirm(in, "\nAre you sure you want to enter this grade? (y/n) ")){
			    		PowerSchool.deleteAssignmentGrade(Integer.parseInt(assignmentId), selectedStudentIdButItsActuallyAnInteger);
			    		if(PowerSchool.enterGrade(courseId, Integer.parseInt(assignmentId), selectedStudentIdButItsActuallyAnInteger, newGrade, Integer.parseInt(points), true) == 1) {
			    			System.out.println("\nSuccessfully entered grade.");
			      
			                   ArrayList<Integer> assignmentIds = PowerSchool.getAssignmentIdByMP(markingPeriod);
			                   ArrayList<Double> grades1 = new ArrayList<Double>();

			                    for (int i = 0; i < assignmentIds.size(); i++) {
			                        grades1.addAll(PowerSchool.getGrades(courseId, assignmentIds.get(i),selectedStudentIdButItsActuallyAnInteger));
			                    }
			                    ArrayList<Double> percent = new ArrayList<Double>();
			                    for (int i = 0; i < grades1.size(); i+=2) {
			                        percent.add((grades1.get(i)/grades1.get(i+1))*100);
			                    }
			                    double total = 0;
			                    for (int i = 0; i < percent.size(); i++) {
			                        total += percent.get(i);
			                    }
			                    double average = total/percent.size();
			                    switch (markingPeriod) {
			                    case 1: PowerSchool.updateCourseGradesMP1(courseId, selectedStudentIdButItsActuallyAnInteger, average); break;
			                    case 2: PowerSchool.updateCourseGradesMP2(courseId, selectedStudentIdButItsActuallyAnInteger, average); break;
			                    case 3: PowerSchool.updateCourseGradesMP3(courseId, selectedStudentIdButItsActuallyAnInteger, average); break;
			                    case 4: PowerSchool.updateCourseGradesMP4(courseId, selectedStudentIdButItsActuallyAnInteger, average); break;
			                    case 5: PowerSchool.updateCourseGradesMidterm(courseId, selectedStudentIdButItsActuallyAnInteger, average); break;
			                    case 6: PowerSchool.updateCourseGradesFinal(courseId, selectedStudentIdButItsActuallyAnInteger, average); break;
			                    default: System.out.println("\nInvalid selection.\n"); break;
			                    }
			                }
			                ArrayList<Double> grades1 = new ArrayList<Double>();
			                if (PowerSchool.getMP1Grade(courseId, selectedStudentIdButItsActuallyAnInteger) == null){
			                    grades1.add(-1.0);
			                } else {
			                    grades1.add((Double) PowerSchool.getMP1Grade(courseId, selectedStudentIdButItsActuallyAnInteger));
			                }
			                if (PowerSchool.getMP2Grade(courseId, selectedStudentIdButItsActuallyAnInteger) == null){
			                    grades1.add(-1.0);
			                } else {
			                    grades1.add((Double) PowerSchool.getMP2Grade(courseId, selectedStudentIdButItsActuallyAnInteger));
			                }
			                if (PowerSchool.getMP3Grade(courseId, selectedStudentIdButItsActuallyAnInteger) == null){
			                    grades1.add(-1.0);
			                } else {
			                    grades1.add((Double) PowerSchool.getMP3Grade(courseId, selectedStudentIdButItsActuallyAnInteger));
			                }
			                if (PowerSchool.getMP4Grade(courseId, selectedStudentIdButItsActuallyAnInteger) == null){
			                    grades1.add(-1.0);
			                } else {
			                    grades1.add((Double) PowerSchool.getMP4Grade(courseId, selectedStudentIdButItsActuallyAnInteger));
			                }
			                if (PowerSchool.getMidtermGrade(courseId, selectedStudentIdButItsActuallyAnInteger) == null){
			                    grades1.add(-1.0);
			                } else {
			                    grades1.add((Double) PowerSchool.getMidtermGrade(courseId, selectedStudentIdButItsActuallyAnInteger));
			                }
			                
			                if (PowerSchool.getFinalGrade(courseId, selectedStudentIdButItsActuallyAnInteger) == null){
			                    grades1.add(-1.0);
			                } else {
			                    grades1.add((Double) PowerSchool.getFinalGrade(courseId, selectedStudentIdButItsActuallyAnInteger));
			                }

			                double grade = Utils.getGrade(grades1);
			                PowerSchool.updateCourseGrade(courseId, selectedStudentIdButItsActuallyAnInteger, grade);
			                PowerSchool.getCourseGrades(selectedStudentIdButItsActuallyAnInteger);

			                ArrayList<Object> courseGrades = PowerSchool.getCourseGrades(selectedStudentIdButItsActuallyAnInteger);
			                ArrayList<Double> fourScale = new ArrayList<Double>();
			                for (int i = 0; i < courseGrades.size(); i++) {
			                    if ((Double) courseGrades.get(i) == -1.0) {

			                    } else if ((Double) courseGrades.get(i) >= 93 && (Double) courseGrades.get(i) <= 100) {
			                        fourScale.add(4.0);
			                    } else if ((Double) courseGrades.get(i) >= 90 && (Double) courseGrades.get(i) <= 92) {
			                        fourScale.add(3.7);
			                    } else if ((Double) courseGrades.get(i) >= 87 && (Double) courseGrades.get(i) <= 89) {
			                        fourScale.add(3.3);
			                    } else if ((Double) courseGrades.get(i) >= 83 && (Double) courseGrades.get(i) <= 86) {
			                        fourScale.add(3.0);
			                    } else if ((Double) courseGrades.get(i) >= 80 && (Double) courseGrades.get(i) <= 82) {
			                        fourScale.add(2.7);
			                    } else if ((Double) courseGrades.get(i) >= 77 && (Double) courseGrades.get(i) <= 79) {
			                        fourScale.add(2.3);
			                    } else if ((Double) courseGrades.get(i) >= 73 && (Double) courseGrades.get(i) <= 76) {
			                        fourScale.add(2.0);
			                    } else if ((Double) courseGrades.get(i) >= 70 && (Double) courseGrades.get(i) <= 72) {
			                        fourScale.add(1.7);
			                    } else if ((Double) courseGrades.get(i) >= 67 && (Double) courseGrades.get(i) <= 69) {
			                        fourScale.add(1.3);
			                    } else if ((Double) courseGrades.get(i) >= 65 && (Double) courseGrades.get(i) <= 66) {
			                        fourScale.add(1.0);
			                    } else if ((Double) courseGrades.get(i) > 65) {
			                        fourScale.add(0.0);
			                    }
			                }
			                ArrayList<Integer> courseIds = PowerSchool.getCourseIds(selectedStudentIdButItsActuallyAnInteger);
			                ArrayList<Integer> creditHours = PowerSchool.getCreditHours(courseIds);
			                int totalGradePoints = 0;
			                int hours = 0;
			                for (int i = 0; i < fourScale.size(); i++) {
			                    totalGradePoints += fourScale.get(i)*creditHours.get(i);
			                    hours += creditHours.get(i);
			                }
			                double gpa = (double) (totalGradePoints)/ (double) hours;
			                double roundedGpa = Math.round(gpa * 100.0) / 100.0;
			                PowerSchool.updateGPA(roundedGpa, selectedStudentIdButItsActuallyAnInteger);
			           
			    			
			    		}else {
			    			System.out.println("Error entering grade.");
			    		}
			    	}
		    	}
			    		        
		        
			    	else {
			System.out.println("No assignments.");
		}
		        }
		 
		
		
	
	 private String getCourseSelectionTeacher() {
		 Teacher teacher = PowerSchool.getTeacher(activeUser);
		 ArrayList<String> courses = PowerSchool.getCourses(teacher.getDepartmentId());
		 System.out.println();		 
		 System.out.println("Choose a course.\n");		 
         int courseSelection = -1;
         while(courseSelection <= 0 || courseSelection > courses.size()) {
        	 int j = 1;
             for (String i: courses) {
                 System.out.println("["+ j++ + "] " + i);
             }
        	 System.out.print("\n::: ");
        	 courseSelection = Utils.getInt(in, -1);
        	 if(courseSelection <= 0 || courseSelection > courses.size()) {
        		 System.out.println("\nInvalid Selection.\n");
        	 }
         }
		return courses.get(courseSelection-1);
	}

	private void showStudentUI() {
	        while (activeUser != null) {
	            switch (getStudentMenuSelection()) {
	                case GRADES: ((Student) activeUser).viewCourseGrades(); break;
	                case COURSE: ((Student) activeUser).viewAssignmentGradesByCourse(in); break;
	                case PASSWORD: changePassword(); break;
	                case LOGOUT: logout(); break;
	                default: System.out.println("\nInvalid selection."); break;
	            }
	        }
	    }
	    
	    /*
	     * Retrieves a teacher's menu selection.
	     * 
	     * @return the menu selection
	     */

		private StudentAction getStudentMenuSelection() {
	        System.out.println();
	        
	        System.out.println("[1] View course grades.");
	        System.out.println("[2] View assignment grades by course.");
	        System.out.println("[3] Change password.");
	        System.out.println("[4] Logout.");
	        System.out.print("\n::: ");

	        switch (Utils.getInt(in, -1)) {
	            case 1: return StudentAction.GRADES;
	            case 2: return StudentAction.COURSE;
	            case 3: return StudentAction.PASSWORD;
	            case 4: return StudentAction.LOGOUT;
	            default: return StudentAction.INVALID;
	        }
	    }
		
		private void viewCourseGrades() {
			// TODO Auto-generated method stub
			
		}

		private void viewAssignmentGradesByCourses() {
		// TODO Auto-generated method stub
			
		}
		
    /*
     * Resets the database to its factory settings.
     */
    
    private void factoryReset() {
    	if(Utils.confirm(in, "\nAre you sure you want to reset all settings and data? (y/n) ")){
    		 try {
    	            PowerSchool.initialize(true);
    	            System.out.println("\nSuccessfully reset database.");
    	        } catch (Exception e) {
    	            e.printStackTrace();
    	        }
    	}
    }
    
    /*
     * Shuts down the application after encountering an error.
     * 
     * @param e the error that initiated the shutdown sequence
     */
    
    /*
     * Logs out of the application.
     */

    private void logout() {
    	if(Utils.confirm(in, "\nAre you sure you want to logout? (y/n) ")) {
    		activeUser = null;
    	}
    }
    
    private void shutdown(Exception e) {
        if (in != null) {
            in.close();
        }
        
        System.out.println("\nEncountered unrecoverable error. Shutting down...\n");
        System.out.println(e.getMessage());
        
        
        System.out.println("\nGoodbye!");
        
        System.exit(0);
    }

    /*
     * Releases all resources and kills the application.
     */

    private void shutdown() {        
        System.out.println();
            
        if (Utils.confirm(in, "Are you sure? (y/n) ")) {
            if (in != null) {
                in.close();
            }
            
            System.out.println("\nGoodbye!");
            System.exit(0);
        }
    }
    /**
     * Logs in with the provided credentials.
     *
     * @param username the username for the requested account
     * @param password the password for the requested account
     * @return true if the credentials were valid; false otherwise
     */

    public boolean login(String username, String password) {
        activeUser = PowerSchool.login(username, password);

        return activeUser != null;
    }

    /**
     * Determines whether or not the user has logged in before.
     *
     * @return true if the user has never logged in; false otherwise
     */

    public boolean isFirstLogin() {
        return activeUser.getLastLogin().equals("0000-00-00 00:00:00.000");
    }
    

    /////// MAIN METHOD ///////////////////////////////////////////////////////////////////

    /*
     * Starts the PowerSchool application.
     *
     * @param args unused command line argument list
     */

    public static void main(String[] args) {
        Application app = new Application();
        app.startup();
    }
}
