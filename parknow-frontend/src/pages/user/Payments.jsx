import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { DollarSign, Receipt, CheckCircle2 } from 'lucide-react';

const Payments = () => {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPayments();
  }, []);

  const fetchPayments = async () => {
    setLoading(true);
    try {
      const res = await api.get('/payments/my');
      if (res.success) {
        setPayments(res.data);
      }
    } catch (err) {
      console.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <DollarSign color="#10b981" /> Billing & Payment History
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>Audit trails and receipts for completed parking charges</p>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>Loading billing receipts...</div>
      ) : payments.length === 0 ? (
        <div className="glass-panel" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
          No billing history records found.
        </div>
      ) : (
        <div className="glass-panel" style={{ overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ background: 'rgba(255,255,255,0.05)', borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '1rem' }}>Transaction ID</th>
                <th style={{ padding: '1rem' }}>Amount Paid</th>
                <th style={{ padding: '1rem' }}>Payment Method</th>
                <th style={{ padding: '1rem' }}>Status</th>
                <th style={{ padding: '1rem' }}>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((p) => (
                <tr key={p.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '1rem', fontWeight: '600', color: '#60a5fa' }}>{p.transactionId}</td>
                  <td style={{ padding: '1rem', fontWeight: '800', fontSize: '1rem' }}>${p.amount?.toFixed(2)}</td>
                  <td style={{ padding: '1rem' }}>{p.paymentMethod}</td>
                  <td style={{ padding: '1rem' }}>
                    <span className="badge badge-available">
                      <CheckCircle2 size={14} /> {p.paymentStatus}
                    </span>
                  </td>
                  <td style={{ padding: '1rem', color: 'var(--text-muted)' }}>
                    {new Date(p.paymentTime).toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default Payments;
