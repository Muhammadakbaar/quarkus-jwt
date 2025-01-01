```toml
name = 'login'
method = 'POST'
url = ' http://localhost:8080/auth/login'
sortWeight = 2000000
id = '86d916a6-71d9-4491-b141-f5e964032493'

[auth]
type = 'BEARER'

[body]
type = 'JSON'
raw = '''
{
  "email": "desinta@gmail.com",
  "password": "123456",
}'''
```
