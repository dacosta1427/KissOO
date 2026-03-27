// Test login using the same format as Server.call
const url = 'http://localhost:8080/rest';

async function testLogin(username, password) {
  const payload = {
    _uuid: '',
    _method: 'Login',
    _class: '',
    username: username,
    password: password
  };
  
  console.log('Testing login for:', username);
  console.log('Payload:', JSON.stringify(payload));
  
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });
    
    const result = await response.json();
    console.log('Response:', JSON.stringify(result, null, 2));
    
    if (result._Success && result.uuid) {
      console.log('✓ Login successful! UUID:', result.uuid);
    } else {
      console.log('✗ Login failed:', result._ErrorMessage);
    }
  } catch (error) {
    console.error('Request error:', error);
  }
}

// Test both credentials
testLogin('admin', 'admin').then(() => {
  console.log('\n---\n');
  return testLogin('a@b.c', 'asd');
});