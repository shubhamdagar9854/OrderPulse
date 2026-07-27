const GEMINI_API_KEY = 'YOUR_GEMINI_API_KEY';
const GEMINI_URL = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${GEMINI_API_KEY}`;

if (!localStorage.getItem('token')) {
    window.location.href = '/index.html';
}

async function getInsights() {
    const userId = document.getElementById('aiUserId').value;
    const resultDiv = document.getElementById('insightsResult');

    if (!userId) {
        resultDiv.innerHTML = `<p style="text-align:center;color:#4d4d4e;">Please enter a User ID</p>`;
        return;
    }

    resultDiv.innerHTML = `<p style="text-align:center;color:#666;">Fetching orders and generating insights...</p>`;

    try {
        const orders = await orderAPI.getByUser(userId);
        if (!orders || orders.length === 0) {
            resultDiv.innerHTML = `<p style="text-align:center;color:#e53e3e;">No orders found for User ID ${userId}</p>`;
            return;
        }

        const prompt = `You are a smart order assistant. Analyze this user's order history and provide insights in simple text format (use bullet points).

Order History:
${JSON.stringify(orders, null, 2)}

Provide:
1. Total orders and total amount spent
2. Most frequently ordered product IDs
3. Spending patterns
4. Recommendations`;

        const res = await fetch(GEMINI_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                contents: [{ parts: [{ text: prompt }] }]
            })
        });

        const data = await res.json();
        const text = data?.candidates?.[0]?.content?.parts?.[0]?.text || 'No insights generated.';

        resultDiv.innerHTML = `
            <div style="background:#f7f8fc;border-radius:8px;padding:1.5rem;">
                <h3 style="margin-bottom:1rem;color:#667eea;">Insights for User #${userId}</h3>
                <div style="white-space:pre-wrap;line-height:1.7;">${text}</div>
            </div>
        `;
    } catch (err) {
        resultDiv.innerHTML = `<p style="text-align:center;color:#e53e3e;">Error: ${err.message}</p>`;
    }
}
