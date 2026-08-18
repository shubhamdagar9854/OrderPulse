let rzpLoading = null;

function loadRazorpayScript() {
    if (window.Razorpay) return Promise.resolve(window.Razorpay);
    if (rzpLoading) return rzpLoading;
    rzpLoading = new Promise((resolve, reject) => {
        const script = document.createElement('script');
        script.src = 'https://checkout.razorpay.com/v1/checkout.js';
        script.async = true;
        script.onload = () => resolve(window.Razorpay);
        script.onerror = () => {
            rzpLoading = null;
            reject(new Error('Could not load Razorpay checkout, please try again'));
        };
        document.head.appendChild(script);
    });
    return rzpLoading;
}

async function openRazorpayCheckout(orderId, { onSuccess, onError } = {}) {
    try {
        const payment = await paymentAPI.razorpayOrder({ orderId });
        const Razorpay = await loadRazorpayScript();
        const amountPaise = Math.round(payment.amount * 100);
        const options = {
            key: payment.keyId,
            amount: amountPaise,
            currency: payment.currency || 'INR',
            name: 'OrderPulse',
            description: 'Order #' + orderId,
            order_id: payment.razorpayOrderId,
            handler: async (response) => {
                try {
                    const verified = await paymentAPI.verify({
                        razorpayOrderId: response.razorpay_order_id,
                        paymentId: response.razorpay_payment_id,
                        signature: response.razorpay_signature
                    });
                    if (onSuccess) onSuccess(verified, response);
                } catch (err) {
                    if (onError) onError(err);
                }
            },
            theme: { color: '#667eea' }
        };
        const rzp = new Razorpay(options);
        rzp.open();
    } catch (err) {
        if (onError) onError(err);
    }
}