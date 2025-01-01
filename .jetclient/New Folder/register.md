```toml
name = 'register'
method = 'POST'
url = ' http://localhost:8080/auth/register'
sortWeight = 1000000
id = '64655709-8861-49a3-b3be-2831a4b68afe'

[body]
type = 'JSON'
raw = '''
{
  "name": "akbar",
  "email": "akbar@gmail.com",
  "password": "123456"
}'''
```
