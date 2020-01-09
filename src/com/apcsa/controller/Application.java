package com.apcsa.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import com.apcsa.data.PowerSchool;
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
    	ArrayList<Student> students = PowerSchool.getStudentsByGrade(getGradeSelection());
    	
    	if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + student.getClassRank());
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
	
    private void enterGrade() {
		
	}

	private void deleteAssignment() {
		// TODO Auto-generated method stub
		
	}

	private void addAssignment() {
		// TODO Auto-generated method stub
		
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
	
	 private String getCourseSelectionTeacher() {
		 Teacher teacher = PowerSchool.getTeacher(activeUser);
		 ArrayList<String> courses = PowerSchool.getCourses(teacher.getDepartmentId());
		 System.out.println();		 
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
	                case GRADES: viewCourseGrades(); break;
	                case COURSE: viewAssignmentGradesByCourses(); break;
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
    	if(Utils.confirm(in, "\nAre you sure you want to reset all settings and data? (y/n)")){
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
    	if(Utils.confirm(in, "\nAre you sure you want to logout? (y/n)")) {
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