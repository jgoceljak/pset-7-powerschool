package com.apcsa.data;

public class QueryUtils {

    /////// QUERY CONSTANTS ///////////////////////////////////////////////////////////////
    
    /*
     * Determines if the default tables were correctly loaded.
     */
	
    public static final String SETUP_SQL =
        "SELECT COUNT(name) AS names FROM sqlite_master " +
            "WHERE type = 'table' " +
        "AND name NOT LIKE 'sqlite_%'";
    
    /*
     * Updates the last login timestamp each time a user logs into the system.
     */

    public static final String LOGIN_SQL =
        "SELECT * FROM users " +
            "WHERE username = ?" +
        "AND auth = ?";
    
    /*
     * Updates the last login timestamp each time a user logs into the system.
     */

    public static final String UPDATE_LAST_LOGIN_SQL =
        "UPDATE users " +
            "SET last_login = ? " +
        "WHERE username = ?";
    
    public static final String UPDATE_AUTH_SQL =
            "UPDATE users " +
                "SET auth = ? " +
            "WHERE username = ?";
    
    /*
     * Retrieves an administrator associated with a user account.
     */

    public static final String GET_ADMIN_SQL =
        "SELECT * FROM administrators " +
            "WHERE user_id = ?";
    
    /*
     * Retrieves a teacher associated with a user account.
     */

    public static final String GET_TEACHER_SQL =
        "SELECT * FROM teachers " +
            "WHERE user_id = ?";
    
    /*
     * Retrieves a student associated with a user account.
     */

    public static final String GET_STUDENT_SQL =
        "SELECT * FROM students " +
            "WHERE user_id = ?";
    
    public static final String GET_COURSE_NUMBER =
            "SELECT * FROM courses " +
                "WHERE course_id = ?";
    
    public static final String GET_COURSE_Id =
            "SELECT * FROM courses " +
                "WHERE course_no = ?";
    
    public static final String GET_NUMBER_OF_COURSES =
            "SELECT COUNT(*) FROM courses";
    
    /*
     * Retrieves all teachers.
     */

    public static final String GET_ALL_TEACHERS_SQL =
        "SELECT * FROM " +
            "teachers, departments " +
        "WHERE " +
            "teachers.department_id = departments.department_id " +
        "ORDER BY " +
            "last_name, first_name";

//needs an inner join to work
	public static final String GET_TEACHERS_BY_DEPARTMENT_SQL =
		"SELECT * FROM " +
		    "teachers, departments " +
		"WHERE " +
		    "teachers.department_id = departments.department_id  AND departments.department_id = ?" +
		"ORDER BY " +
		    "last_name, first_name";
	
	public static final String GET_STUDENTS =
	        "SELECT * FROM " +
	            "students " +
	        "ORDER BY " +
	            "last_name, first_name";
	
	public static final String GET_STUDENTS_BY_GRADE =
	        "SELECT * FROM " +
	            "students " +
	        "WHERE "+
	            "grade_level = ?" +
	        "ORDER BY " +
	            "last_name, first_name";
	
	 public static final String GET_STUDENT_COURSES_SQL =
		        "SELECT courses.title, grade, courses.course_id, courses.course_no FROM course_grades " +
		        "INNER JOIN courses ON course_grades.course_id = courses.course_id " +
		        "INNER JOIN students ON students.student_id = course_grades.student_id " +
		        "WHERE students.student_id = ?";
	 
	 public static final String GET_COURSE_NUMBERS_FOR_STUDENT =
		        "SELECT courses.title, grade, courses.course_id, courses.course_no FROM course_grades " +
		        "INNER JOIN courses ON course_grades.course_id = courses.course_id " +
		        "INNER JOIN students ON students.student_id = course_grades.student_id " +
		        "WHERE students.student_id = ?";
	
	public static final String GET_STUDENTS_BY_COURSE =
	        "SELECT * FROM " +
	            "students, courses, course_grades " +
	        "WHERE "+
	            "courses.course_no = ? AND courses.course_id = course_grades.course_id AND course_grades.student_id = students.student_id " +
	        "ORDER BY " +
	            "last_name, first_name";
	
	public static final String GET_STUDENT_ID_BY_COURSE =
			"SELECT * FROM " +
		            "students, courses, course_grades " +
		        "WHERE "+
		            "courses.course_no = ? AND courses.course_id = course_grades.course_id AND course_grades.student_id = students.student_id ";
	
	public static final String GET_COURSES =
            "SELECT * FROM courses, teachers " +
             "WHERE teachers.department_id =? AND teachers.department_id = courses.department_id "+
             "ORDER BY courses.course_id";
	
	public static final String ADD_ASSIGNMENT = 
    		"INSERT INTO assignments " +
    		"VALUES(?, ?, ?, ?, ?, ?, ?)";
	
	public static final String ENTER_GRADE = 
    		"INSERT INTO assignment_grades " +
    		"VALUES(?, ?, ?, ?, ?, ?)";
	
	public static final String GET_NUMBER_OF_ASSIGNMENTS = 
			"SELECT COUNT(*) FROM assignments";
	
