const API_BASE = '';

function getToken() {
    return localStorage.getItem('jwt_token');
}

function setToken(token) {
    localStorage.setItem('jwt_token', token);
}

function clearToken() {
    localStorage.removeItem('jwt_token');
}

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + getToken()
    };
}

async function apiRequest(url, options = {}) {
    options.headers = { ...authHeaders(), ...options.headers };
    const response = await fetch(API_BASE + url, options);
    if (response.status === 401) {
        clearToken();
        window.location.href = '/login';
        return null;
    }
    return response;
}

async function login(email, password) {
    const response = await fetch('http://localhost:8081/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
        throw new Error('Неверный email или пароль');
    }

    const data = await response.json();
    setToken(data.token);
    return data;
}

async function register(email, password, firstName, lastName) {
    const response = await fetch('http://localhost:8081/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, firstName, lastName })
    });

    if (!response.ok) {
        const err = await response.json();
        throw new Error(err.message || 'Ошибка регистрации');
    }

    const data = await response.json();
    setToken(data.token);
    return data;
}

function logout() {
    clearToken();
    window.location.href = '/login';
}

function formatMoney(amount) {
    return new Intl.NumberFormat('ru-RU', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(amount);
}
