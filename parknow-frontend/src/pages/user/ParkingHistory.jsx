import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';
import EmptyState from '../../components/common/EmptyState';
import { Clock, Calendar, MapPin, Car, CheckCircle } from 'lucide-react';

const ParkingHistory = () => {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/sessions/history');
      setSessions(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load session history.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner message="Fetching parking history..." />;

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '800' }}>Parking Session History</h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>View past completed and historical parking sessions</p>
      </div>

      {error && <ErrorBanner message={error} />}

      {sessions.length === 0 ? (
        <EmptyState 
          title="No Parking History" 
          description="You haven't completed any parking sessions yet." 
          icon={Clock} 
          actionLink="/lots" 
          actionText="Find & Reserve Parking"
        />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {sessions.map((s) => (
            <div key={s.id} className="glass-panel" style={{ padding: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
                  <span className="badge badge-available">
                    <CheckCircle size={14} /> {s.status}
                  </span>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Ticket: {s.ticketCode}</span>
                </div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: '700' }}>{s.lotName} — Slot {s.slotNumber}</h3>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginTop: '0.25rem' }}>
                  <Car size={14} style={{ display: 'inline', verticalAlign: 'middle' }} /> {s.licensePlate} ({s.vehicleType})
                </p>
              </div>

              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--accent)' }}>₹{s.totalFee || '0.00'}</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                  {new Date(s.entryTime).toLocaleDateString()} ({s.durationMinutes || 0} mins)
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ParkingHistory;
