import json
import sys

filename = 'TestMePlease.json'
if len(sys.argv) > 1:
    filename = sys.argv[1]

# read text from file named 'foo.json' into json_data variable
with open(filename) as f:
    json_data = f.read()

def b(s):
    return f'<b>{s}</b>'

def q(s):
    return f'<td>{s}</td>'

# Parse the JSON data
data = json.loads(json_data)

print("<html><head><title>Test Cases</title></head><body><table border='1'>")
print("<tr><th>Method Name</th><th>Method Header</th><th>Answer</th></tr>")

for method in data['methods']:
    print("<tr>")
    print(f"{q(b(method['name']))}")
    print(f"{q(b(method['header']))}")
    print(f"{q(b(method.get('answer', '')))}")
    print("</tr>")
    
    for test in method['tests']:
        print("<tr>")
        print(f"{q(test['actualParameters'])}")
        print(f"{q(test['result'])}")
        print("<td></td>")
        print("</tr>")
    # html row that spans 3 columns
    print(f"<tr><td colspan='3'>&nbsp;</td></tr>")
    
print("</table></body></html>") 
