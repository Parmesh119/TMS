```toml
name = 'create employee'
method = 'POST'
url = 'http://localhost:3005/api/v1/employees/create'
sortWeight = 1000000
id = 'fab4d25b-c08f-4fd0-85ee-5c93bc8ae054'

[body]
type = 'JSON'
raw = '''
{
  name: "maven1", 
  email: "trswm@e-record.com", 
  contactNumber: "4578457893", 
  role: "STAFF"
}
'''
```
