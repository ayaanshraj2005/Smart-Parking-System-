import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { Calendar, Clock, LogOut, CheckCircle2, AlertCircle, DollarSign, Car } from 'lucide-react';

const ActiveSession = () => {
  const [sessions, setSessions] = useState([]);
  const [ticketInput, setTicketInput] = useState('');
  const [selectedMethod, setSelectedMethod] = useState('UPI');
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    fetchActiveSessions();
  }, []);

  const fetchActiveSessions = async () => {
    setLoading(true);
    try {
      const res = await api.get('/sessions/active');
      if (res.success) {
        setSessions(res.data);
      }
    } catch (err) {
      console.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleStartSession = async (e) => {
    e.preventDefault();
    if (!ticketInput) return;
    setError('');
    setMessage('');
    setActionLoading(true);

    try {
      const res = await api.post('/sessions/entry', { identifier: ticketInput });
      if (res.success) {
        setMessage('Vehicle entry recorded! Parking session initiated.');
        setTicketInput('');
        fetchActiveSessions();
      }
    } catch (err) {
      setError(err.message || 'Failed to start session');
    } finally {
      setActionLoading(false);
    }
  };

  const handleEndSession = async (sessionId) => {
    setError('');
    setMessage('');
    setActionLoading(true);

    try {
      const res = await api.post('/sessions/exit', {
        sessionId: sessionId,
        paymentMethod: selectedMethod,
      });

      if (res.success) {
        setMessage(`Parking session completed! Total fee processed: $${res.data.totalFee?.toFixed(2)}`);
        fetchActiveSessions();
      }
    } catch (err) {
      setError(err.message || 'Failed to complete checkout');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="container">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <Calendar color="#3b82f6" /> Active Parking Sessions
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>Real-time gate check-in & automated exit fee calculator</p>
      </div>

      {message && (
        <div style={{ background: 'rgba(16, 185, 129, 0.15)', border: '1px solid rgba(16, 185, 129, 0.3)', color: '#34d399', padding: '1rem', borderRadius: '12px', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <CheckCircle2 size={18} /> {message}
        </div>
      )}

      {error && (
        <div style={{ background: 'rgba(239, 68, 68, 0.15)', border: '1px solid rgba(239, 68, 68, 0.3)', color: '#f87171', padding: '1rem', borderRadius: '12px', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={18} /> {error}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: '2rem' }}>
        {/* Left: Active Live Sessions */}
        <div>
          <h3 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '1rem' }}>Vehicles Currently Parked</h3>
          {loading ? (
            <div style={{ padding: '2rem', color: 'var(--text-muted)' }}>Loading active sessions...</div>
          ) : sessions.length === 0 ? (
            <div className="glass-panel" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              No vehicles currently parked in active sessions.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {sessions.map((s) => (
                <div key={s.id} className="glass-panel" style={{ padding: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.4rem' }}>
                      <span className="badge badge-occupied">IN PROGRESS</span>
                      <strong style={{ fontSize: '1.1rem' }}>{s.licensePlate}</strong>
                    </div>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{s.lotName} - Slot {s.slotNumber} (Floor {s.floorNumber})</p>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.2rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <Clock size={14} /> Entry: {new Date(s.entryTime).toLocaleTimeString()}
                    </p>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem' }}>
                    <select
                      value={selectedMethod}
                      onChange={(e) => setSelectedMethod(e.target.value)}
                      className="form-input"
                      style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem', background: '#0f172a' }}
                    >
                      <option value="UPI">UPI / GPay</option>
                      <option value="CREDIT_CARD">Credit Card</option>
                      <option value="WALLET">Wallet</option>
                    </select>

                    <button
                      onClick={() => handleEndSession(s.id)}
                      disabled={actionLoading}
                      className="btn-primary"
                      style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
                    >
                      <LogOut size={16} /> Exit & Pay Fee
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right: Security Gate Check-In Form */}
        <div>
          <div className="glass-panel" style={{ padding: '2rem' }}>
            <h3 style={{ fontSize: '1.2rem', fontWeight: '700', marginBottom: '1rem' }}>Gate Check-In Simulator</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
              Enter ticket code at gate barrier to record entry and lock slot status.
            </p>

            <form onSubmit={handleStartSession}>
              <div style={{ marginBottom: '1.25rem' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.4rem', color: 'var(--text-muted)' }}>Digital Ticket Code</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. PN-A1B2C3D4"
                  className="form-input"
                  value={ticketInput}
                  onChange={(e) => setTicketInput(e.target.value)}
                />
              </div>

              <button type="submit" disabled={actionLoading} className="btn-primary" style={{ width: '100%', justifyContent: 'center' }}>
                Start Parking Session
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ActiveSession;
