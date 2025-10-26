import requests
from bs4 import BeautifulSoup
import pandas as pd
import time
import os

# Get all course prefixes
page_url = "https://transfercredit.utdallas.edu/search-by-utd-course/"
resp = requests.get(page_url)
soup = BeautifulSoup(resp.text, "html.parser")

dropdown = soup.find("select", id="coursePrefix")
prefixes = [opt["value"].strip() for opt in dropdown.find_all("option")[1:] if opt.get("value").strip() != ""]
print(f"Found {len(prefixes)} prefixes.")

# API URL and headers
api_url = "https://apps.utdallas.edu/transfercredit/ajax-search.php"
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                  "AppleWebKit/537.36 (KHTML, like Gecko) "
                  "Chrome/140.0.0.0 Safari/537.36",
    "Accept": "*/*",
    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
    "Origin": "https://transfercredit.utdallas.edu",
    "Referer": "https://transfercredit.utdallas.edu/"
}

# Loop through prefixes and parse tables
courses = []

for prefix in prefixes:
    payload = {"searchTerm": prefix, "option": "courseNumber"}
    try:
        resp = requests.post(api_url, data=payload, headers=headers)
        resp.raise_for_status()
        soup_courses = BeautifulSoup(resp.text, "html.parser")
        
        select = soup_courses.find_all("option")
        if not select:
            print(f"No course numbers found for prefix {prefix}")
            continue

        course_numbers = [opt.get_text(strip=True) for opt in select[1:]]
        print(f"Found course numbers for prefix {prefix}: {course_numbers}")
        
        for number in course_numbers:
            course = f"{prefix} {number}"
            payload_course = {"searchTerm": course, "option": "course"}
            resp_table = requests.post(api_url, data=payload_course, headers=headers)
            soup_table = BeautifulSoup(resp_table.text, "html.parser")
            
            table = soup_table.find("table", class_=["resultsTable", "w-100"])
            if not table:
                print(f"No table found for {course}")
                continue
            
            rows = table.find_all("tr")[1:]
            for row in rows:
                cells = row.find_all("td")
                if len(cells) < 3:
                    continue
                utd_cnum = cells[0].get_text(" ", strip=True).split("(")[0].strip()
                utd_cname = cells[0].find("small").get_text(strip=True) if cells[0].find("small") else ""
                school = cells[1].get_text(strip=True)
                school_cnum = cells[2].get_text(" ", strip=True).split("(")[0].strip()
                school_cname = cells[2].find("small").get_text(strip=True) if cells[2].find("small") else ""

                courses.append({
                    "Course Prefix": prefix,
                    "Course Number": number,
                    "Course": course,
                    "UTD Course Number": utd_cnum,
                    "UTD Course Name": utd_cname,
                    "School": school,
                    "School Course Number": school_cnum,
                    "School Course Name": school_cname
                })

            time.sleep(0.1)

    except requests.exceptions.RequestException as e:
        print(f"Request failed for prefix {prefix}: {e}")

    time.sleep(0.2)

# Save to csv
df = pd.DataFrame(courses)
df = df.sort_values(by=["Course Prefix", "Course Number", "UTD Course Number", "UTD Course Name", "School", "School Course Number", "School Course Name"])
df.to_csv("UTD Transfer Courses.csv", index=False)
print(f"Saved {len(df)} courses to 'UTD Transfer Courses.csv'")
