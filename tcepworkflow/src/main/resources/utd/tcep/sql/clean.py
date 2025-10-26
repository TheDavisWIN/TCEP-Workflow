import pandas as pd

df = pd.read_csv("UTD Transfer Courses.csv")
key_cols = ["UTD Course Number", "UTD Course Name", "School", "School Course Number", "School Course Name"]

df["placeholder"] = df["Course Number"].str.contains("-")
clean_df = df.sort_values("placeholder").drop_duplicates(subset=key_cols, keep="first")
clean_df = clean_df.drop(columns=["placeholder"])
clean_df = clean_df.sort_values(by=["Course Prefix", "Course Number", "UTD Course Number", "UTD Course Name", "School", "School Course Number", "School Course Name"])

clean_df.to_csv("UTD Transfer Courses (cleaned).csv", index=False)
print(f"Cleaned file saved with {len(clean_df)} unique rows to 'UTD Transfer Courses (cleaned).csv'")
