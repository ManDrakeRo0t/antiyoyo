function checkAuthStatus() {
    const userData = localStorage.getItem('userData');
    if (userData) {
        try {
            currentUser = JSON.parse(userData);
        } catch (e) {
            console.error('Error parsing user data:', e);
            localStorage.removeItem('userData');
            moveToLogin();
        }
    } else {
        moveToLogin();
    }
}

function moveToLogin() {
 
    window.location.href = "login.html"
    
}

checkAuthStatus();