if (!localStorage.getItem('token')) {
    window.location.href = '/index.html';
}

const userTableBody = document.getElementById('userTableBody');
const addModal = document.getElementById('addModal');
const loginModal = document.getElementById('loginModal');
const passwordModal = document.getElementById('passwordModal');
let passwordTargetId = null;

document.getElementById('addUserForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        name: document.getElementById('addName').value,
        email: document.getElementById('addEmail').value,
        password: document.getElementById('addPassword').value
    };
    try {
        await userAPI.register(data);
        const loginRes = await userAPI.login({ email: data.email, password: data.password });
        localStorage.setItem('token', loginRes.token);
        localStorage.setItem('user', JSON.stringify({ userId: loginRes.userId, name: loginRes.name, role: loginRes.role }));
        alert(`Registered & logged in as ${loginRes.name}!`);
        closeAddModal();
        loadUsers();
    } catch (err) {
        alert('Error: ' + err.message);
    }
});

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        email: document.getElementById('loginEmail').value,
        password: document.getElementById('loginPassword').value
    };
    try {
        const res = await userAPI.login(data);
        localStorage.setItem('token', res.token);
        localStorage.setItem('user', JSON.stringify({ userId: res.userId, name: res.name, role: res.role }));
        document.getElementById('loginResult').innerHTML =
            `<div style="padding: 0.75rem; background: #c6f6d5; color: #276749; border-radius: 6px;">
                Logged in as ${res.name} (${res.role})
            </div>`;
        setTimeout(closeLoginModal, 1500);
    } catch (err) {
        document.getElementById('loginResult').innerHTML =
            `<div style="padding: 0.75rem; background: #fed7d7; color: #9b2c2c; border-radius: 6px;">
                Login failed: ${err.message}
            </div>`;
    }
});

async function loadUsers() {
    try {
        const users = await userAPI.getAll();
        const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
        userTableBody.innerHTML = users.map(u => {
            const isCurrentUser = currentUser.userId === u.id;
            const loginOrLogout = isCurrentUser
                ? `<button class="btn btn-sm btn-danger" onclick="logout()">Logout</button>`
                : `<button class="btn btn-sm btn-primary" onclick="showLogin(${u.id})">Login</button>`;
            const roleAction = (currentUser.role === 'ADMIN' && u.role !== 'ADMIN')
                ? `<button class="btn btn-sm btn-success" onclick="makeAdmin(${u.id}, '${u.name}')">Make Admin</button>`
                : '';
            const pwdAction = (currentUser.userId === u.id || currentUser.role === 'ADMIN')
                ? `<button class="btn btn-sm btn-warning" onclick="openPasswordModal(${u.id}, '${u.name}')">Change Password</button>`
                : '';
            return `
            <tr>
                <td>${u.id}</td>
                <td>${u.name}${isCurrentUser ? ' (You)' : ''}</td>
                <td>${u.email}</td>
                <td>${u.role}</td>
                <td>${u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-'}</td>
                <td style="display:flex;gap:0.25rem;flex-wrap:wrap;">${pwdAction} ${roleAction} ${loginOrLogout}</td>
            </tr>`;
        }).join('');
    } catch (err) {
        userTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:#e53e3e;">Failed to load: ${err.message}</td></tr>`;
    }
}

async function makeAdmin(id, name) {
    if (!confirm(`Promote "${name}" to ADMIN?`)) return;
    try {
        await userAPI.updateRole(id, { role: 'ADMIN' });
        alert(`${name} is now an ADMIN! They need to login again.`);
        loadUsers();
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    loadUsers();
}

function openAddModal() {
    addModal.classList.add('show');
}

function closeAddModal() {
    addModal.classList.remove('show');
    document.getElementById('addUserForm').reset();
}

function showLogin(id) {
    document.getElementById('loginEmail').value = '';
    document.getElementById('loginPassword').value = '';
    document.getElementById('loginResult').innerHTML = '';
    loginModal.classList.add('show');
}

function closeLoginModal() {
    loginModal.classList.remove('show');
    document.getElementById('loginForm').reset();
    document.getElementById('loginResult').innerHTML = '';
}

document.getElementById('passwordForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const current = JSON.parse(localStorage.getItem('user') || '{}');
    const isAdmin = current.role === 'ADMIN' && current.userId !== passwordTargetId;
    const resultDiv = document.getElementById('passwordResult');
    const data = {
        currentPassword: document.getElementById('pwdCurrent').value,
        newPassword: document.getElementById('pwdNew').value
    };
    try {
        await userAPI.changePassword(passwordTargetId, data);
        resultDiv.innerHTML = `<div style="padding:0.75rem;background:#c6f6d5;color:#276749;border-radius:6px;">Password updated successfully!</div>`;
        setTimeout(closePasswordModal, 1500);
        if (!isAdmin) {
            setTimeout(() => {
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                window.location.href = '/index.html';
            }, 1800);
        }
    } catch (err) {
        resultDiv.innerHTML = `<div style="padding:0.75rem;background:#fed7d7;color:#9b2c2c;border-radius:6px;">Failed: ${err.message}${err.message.includes('incorrect') ? ' (must enter your own current password)' : ''}</div>`;
    }
});

function openPasswordModal(id, name) {
    passwordTargetId = id;
    const isAdmin = JSON.parse(localStorage.getItem('user') || '{}').role === 'ADMIN';
    const self = JSON.parse(localStorage.getItem('user') || '{}').userId === id;
    document.getElementById('pwdCurrent').value = '';
    document.getElementById('pwdNew').value = '';
    document.getElementById('passwordResult').innerHTML = '';
    if (!self && isAdmin) {
        document.getElementById('pwdCurrent').disabled = true;
        document.getElementById('pwdCurrent').placeholder = 'Admin reset - not required';
    } else {
        document.getElementById('pwdCurrent').disabled = false;
        document.getElementById('pwdCurrent').placeholder = '';
    }
    document.getElementById('passwordModalUser').textContent = '- ' + name + ' (#' + id + ')';
    passwordModal.classList.add('show');
}

function closePasswordModal() {
    passwordModal.classList.remove('show');
    document.getElementById('passwordForm').reset();
}.

loadUsers();