const ordersTableBody = document.getElementById('ordersTableBody');
const createModal = document.getElementById('createModal');
const trackModal = document.getElementById('trackModal');

if (!localStorage.getItem('token')) {
    window.location.href = '/index.html';
}

const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
const isAdmin = currentUser.role === 'ADMIN';
const myUserInfo = document.getElementById('myUserInfo');
if (myUserInfo && currentUser.name) {
    myUserInfo.textContent = 'Logged in as: ' + currentUser.name + ' (ID: ' + currentUser.userId + ')';
}

const STATUS_COLORS = {
    'UNPAID': { bg: '#fefcbf', fg: '#975a16' },
    'PAID': { bg: '#bee3f8', fg: '#2c5282' },
    'CONFIRMED': { bg: '#c6f6d5', fg: '#276749' },
    'PROCESSING': { bg: '#b2f5ea', fg: '#234e52' },
    'SHIPPED': { bg: '#e9d8fd', fg: '#553c9a' },
    'DELIVERED': { bg: '#68d391', fg: '#22543d' },
    'CANCELLED': { bg: '#fed7d7', fg: '#9b2c2c' }
};

const NEXT_STATUS = {
    'UNPAID': ['PAID'],
    'PAID': ['CONFIRMED'],
    'CONFIRMED': ['PROCESSING'],
    'PROCESSING': ['SHIPPED'],
    'SHIPPED': ['DELIVERED'],
    'DELIVERED': [],
    'CANCELLED': []
};

function badge(status) {
    const c = STATUS_COLORS[status] || { bg: '#e2e8f0', fg: '#2d3748' };
    return `<span style="padding:0.25rem 0.5rem;border-radius:4px;font-size:0.8rem;background:${c.bg};color:${c.fg}">${status}</span>`;
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
        ordersTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:#e53e3e;">Please enter a User ID</td></tr>`;
        return;
    }
    try {
        const orders = await orderAPI.getByUser(userId);
        if (orders.length === 0) {
            ordersTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center;">No orders found for this user</td></tr>`;
            return;
        }
        ordersTableBody.innerHTML = orders.map(o => `
            <tr>
                <td>${o.id}</td>
                <td>${o.userId}</td>
                <td>${o.productId}</td>
                <td>${o.quantity}</td>
                <td>₹${parseFloat(o.totalPrice).toFixed(2)}</td>
                <td>${badge(o.status)}</td>
                <td>
                    <button class="btn btn-sm btn-outline" onclick="showTrack(${o.id})" style="margin-right:0.25rem;">Track</button>
                    ${o.status === 'UNPAID' ? `<button class="btn btn-sm btn-success" onclick="payOrder(${o.id})">Pay</button>` : ''}
                    ${(o.status === 'UNPAID' || o.status === 'PAID' || o.status === 'CONFIRMED' || o.status === 'PROCESSING') ? `<button class="btn btn-sm btn-danger" onclick="cancelOrder(${o.id})" style="margin-left:0.25rem;">Cancel</button>` : ''}
                    ${isAdmin && (NEXT_STATUS[o.status] || []).length > 0 ? `
                        <select id="statusSel-${o.id}" style="margin-left:0.25rem;padding:0.25rem;border:1px solid #ccc;border-radius:4px;font-size:0.8rem;">
                            ${(NEXT_STATUS[o.status] || []).map(s => `<option value="${s}">${s}</option>`).join('')}
                        </select>
                        <button class="btn btn-sm btn-primary" onclick="advanceStatus(${o.id})">Update</button>
                    ` : ''}
                </td>
            </tr>
        `).join('');
    } catch (err) {
        ordersTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:#e53e3e;">Failed to load: ${err.message}</td></tr>`;
    }
}

async function advanceStatus(id) {
    const select = document.getElementById('statusSel-' + id);
    if (!select || !select.value) return;
    try {
        const updated = await orderAPI.updateStatus(id, select.value);
        alert('Order #' + id + ' moved to ' + updated.status);
        loadOrders();
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

async function showTrack(id) {
    try {
        const o = await orderAPI.getById(id);
        const timeline = o.timeline || [];
        const steps = ['UNPAID', 'PAID', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];
        const cancelled = o.status === 'CANCELLED';
        const currentIdx = cancelled ? -1 : steps.indexOf(o.status);
        const trackList = document.getElementById('trackList');
        let html = '';
        steps.forEach((s, i) => {
            const done = !cancelled && i <= currentIdx;
            const entry = timeline.find(t => t.status === s);
            const time = entry && entry.timestamp ? new Date(entry.timestamp).toLocaleString() : '';
            const labels = { UNPAID: 'Pending Payment', PAID: 'Payment Received', CONFIRMED: 'Order Confirmed', PROCESSING: 'Processing', SHIPPED: 'Shipped', DELIVERED: 'Delivered' };
            html += `
                <div class="track-step ${done ? 'done' : ''}">
                    <div class="track-dot ${done ? 'done' : ''}">
                        ${done ? '&#10003;' : '&bull;'}
                    </div>
                    <div class="track-content">
                        <strong>${labels[s]}</strong>
                        <span class="track-time">${time ? time : 'Pending'}</span>
                    </div>
                </div>`;
        });
        if (cancelled) {
            const entry = timeline.find(t => t.status === 'CANCELLED');
            html += `
                <div class="track-step done">
                    <div class="track-dot done">&#10003;</div>
                    <div class="track-content">
                        <strong>Cancelled</strong>
                        <span class="track-time">${entry && entry.timestamp ? new Date(entry.timestamp).toLocaleString() : ''}</span>
                    </div>
                </div>`;
        }
        trackList.innerHTML = html;
        document.getElementById('trackOrderTitle').textContent = 'Order #' + o.id + ' Tracking' + (o.statusLabel ? ' — ' + o.statusLabel : '');
        trackModal.classList.add('show');
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

function closeTrackModal() {
    trackModal.classList.remove('show');
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