const productTableBody = document.getElementById('productTableBody');
const addModal = document.getElementById('addModal');
const editModal = document.getElementById('editModal');

if (!localStorage.getItem('token')) {
    window.location.href = '/index.html';
}

const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
const roleDisplay = document.getElementById('userRoleDisplay');
if (roleDisplay && currentUser.role) {
    roleDisplay.textContent = 'Your role: ' + currentUser.role;
}
const isAdmin = currentUser.role === 'ADMIN';
if (!isAdmin) {
    const addBtn = document.getElementById('addProductBtn');
    if (addBtn) addBtn.style.display = 'none';
    if (roleDisplay) roleDisplay.textContent += ' (VIEW only - ADMIN required to add/edit)';
}

document.getElementById('addProductForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        name: document.getElementById('addName').value,
        description: document.getElementById('addDescription').value,
        price: parseFloat(document.getElementById('addPrice').value),
        quantity: parseInt(document.getElementById('addQuantity').value)
    };
    try {
        await productAPI.create(data);
        alert('Product created successfully!');
        closeAddModal();
        loadProducts();
    } catch (err) {
        alert('Error: ' + err.message);
    }
});

document.getElementById('editProductForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('editId').value;
    const data = {
        name: document.getElementById('editName').value,
        description: document.getElementById('editDescription').value,
        price: parseFloat(document.getElementById('editPrice').value),
        quantity: parseInt(document.getElementById('editQuantity').value)
    };
    try {
        await productAPI.update(id, data);
        alert('Product updated successfully!');
        closeEditModal();
        loadProducts();
    } catch (err) {
        alert('Error: ' + err.message);
    }
});

async function loadProducts() {
    try {
        const products = await productAPI.getAll();
        productTableBody.innerHTML = products.map(p => `
            <tr>
                <td>${p.id}</td>
                <td>${p.name}</td>
                <td>${p.description || '-'}</td>
                <td>₹${parseFloat(p.price).toFixed(2)}</td>
                <td>${p.quantity}</td>
                <td>
                    ${isAdmin ? `<button class="btn btn-sm btn-warning btn-edit" onclick="openEditModal(${p.id}, '${p.name.replace(/'/g, "\\'")}', '${(p.description || '').replace(/'/g, "\\'")}', ${p.price}, ${p.quantity})">Edit</button>` : '<span style="color:#999;font-size:0.85rem;">View only</span>'}
                </td>
            </tr>
        `).join('');
    } catch (err) {
        productTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:#e53e3e;">Failed to load: ${err.message}</td></tr>`;
    }
}

function openAddModal() {
    addModal.classList.add('show');
}

function closeAddModal() {
    addModal.classList.remove('show');
    document.getElementById('addProductForm').reset();
}

function openEditModal(id, name, description, price, quantity) {
    document.getElementById('editId').value = id;
    document.getElementById('editName').value = name;
    document.getElementById('editDescription').value = description;
    document.getElementById('editPrice').value = price;
    document.getElementById('editQuantity').value = quantity;
    editModal.classList.add('show');
}

function closeEditModal() {
    editModal.classList.remove('show');
    document.getElementById('editProductForm').reset();
}

loadProducts();