	public static final String GET_LAST_ASSIGNMENT_ID = 
			"SELECT * FROM "+
					"assignments "+
				"ORDER BY assignment_id DESC";
	
	 public static final String DELETE_ASSIGNMENT = 
	    		"DELETE FROM assignments " +
	    				"WHERE course_id = ?" +
	    				"AND marking_period = ?" +
	    				"AND title = ?";

	 public static final String DELETE_ASSIGNMENT_GRADE = 
	    		"DELETE FROM assignment_grades " +
	    				"WHERE assignment_id = ?" +
	    				"AND student_id = ?";
	 
	 public static final String DELETE_ASSIGNMENT_GRADES = 
	    		"DELETE FROM assignment_grades " +
	    				"WHERE assignment_id = ? " +
	    				"AND course_id = ?";
	 
	public static final String GET_ASSIGNMENTS =
			"SELECT * FROM assignments " +
			"WHERE course_id = ? AND marking_period = ?";
	
	public static final String GET_ASSIGNMENTS_GRADE =
			"SELECT * FROM assignment_grades " +
			"WHERE assignment_id = ? AND student_id = ?";
	
	public static final String GET_POINT_VALUES =
			"SELECT * FROM assignments " +
			"WHERE course_id = ? AND marking_period = ?";

	public static final String GET_STUDENT_BY_ID =
			"SELECT * FROM students " +
					"WHERE student_id = ?" + 
			"ORDER BY " +
				"last_name, first_name";
	
	public static final String UPDATE_COURSE_MP1 =
	        "UPDATE course_grades " +
	        "SET mp1 = ? " +
	        "WHERE course_id = ? " +
	        "AND student_id = ?";

	    public static final String UPDATE_COURSE_MP2 =
	        "UPDATE course_grades " +
	        "SET mp2 = ? " +
	        "WHERE course_id = ?" +
	        "AND student_id = ?";

	    public static final String UPDATE_COURSE_MP3 =
	        "UPDATE course_grades " +
	        "SET mp3 = ? " +
	        "WHERE course_id = ?" +
	        "AND student_id = ?";

	    public static final String UPDATE_COURSE_MP4 =
	        "UPDATE course_grades " +
	        "SET mp4 = ? " +
	        "WHERE course_id = ?" +
	        "AND student_id = ?";

	    public static final String UPDATE_COURSE_MIDTERM =
	        "UPDATE course_grades " +
	        "SET midterm_exam = ? " +
	        "WHERE course_id = ?" +
	        "AND student_id = ?";

	    public static final String UPDATE_COURSE_FINAL =
	        "UPDATE course_grades " +
	        "SET final_exam = ? " +
	        "WHERE course_id = ?" +
	        "AND student_id = ?";
	    
	    public static final String GET_MP1_GRADE =
            "SELECT mp1 FROM course_grades " +
            "WHERE course_id = ? " +
            "AND student_id = ?";

	    public static final String GET_MP2_GRADE =
            "SELECT mp2 FROM course_grades " +
            "WHERE course_id = ? " +
            "AND student_id = ?";

       public static final String GET_MP3_GRADE =
            "SELECT mp3 FROM course_grades " +
            "WHERE course_id = ? " +
            "AND student_id = ?";

        public static final String GET_MP4_GRADE =
            "SELECT mp4 FROM course_grades " +
            "WHERE course_id = ? " +
            "AND student_id = ?";

        public static final String GET_MIDTERM_GRADE =
            "SELECT midterm_exam FROM course_grades " +
            "WHERE course_id = ? " +
            "AND student_id = ?";

        public static final String GET_FINAL_GRADE =
            "SELECT final_exam FROM course_grades " +
            "WHERE course_id = ? " +
            "AND student_id = ?"; 
	    
	    public static final String GET_COURSE_ID_BY_STUDENT =
            "SELECT course_id FROM course_grades " +
            "WHERE student_id = ?";
	    
	    public static final String GET_CREDIT_HOURS =
            "SELECT credit_hours FROM courses " +
            "WHERE course_id = ?";
	    
	    public static final String GET_ASSIGNMENT_BY_MP =
	            "SELECT assignment_id FROM assignments " +
	            "WHERE marking_period = ?";
	    
	    public static final String GET_ALL_GRADES_FOR_STUDENT =
	            "SELECT grade FROM course_grades " +
	            "WHERE student_id = ?";
	    
	    public static final String GET_GRADES =
	            "SELECT points_earned, points_possible FROM assignment_grades " +
	            "WHERE course_id = ? " +
	            "AND assignment_id = ?" +
	            "AND student_id = ? ";
	    
	    public static final String UPDATE_COURSE_GRADE =
	            "UPDATE course_grades " +
	            "SET grade = ? " +
	            "WHERE course_id = ?" +
	            "AND student_id = ?";
	    
	    public static final String UPDATE_GPA =
	            "UPDATE students " +
	            "SET gpa = ?" +
	            "WHERE student_id = ?";
	    
		 public static final String GET_ASSIGNMENT_ID = 
				 "SELECT * FROM assignments " +
							"WHERE course_id = ? AND marking_period = ? AND title = ?";

}