import pandas as pd

df = pd.read_csv("UTD Transfer Courses (cleaned).csv")

# List of equivalent UTD Courses for 'equivalent_course' table

utd_courses = df[["Course Prefix", "Course Number", "UTD Course Number", "UTD Course Name"]]
utd_courses = utd_courses.sort_values(by=["UTD Course Number"])
utd_courses = utd_courses.drop_duplicates(subset=["UTD Course Name"])

utd_courses.to_csv("utd_courses.csv", index=False)
print(f"Saved {len(utd_courses)} unique courses to 'utd_courses.csv'")

# List of incoming courses for 'incoming_course' table

incoming_courses = df[["School", "School Course Number", "School Course Name", "UTD Course Number"]]
incoming_courses = incoming_courses.sort_values(key=lambda x: x.str.lower(), by=["UTD Course Number", "School", "School Course Number", "School Course Name"])

incoming_courses.to_csv("incoming_courses.csv", index=False)
print(f"Saved {len(incoming_courses)} unique courses to 'incoming_courses.csv'")

# List of schools for 'institution' table

schools = df["School"]
schools = schools.sort_values(key=lambda x: x.str.lower())
schools = schools.drop_duplicates()

schools.to_csv("schools.csv", index=False)
print(f"Saved {len(schools)} unique schools to 'schools.csv'")
