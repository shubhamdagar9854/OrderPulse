const ordersTableBody = document.getElementById('ordersTableBody');
const createModal = document.getElementById('createModal');

if (!localStorage.getItem('token')) {
    window.location.href = '/index.html';
}

const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
const myUserInfo = document.getElementById('myUserInfo');
if (myUserInfo && currentUser.name) {
    myUserInfo.textContent = 'Logged in as: ' + currentUser.name + ' (ID: ' + currentUser.userId + ')';
}

const userIdInput = document.getElementById('userIdInput');
if (currentUser.userId) {
    userIdInput.value = currentUser.userId;
    loadOrders();
}

document.getElementById('createOrderForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        userId: parseInt(document.getElementById('orderUserId').value),
        productId: parseInt(document.getElementById('orderProductId').value),
        quantity: parseInt(document.getElementById('orderQuantity').value)
    };
    try {
        await orderAPI.create(data);
        alert('Order placed successfully!');
        closeCreateModal();
        const userId = document.getElementById('userIdInput').value;
        if (userId) loadOrders();
    } catch (err) {
        alert('Error: ' + err.message);
    }
});

async function loadOrders() {
    const userId = document.getElementById('userIdInput').value;
    if (!userId) {
        ordersTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:#e53e3e;">Please enter a User ID</td></tr>`;
        return;
    }
    try {
        const orders = await orderAPI.getByUser(userId);
        if (orders.length === 0) {
            ordersTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No orders found for this user</td></tr>`;
            return;
        }
        ordersTableBody.innerHTML = orders.map(o => `
            <tr>
                <td>${o.id}</td>
                <td>${o.userId}</td>
                <td>${o.productId}</td>
                <td>${o.quantity}</td>
                <td>₹${parseFloat(o.totalPrice).toFixed(2)}</td>
                <td>
                    <span style="padding:0.25rem 0.5rem;border-radius:4px;font-size:0.8rem;background:${
            o.status === 'CANCELLED' ? '#fed7d7' : o.status === 'UNPAID' ? '#fefcbf' : '#c6f6d5'
        };color:${
            o.status === 'CANCELLED' ? '#9b2c2c' : o.status === 'UNPAID' ? '#975a16' : '#276749'
        }">${o.status}</span>
                </td>
                <td>
                    ${o.status === 'UNPAID' ? `<button class="btn btn-sm btn-success" onclick="payOrder(${o.id})">Pay with Razorpay</button>` : ''}
                    ${(o.status === 'UNPAID' || o.status === 'PAID') ? `<button class="btn btn-sm btn-danger" onclick="cancelOrder(${o.id})" style="margin-left:0.25rem;">Cancel</button>` : ''}
                </td>
            </tr>
        `).join('');
    } catch (err) {
        ordersTableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:#e53e3e;">Failed to load: ${err.message}</td></tr>`;
    }
}

async function cancelOrder(id) {
    if (!confirm('Cancel this order?')) return;
    try {
        await orderAPI.cancel(id);
        loadOrders();
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

async function payOrder(id) {
    await openRazorpayCheckout(id, {
        onSuccess: (verified) => {
            alert('Payment successful! Order #' + verified.orderId + ' marked PAID (Payment ID: ' + verified.paymentId + ')');
            loadOrders();
        },
        onError: (err) => {
            alert('Payment failed: ' + err.message);
        }
    });
}

async function loadProductDropdown() {
    const select = document.getElementById('orderProductId');
    const info = document.getElementById('selectedProductInfo');
    try {
        const products = await productAPI.getAll();
        if (!products || products.length === 0) {
            select.innerHTML = '<option value="">-- No products available --</option>';
            info.innerHTML = '<span style="color:#e53e3e;">Go to Products page and add products first</span>';
            return;
        }
        select.innerHTML = '<option value="">-- Select Product --</option>' +
            products.map(p => `<option value="${p.id}" data-price="${p.price}" data-stock="${p.quantity}">${p.name} - ₹${parseFloat(p.price).toFixed(2)} (Stock: ${p.quantity})</option>`).join('');
        select.onchange = function() {
            const opt = this.options[this.selectedIndex];
            if (opt.value) {
                info.innerHTML = 'Price: ₹' + parseFloat(opt.dataset.price).toFixed(2) + ' | Available: ' + opt.dataset.stock;
            } else {
                info.innerHTML = '';
            }
        };
        info.innerHTML = '';
    } catch (err) {
        select.innerHTML = '<option value="">-- Failed to load products --</option>';
        info.innerHTML = '<span style="color:#e53e3e;">Error: ' + err.message + '</span>';
    }
}

function openCreateModal() {
    document.getElementById('orderUserId').value = currentUser.userId || '';
    loadProductDropdown();
    createModal.classList.add('show');
}

function closeCreateModal() {
    createModal.classList.remove('show');
    document.getElementById('createOrderForm').reset();
    document.getElementById('selectedProductInfo').innerHTML = '';
}