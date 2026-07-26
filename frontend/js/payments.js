if (!localStorage.getItem('token')) {
    window.location.href = '/index.html';
}

document.getElementById('paymentForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        orderId: parseInt(document.getElementById('payOrderId').value),
        amount: parseFloat(document.getElementById('payAmount').value)
    };
    try {
        const res = await paymentAPI.process(data);
        document.getElementById('paymentResult').innerHTML =
            `<div style="padding:0.75rem;background:#c6f6d5;color:#276749;border-radius:6px;">
                Payment successful! ID: ${res.id} | Status: ${res.status}
            </div>`;
        document.getElementById('paymentForm').reset();
    } catch (err) {
        document.getElementById('paymentResult').innerHTML =
            `<div style="padding:0.75rem;background:#fed7d7;color:#9b2c2c;border-radius:6px;">
                Payment failed: ${err.message}
            </div>`;
    }
});

document.getElementById('refundForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        orderId: parseInt(document.getElementById('refundOrderId').value)
    };
    try {
        const res = await paymentAPI.refund(data);
        document.getElementById('refundResult').innerHTML =
            `<div style="padding:0.75rem;background:#c6f6d5;color:#276749;border-radius:6px;">
                Refund processed! ID: ${res.id} | Status: ${res.status}
            </div>`;
        document.getElementById('refundForm').reset();
    } catch (err) {
        document.getElementById('refundResult').innerHTML =
            `<div style="padding:0.75rem;background:#fed7d7;color:#9b2c2c;border-radius:6px;">
                Refund failed: ${err.message}
            </div>`;
    }
});