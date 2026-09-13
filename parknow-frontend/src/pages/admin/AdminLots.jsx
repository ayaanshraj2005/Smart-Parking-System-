import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';
import EmptyState from '../../components/common/EmptyState';
import ConfirmationModal from '../../components/common/ConfirmationModal';
import { MapPin, Plus, Power, Building2 } from 'lucide-react';

const AdminLots = () => {
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Form State
  const [showAdd, setShowAdd] = useState(false);
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [city, setCity] = useState('');
  const [capacity, setCapacity] = useState(10);
  const [submitting, setSubmitting] = useState(false);

  // Status Toggle Modal
  const [selectedLotToggle, setSelectedLotToggle] = useState(null);

  useEffect(() => {
    fetchLots();
  }, []);

  const fetchLots = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/parking-lots');
      setLots(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load parking lots.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateLot = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await api.post('/admin/parking-lots', {
        name,
        address,
        city,
        totalCapacity: parseInt(capacity)
      });
      setShowAdd(false);
      setName('');
      setAddress('');
      setCity('');
      fetchLots();
    } catch (err) {
      setError(err.message || 'Failed to create parking lot.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleLotStatus = async () => {
    if (!selectedLotToggle) return;
    try {
      const newStatus = !selectedLotToggle.isActive;
      await api.patch(`/admin/parking-lots/${selectedLotToggle.id}/status?isActive=${newStatus}`);
      setSelectedLotToggle(null);
      fetchLots();
    } catch (err) {
      setError(err.message || 'Failed to update lot status.');
    }
  };

  if (loading) return <LoadingSpinner message="Loading parking facilities..." />;

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Building2 color="var(--primary)" /> Parking Facility Management
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>Configure parking lots, capacity limits, and operational states</p>
        </div>

        <button onClick={() => setShowAdd(true)} className="btn-primary">
          <Plus size={18} /> Add Parking Lot
        </button>
      </div>

      {error && <ErrorBanner message={error} onClose={() => setError('')} />}

      {lots.length === 0 ? (
        <EmptyState title="No Parking Lots" description="Click 'Add Parking Lot' to register your first facility." icon={Building2} />
      ) : (
        <div className="glass-panel" style={{ overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ background: 'rgba(255,255,255,0.05)', borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '1rem' }}>Facility Name</th>
                <th style={{ padding: '1rem' }}>Address & City</th>
                <th style={{ padding: '1rem' }}>Total Capacity</th>
                <th style={{ padding: '1rem' }}>Available Capacity</th>
                <th style={{ padding: '1rem' }}>Status</th>
                <th style={{ padding: '1rem', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {lots.map((lot) => (
                <tr key={lot.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '1rem', fontWeight: '700' }}>{lot.name}</td>
                  <td style={{ padding: '1rem', color: 'var(--text-muted)' }}>{lot.address}, {lot.city}</td>
                  <td style={{ padding: '1rem' }}>{lot.totalCapacity} Slots</td>
                  <td style={{ padding: '1rem', fontWeight: '700', color: lot.availableCapacity > 0 ? 'var(--accent)' : 'var(--danger)' }}>
                    {lot.availableCapacity} Free
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <span className={`badge ${lot.isActive ? 'badge-available' : 'badge-maintenance'}`}>
                      {lot.isActive ? 'ACTIVE' : 'DEACTIVATED'}
                    </span>
                  </td>
                  <td style={{ padding: '1rem', textAlign: 'right' }}>
                    <button
                      onClick={() => setSelectedLotToggle(lot)}
                      className={lot.isActive ? 'btn-danger' : 'btn-secondary'}
                      style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}
                    >
                      <Power size={14} /> {lot.isActive ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Add Lot Modal */}
      {showAdd && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(5px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="glass-panel" style={{ width: '100%', maxWidth: '480px', padding: '2rem' }}>
            <h3 style={{ fontSize: '1.4rem', fontWeight: '700', marginBottom: '1.5rem' }}>Create Parking Facility</h3>
            <form onSubmit={handleCreateLot} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Facility Name</label>
                <input type="text" required className="form-input" placeholder="Downtown Central Garage" value={name} onChange={(e) => setName(e.target.value)} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Address</label>
                <input type="text" required className="form-input" placeholder="100 Main St" value={address} onChange={(e) => setAddress(e.target.value)} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>City</label>
                <input type="text" required className="form-input" placeholder="Metropolis" value={city} onChange={(e) => setCity(e.target.value)} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Total Capacity</label>
                <input type="number" min="1" required className="form-input" value={capacity} onChange={(e) => setCapacity(e.target.value)} />
              </div>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowAdd(false)} className="btn-secondary" style={{ flex: 1 }}>Cancel</button>
                <button type="submit" disabled={submitting} className="btn-primary" style={{ flex: 1 }}>{submitting ? 'Creating...' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Confirmation Modal */}
      <ConfirmationModal
        isOpen={Boolean(selectedLotToggle)}
        title={`${selectedLotToggle?.isActive ? 'Deactivate' : 'Activate'} Parking Facility`}
        message={`Are you sure you want to ${selectedLotToggle?.isActive ? 'deactivate' : 'activate'} ${selectedLotToggle?.name}?`}
        confirmText="Confirm Change"
        onConfirm={handleToggleLotStatus}
        onCancel={() => setSelectedLotToggle(null)}
        isDanger={selectedLotToggle?.isActive}
      />
    </div>
  );
};

export default AdminLots;
