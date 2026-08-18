if (!localStorage.getItem('token')) {
    window.location.href = '/index.html';
}

const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
const myUserInfo = document.getElementById('myUserInfo');
if (myUserInfo && currentUser.name) {
    myUserInfo.textContent = 'Logged in as: ' + currentUser.name + ' (ID: ' + currentUser.userId + ')';
}

async function loadCart() {
    const tbody = document.getElementById('cartTableBody');
    if (!currentUser.userId) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;">Please log in to view your cart</td></tr>`;
        return;
    }
    try {
        const cart = await orderAPI.getCart(currentUser.userId);
        const items = cart.items || [];
        if (items.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;">Your cart is empty</td></tr>`;
            document.getElementById('cartTotal').textContent = 'Total: ₹0.00';
            return;
        }
        tbody.innerHTML = items.map(it => `
            <tr>
                <td>${it.productName} <span style="color:#999;font-size:0.8rem;">(#${it.productId})</span></td>
                <td>₹${parseFloat(it.unitPrice).toFixed(2)}</td>
                <td>
                    <input type="number" min="1" value="${it.quantity}" onchange="updateQty(${it.productId}, this.value)"
                           style="width:70px;padding:0.35rem;border:1px solid #d1d5db;border-radius:4px;">
                </td>
                <td>₹${parseFloat(it.lineTotal).toFixed(2)}</td>
                <td>
                    <button class="btn btn-sm btn-danger" onclick="removeItem(${it.productId})">Remove</button>
                </td>
            </tr>
        `).join('');
        document.getElementById('cartTotal').textContent = 'Total: ₹' + parseFloat(cart.totalAmount).toFixed(2);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:#e53e3e;">Failed to load cart: ${err.message}</td></tr>`;
    }
}

async function loadProducts() {
    const select = document.getElementById('cartProductSelect');
    const info = document.getElementById('cartProductInfo');
    try {
        const products = await productAPI.getAll();
        if (!products || products.length === 0) {
            select.innerHTML = '<option value="">-- No products available --</option>';
            return;
        }
        select.innerHTML = '<option value="">-- Select Product --</option>' +
            products.map(p => `<option value="${p.id}" data-price="${p.price}">${p.name} - ₹${parseFloat(p.price).toFixed(2)}</option>`).join('');
        select.onchange = function() {
            const opt = this.options[this.selectedIndex];
            info.innerHTML = opt.value ? 'Price: ₹' + parseFloat(opt.dataset.price).toFixed(2) : '';
        };
    } catch (err) {
        select.innerHTML = '<option value="">-- Failed to load products --</option>';
    }
}

async function addToCart() {
    const productId = parseInt(document.getElementById('cartProductSelect').value);
    const quantity = parseInt(document.getElementById('cartQuantity').value) || 1;
    if (!productId) {
        alert('Please select a product');
        return;
    }
    try {
        await orderAPI.addToCart(currentUser.userId, { productId, quantity });
        loadCart();
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

async function updateQty(productId, value) {
    const quantity = parseInt(value) || 1;
    try {
        await orderAPI.updateCartItem(currentUser.userId, productId, { productId, quantity });
        loadCart();
    } catch (err) {
        alert('Error: ' + err.message);
        loadCart();
    }
}

async function removeItem(productId) {
    try {
        await orderAPI.removeCartItem(currentUser.userId, productId);
        loadCart();
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

async function clearCart() {
    if (!confirm('Clear the entire cart?')) return;
    try {
        await orderAPI.clearCart(currentUser.userId);
        loadCart();
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

async function checkout() {
    if (!confirm('Place orders for all items in the cart?')) return;
    try {
        const orders = await orderAPI.checkout(currentUser.userId);
        alert('Checkout successful! ' + orders.length + ' order(s) placed. Order IDs: ' + orders.map(o => '#' + o.id).join(', '));
        window.location.href = '/orders.html';
    } catch (err) {
        alert('Checkout failed: ' + err.message);
    }
}

loadCart();
loadProducts();