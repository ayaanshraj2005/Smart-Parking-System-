import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';
import EmptyState from '../../components/common/EmptyState';
import { Layers, Plus, Wrench, Ban, CheckCircle } from 'lucide-react';

const AdminSlots = () => {
  const [lots, setLots] = useState([]);
  const [selectedLotId, setSelectedLotId] = useState('');
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Add Slot Form
  const [showAddSlot, setShowAddSlot] = useState(false);
  const [slotNumber, setSlotNumber] = useState('');
  const [floorNumber, setFloorNumber] = useState(1);
  const [vehicleType, setVehicleType] = useState('CAR');
  const [submittingSlot, setSubmittingSlot] = useState(false);

  useEffect(() => {
    fetchLots();
  }, []);

  useEffect(() => {
    if (selectedLotId) {
      fetchSlots(selectedLotId);
    }
  }, [selectedLotId]);

  const fetchLots = async () => {
    setLoading(true);
    try {
      const res = await api.get('/parking-lots');
      setLots(res.data || []);
      if (res.data?.length > 0) {
        setSelectedLotId(res.data[0].id);
      }
    } catch (err) {
      setError(err.message || 'Failed to fetch parking lots');
    } finally {
      setLoading(false);
    }
  };

  const fetchSlots = async (lotId) => {
    try {
      const res = await api.get(`/parking-slots/lot/${lotId}`);
      setSlots(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch slots');
    }
  };

  const handleCreateSlot = async (e) => {
    e.preventDefault();
    setSubmittingSlot(true);
    setError('');
    try {
      await api.post('/admin/slots', {
        lotId: parseInt(selectedLotId),
        slotNumber,
        floorNumber: parseInt(floorNumber),
        vehicleType
      });
      setShowAddSlot(false);
      setSlotNumber('');
      fetchSlots(selectedLotId);
    } catch (err) {
      setError(err.message || 'Failed to create slot');
    } finally {
      setSubmittingSlot(false);
    }
  };

  const handleUpdateSlotStatus = async (slotId, status) => {
    try {
      await api.patch(`/admin/slots/${slotId}/status?status=${status}`);
      fetchSlots(selectedLotId);
    } catch (err) {
      setError(err.message || 'Failed to update slot status');
    }
  };

  if (loading) return <LoadingSpinner message="Loading slot management..." />;

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Layers color="var(--primary)" /> Parking Slot Management
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>Configure individual slots, floors, vehicle compatibility, and maintenance overrides</p>
        </div>

        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <select
            className="form-input"
            value={selectedLotId}
            onChange={(e) => setSelectedLotId(e.target.value)}
            style={{ width: '220px' }}
          >
            {lots.map((l) => (
              <option key={l.id} value={l.id}>{l.name}</option>
            ))}
          </select>

          <button onClick={() => setShowAddSlot(true)} className="btn-primary">
            <Plus size={18} /> Add Slot
          </button>
        </div>
      </div>

      {error && <ErrorBanner message={error} onClose={() => setError('')} />}

      {slots.length === 0 ? (
        <EmptyState title="No Slots Configured" description="Click 'Add Slot' to configure parking slots for this lot." icon={Layers} />
      ) : (
        <div className="glass-panel" style={{ overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ background: 'rgba(255,255,255,0.05)', borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '1rem' }}>Slot Number</th>
                <th style={{ padding: '1rem' }}>Floor Level</th>
                <th style={{ padding: '1rem' }}>Vehicle Type</th>
                <th style={{ padding: '1rem' }}>Status</th>
                <th style={{ padding: '1rem', textAlign: 'right' }}>Status Override Actions</th>
              </tr>
            </thead>
            <tbody>
              {slots.map((s) => (
                <tr key={s.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '1rem', fontWeight: '700', color: 'var(--primary)' }}>{s.slotNumber}</td>
                  <td style={{ padding: '1rem' }}>Floor {s.floorNumber}</td>
                  <td style={{ padding: '1rem' }}>{s.vehicleType}</td>
                  <td style={{ padding: '1rem' }}>
                    <span className={`badge ${
                      s.status === 'AVAILABLE' ? 'badge-available' :
                      s.status === 'OCCUPIED' ? 'badge-occupied' :
                      s.status === 'RESERVED' ? 'badge-reserved' : 'badge-maintenance'
                    }`}>
                      {s.status}
                    </span>
                  </td>
                  <td style={{ padding: '1rem', textAlign: 'right' }}>
                    <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                      <button
                        onClick={() => handleUpdateSlotStatus(s.id, 'AVAILABLE')}
                        className="btn-secondary"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', color: '#34d399' }}
                      >
                        <CheckCircle size={12} /> Available
                      </button>
                      <button
                        onClick={() => handleUpdateSlotStatus(s.id, 'MAINTENANCE')}
                        className="btn-secondary"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', color: '#fbbf24' }}
                      >
                        <Wrench size={12} /> Maintenance
                      </button>
                      <button
                        onClick={() => handleUpdateSlotStatus(s.id, 'DISABLED')}
                        className="btn-secondary"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', color: '#f87171' }}
                      >
                        <Ban size={12} /> Disable
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Add Slot Modal */}
      {showAddSlot && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(5px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="glass-panel" style={{ width: '100%', maxWidth: '450px', padding: '2rem' }}>
            <h3 style={{ fontSize: '1.4rem', fontWeight: '700', marginBottom: '1.5rem' }}>Add Parking Slot</h3>
            <form onSubmit={handleCreateSlot} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Slot Number</label>
                <input type="text" required className="form-input" placeholder="A101" value={slotNumber} onChange={(e) => setSlotNumber(e.target.value)} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Floor Level</label>
                <input type="number" min="0" required className="form-input" value={floorNumber} onChange={(e) => setFloorNumber(e.target.value)} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Vehicle Type</label>
                <select className="form-input" value={vehicleType} onChange={(e) => setVehicleType(e.target.value)}>
                  <option value="CAR">CAR (Four Wheeler)</option>
                  <option value="SUV">SUV (Large Vehicle)</option>
                  <option value="TWO_WHEELER">TWO_WHEELER (Bike)</option>
                  <option value="ELECTRIC_VEHICLE">ELECTRIC_VEHICLE (EV)</option>
                  <option value="TRUCK">TRUCK</option>
                </select>
              </div>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowAddSlot(false)} className="btn-secondary" style={{ flex: 1 }}>Cancel</button>
                <button type="submit" disabled={submittingSlot} className="btn-primary" style={{ flex: 1 }}>
                  {submittingSlot ? 'Saving...' : 'Save Slot'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminSlots;
