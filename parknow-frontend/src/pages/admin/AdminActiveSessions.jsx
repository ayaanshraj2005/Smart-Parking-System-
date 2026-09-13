import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';
import EmptyState from '../../components/common/EmptyState';
import ConfirmationModal from '../../components/common/ConfirmationModal';
import { Calendar, LogOut, Car, MapPin, Clock } from 'lucide-react';

const AdminActiveSessions = () => {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedExitSession, setSelectedExitSession] = useState(null);
  const [exiting, setExiting] = useState(false);

  useEffect(() => {
    fetchActiveSessions();
  }, []);

  const fetchActiveSessions = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/sessions/active');
      setSessions(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch active sessions.');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmExit = async () => {
    if (!selectedExitSession) return;
    setExiting(true);
    try {
      await api.post(`/sessions/${selectedExitSession.id}/exit`);
      setSelectedExitSession(null);
      fetchActiveSessions();
    } catch (err) {
      setError(err.message || 'Failed to check out session.');
    } finally {
      setExiting(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading live active parking sessions..." />;

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <Calendar color="var(--accent)" /> Live Active Parking Sessions
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>Real-time monitoring of vehicles parked inside facilities with admin force check-out capability</p>
      </div>

      {error && <ErrorBanner message={error} onClose={() => setError('')} />}

      {sessions.length === 0 ? (
        <EmptyState title="No Active Vehicles" description="There are currently no vehicles inside any parking facility." icon={Calendar} />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
          {sessions.map((s) => (
            <div key={s.id} className="glass-panel" style={{ padding: '1.75rem', border: '1px solid rgba(16, 185, 129, 0.3)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <span className="badge badge-available">
                  <Clock size={14} /> IN PROGRESS
                </span>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>ID: #{s.id}</span>
              </div>

              <h3 style={{ fontSize: '1.25rem', fontWeight: '800', marginBottom: '0.25rem' }}>{s.lotName}</h3>
              <p style={{ color: 'var(--primary)', fontWeight: '700', fontSize: '1rem', marginBottom: '1rem' }}>
                Floor {s.floorNumber} — Slot {s.slotNumber}
              </p>

              <div style={{ background: 'rgba(15, 23, 42, 0.6)', padding: '1rem', borderRadius: '10px', marginBottom: '1.25rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.85rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>License Plate:</span>
                  <strong style={{ color: '#fff' }}>{s.licensePlate}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.85rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Vehicle Type:</span>
                  <span>{s.vehicleType}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Check-in Time:</span>
                  <span>{new Date(s.entryTime).toLocaleTimeString()}</span>
                </div>
              </div>

              <button
                onClick={() => setSelectedExitSession(s)}
                className="btn-danger"
                style={{ width: '100%', justifyContent: 'center', display: 'flex', alignItems: 'center', gap: '0.5rem' }}
              >
                <LogOut size={16} /> Force Gate Exit Checkout
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Confirmation Modal */}
      <ConfirmationModal
        isOpen={Boolean(selectedExitSession)}
        title="Admin Force Check-Out"
        message={`Are you sure you want to process exit check-out for vehicle ${selectedExitSession?.licensePlate} in Slot ${selectedExitSession?.slotNumber}? Slot will be released to AVAILABLE.`}
        confirmText="Confirm Exit & Bill"
        onConfirm={handleConfirmExit}
        onCancel={() => setSelectedExitSession(null)}
        isDanger={true}
        isLoading={exiting}
      />
    </div>
  );
};

export default AdminActiveSessions;
