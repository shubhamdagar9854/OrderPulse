const DEV = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
const API_BASE = DEV ? 'http://localhost:8080' : '';

function getToken() {
    return localStorage.getItem('token');
}

async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    const config = { headers, ...options };
    if (config.body && typeof config.body === 'object') {
        config.body = JSON.stringify(config.body);
    }
    const res = await fetch(url, config);
    if (res.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        throw new Error('Unauthorized - Please login');
    }
    if (!res.ok) {
        const err = await res.text();
        throw new Error(err || `HTTP ${res.status}`);
    }
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

const userAPI = {
    getAll: () => apiRequest('/api/users'),
    getById: (id) => apiRequest(`/api/users/${id}`),
    register: (data) => apiRequest('/api/users/register', { method: 'POST', body: data }),
    login: (data) => apiRequest('/api/users/login', { method: 'POST', body: data }),
    updateRole: (id, data) => apiRequest(`/api/users/${id}/role`, { method: 'PUT', body: data }),
};

const productAPI = {
    getAll: () => apiRequest('/api/products'),
    getById: (id) => apiRequest(`/api/products/${id}`),
    create: (data) => apiRequest('/api/products', { method: 'POST', body: data }),
    update: (id, data) => apiRequest(`/api/products/${id}`, { method: 'PUT', body: data }),
    reduceStock: (id, quantity) => apiRequest(`/api/products/${id}/reduce`, { method: 'PUT', body: { quantity } }),
};

const orderAPI = {
    getAll: () => apiRequest('/api/orders'),
    create: (data) => apiRequest('/api/orders', { method: 'POST', body: data }),
    getByUser: (userId) => apiRequest(`/api/orders/user/${userId}`),
    cancel: (id) => apiRequest(`/api/orders/${id}/cancel`, { method: 'PUT' }),
    updateStatus: (id, status) => apiRequest(`/api/orders/${id}/status`, { method: 'PUT', body: { status } }),
};

const paymentAPI = {
    getAll: () => apiRequest('/api/payments'),
    razorpayOrder: (data) => apiRequest('/api/payments/razorpay/order', { method: 'POST', body: data }),
    verify: (data) => apiRequest('/api/payments/verify', { method: 'POST', body: data }),
    refund: (data) => apiRequest('/api/payments/refund', { method: 'POST', body: data }),
};