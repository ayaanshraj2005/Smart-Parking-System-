import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { ShieldCheck, DollarSign, Car, LayoutDashboard, Plus, RefreshCw, Users, MapPin, CheckCircle } from 'lucide-react';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';

const AdminDashboard = () => {
  const [summary, setSummary] = useState(null);
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // New Lot Modal State
  const [showAddLot, setShowAddLot] = useState(false);
  const [lotName, setLotName] = useState('');
  const [lotAddress, setLotAddress] = useState('');
  const [lotCity, setLotCity] = useState('');
  const [lotCapacity, setLotCapacity] = useState(10);
  const [submittingLot, setSubmittingLot] = useState(false);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError('');
    try {
      const [dashRes, lotsRes] = await Promise.all([
        api.get('/admin/dashboard'),
        api.get('/parking-lots')
      ]);

      setSummary(dashRes.data);
      setLots(lotsRes.data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch admin dashboard statistics.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateLot = async (e) => {
    e.preventDefault();
    setSubmittingLot(true);
    try {
      await api.post('/admin/parking-lots', {
        name: lotName,
        address: lotAddress,
        city: lotCity,
        totalCapacity: parseInt(lotCapacity),
      });

      setShowAddLot(false);
      setLotName('');
      setLotAddress('');
      setLotCity('');
      fetchDashboardData();
    } catch (err) {
      alert(err.message || 'Failed to create lot');
    } finally {
      setSubmittingLot(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading Admin Dashboard Analytics..." />;

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <ShieldCheck color="var(--primary)" /> Admin Dashboard & Control Center
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>Real-time revenue monitoring, occupancy gauges, and facility management</p>
        </div>

        <div style={{ display: 'flex', gap: '1rem' }}>
          <button onClick={fetchDashboardData} className="btn-secondary" style={{ padding: '0.6rem 1rem' }}>
            <RefreshCw size={16} /> Refresh KPI
          </button>
          <button onClick={() => setShowAddLot(true)} className="btn-primary" style={{ padding: '0.6rem 1.2rem' }}>
            <Plus size={18} /> Add Parking Lot
          </button>
        </div>
      </div>

      {error && <ErrorBanner message={error} />}

      {/* KPI Widgets Row */}
      <div className="grid-cols-3" style={{ marginBottom: '2.5rem' }}>
        <div className="glass-panel" style={{ padding: '1.75rem', background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(18, 26, 47, 0.8) 100%)' }}>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: '600' }}>Today's Revenue</span>
          <h2 style={{ fontSize: '2.25rem', fontWeight: '800', color: '#60a5fa', marginTop: '0.4rem' }}>
            ₹{summary?.todaysRevenue?.toFixed(2) || '0.00'}
          </h2>
          <span style={{ fontSize: '0.75rem', color: '#34d399', marginTop: '0.5rem', display: 'inline-block' }}>
            Monthly: ₹{summary?.monthlyRevenue?.toFixed(2) || '0.00'}
          </span>
        </div>

        <div className="glass-panel" style={{ padding: '1.75rem', background: 'linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(18, 26, 47, 0.8) 100%)' }}>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: '600' }}>Overall Occupancy Rate</span>
          <h2 style={{ fontSize: '2.25rem', fontWeight: '800', color: '#34d399', marginTop: '0.4rem' }}>
            {summary?.occupancyPercentage || 0}%
          </h2>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.5rem', display: 'inline-block' }}>
            {summary?.occupiedSlots || 0} of {summary?.totalParkingSlots || 0} slots occupied
          </span>
        </div>

        <div className="glass-panel" style={{ padding: '1.75rem', background: 'linear-gradient(135deg, rgba(245, 158, 11, 0.2) 0%, rgba(18, 26, 47, 0.8) 100%)' }}>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: '600' }}>Active Reservations</span>
          <h2 style={{ fontSize: '2.25rem', fontWeight: '800', color: '#fbbf24', marginTop: '0.4rem' }}>
            {summary?.activeReservations || 0}
          </h2>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.5rem', display: 'inline-block' }}>
            Completed: {summary?.completedReservations || 0}
          </span>
        </div>
      </div>

      {/* Managed Facilities Table */}
      <h2 style={{ fontSize: '1.4rem', fontWeight: '700', marginBottom: '1.25rem' }}>Managed Parking Facilities</h2>
      <div className="glass-panel" style={{ overflow: 'hidden', marginBottom: '2rem' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
          <thead>
            <tr style={{ background: 'rgba(255,255,255,0.05)', borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
              <th style={{ padding: '1rem' }}>Facility Name</th>
              <th style={{ padding: '1rem' }}>City</th>
              <th style={{ padding: '1rem' }}>Total Capacity</th>
              <th style={{ padding: '1rem' }}>Available Capacity</th>
              <th style={{ padding: '1rem' }}>Status</th>
            </tr>
          </thead>
          <tbody>
            {lots.map((lot) => (
              <tr key={lot.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                <td style={{ padding: '1rem', fontWeight: '700' }}>{lot.name}</td>
                <td style={{ padding: '1rem' }}>{lot.city}</td>
                <td style={{ padding: '1rem' }}>{lot.totalCapacity} Slots</td>
                <td style={{ padding: '1rem', fontWeight: '700', color: lot.availableCapacity > 0 ? '#34d399' : '#f87171' }}>
                  {lot.availableCapacity} Free
                </td>
                <td style={{ padding: '1rem' }}>
                  <span className={`badge ${lot.isActive ? 'badge-available' : 'badge-maintenance'}`}>
                    {lot.isActive ? 'ACTIVE' : 'DEACTIVATED'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal for adding parking lot */}
      {showAddLot && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(5px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="glass-panel" style={{ width: '100%', maxWidth: '480px', padding: '2rem' }}>
            <h3 style={{ fontSize: '1.4rem', fontWeight: '700', marginBottom: '1.5rem' }}>Create New Parking Facility</h3>
            <form onSubmit={handleCreateLot} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Facility Name</label>
                <input type="text" required className="form-input" placeholder="Metro Plaza Garage" value={lotName} onChange={(e) => setLotName(e.target.value)} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Address</label>
                <input type="text" required className="form-input" placeholder="500 Main Street" value={lotAddress} onChange={(e) => setLotAddress(e.target.value)} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>City</label>
                <input type="text" required className="form-input" placeholder="Metropolis" value={lotCity} onChange={(e) => setLotCity(e.target.value)} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Total Capacity</label>
                <input type="number" min="1" required className="form-input" value={lotCapacity} onChange={(e) => setLotCapacity(e.target.value)} />
              </div>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowAddLot(false)} className="btn-secondary" style={{ flex: 1 }}>Cancel</button>
                <button type="submit" disabled={submittingLot} className="btn-primary" style={{ flex: 1 }}>
                  {submittingLot ? 'Creating...' : 'Create Facility'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
