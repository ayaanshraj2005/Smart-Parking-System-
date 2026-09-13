import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';
import EmptyState from '../../components/common/EmptyState';
import { DollarSign, Save, Building2 } from 'lucide-react';

const AdminPricing = () => {
  const [lots, setLots] = useState([]);
  const [selectedLotId, setSelectedLotId] = useState('');
  const [pricingRules, setPricingRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  // Form State for creating/updating pricing rule
  const [vehicleType, setVehicleType] = useState('CAR');
  const [baseHourlyRate, setBaseHourlyRate] = useState(50.00);
  const [peakMultiplier, setPeakMultiplier] = useState(1.00);
  const [overstayPenaltyRate, setOverstayPenaltyRate] = useState(20.00);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchLots();
  }, []);

  useEffect(() => {
    if (selectedLotId) {
      fetchPricingRules(selectedLotId);
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

  const fetchPricingRules = async (lotId) => {
    try {
      const res = await api.get(`/parking-lots/${lotId}/pricing-rules`);
      setPricingRules(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSaveRule = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setMessage('');
    try {
      await api.post(`/admin/parking-lots/${selectedLotId}/pricing-rules`, {
        lotId: parseInt(selectedLotId),
        vehicleType,
        baseHourlyRate: parseFloat(baseHourlyRate),
        peakHourMultiplier: parseFloat(peakMultiplier),
        overstayPenaltyRate: parseFloat(overstayPenaltyRate)
      });
      setMessage('Pricing rule configured successfully!');
      fetchPricingRules(selectedLotId);
    } catch (err) {
      setError(err.message || 'Failed to configure pricing rule');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading pricing configurations..." />;

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <DollarSign color="var(--accent)" /> Dynamic Pricing Rules Engine
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>Configure multi-tier vehicle rates, peak multipliers, and overstay penalty rates</p>
        </div>

        <select
          className="form-input"
          value={selectedLotId}
          onChange={(e) => setSelectedLotId(e.target.value)}
          style={{ width: '240px' }}
        >
          {lots.map((l) => (
            <option key={l.id} value={l.id}>{l.name}</option>
          ))}
        </select>
      </div>

      {error && <ErrorBanner message={error} onClose={() => setError('')} />}
      {message && (
        <div style={{ background: 'rgba(16, 185, 129, 0.15)', border: '1px solid rgba(16, 185, 129, 0.3)', color: '#34d399', padding: '0.85rem 1rem', borderRadius: '10px', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
          {message}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
        {/* Configure Form */}
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: '700', marginBottom: '1.25rem' }}>Configure Pricing Rule</h3>
          <form onSubmit={handleSaveRule} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Vehicle Type</label>
              <select className="form-input" value={vehicleType} onChange={(e) => setVehicleType(e.target.value)}>
                <option value="CAR">CAR (Four Wheeler)</option>
                <option value="SUV">SUV (Large Vehicle)</option>
                <option value="TWO_WHEELER">TWO_WHEELER (Bike / Scooter)</option>
                <option value="ELECTRIC_VEHICLE">ELECTRIC_VEHICLE (EV)</option>
                <option value="TRUCK">TRUCK</option>
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Base Hourly Rate (₹/hr)</label>
              <input type="number" step="0.01" min="0" required className="form-input" value={baseHourlyRate} onChange={(e) => setBaseHourlyRate(e.target.value)} />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Peak Multiplier (e.g. 1.0 - 2.5)</label>
              <input type="number" step="0.1" min="1.0" required className="form-input" value={peakMultiplier} onChange={(e) => setPeakMultiplier(e.target.value)} />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.3rem', color: 'var(--text-muted)' }}>Overstay Penalty Rate (₹/overstay hr)</label>
              <input type="number" step="0.01" min="0" required className="form-input" value={overstayPenaltyRate} onChange={(e) => setOverstayPenaltyRate(e.target.value)} />
            </div>

            <button type="submit" disabled={submitting} className="btn-primary" style={{ justifyContent: 'center', marginTop: '0.5rem' }}>
              <Save size={18} /> {submitting ? 'Saving Rule...' : 'Save Pricing Rule'}
            </button>
          </form>
        </div>

        {/* Existing Rules Table */}
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: '700', marginBottom: '1.25rem' }}>Active Facility Pricing Rules</h3>

          {pricingRules.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>No custom pricing rules configured yet. Default fallback rate applies (₹50.00/hr).</p>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '0.75rem 0' }}>Vehicle</th>
                  <th style={{ padding: '0.75rem 0' }}>Base Rate</th>
                  <th style={{ padding: '0.75rem 0' }}>Peak Surcharge</th>
                  <th style={{ padding: '0.75rem 0' }}>Overstay Penalty</th>
                </tr>
              </thead>
              <tbody>
                {pricingRules.map((r) => (
                  <tr key={r.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                    <td style={{ padding: '0.75rem 0', fontWeight: '700' }}>{r.vehicleType}</td>
                    <td style={{ padding: '0.75rem 0' }}>₹{r.baseHourlyRate}/hr</td>
                    <td style={{ padding: '0.75rem 0', color: 'var(--warning)' }}>{r.peakHourMultiplier}x</td>
                    <td style={{ padding: '0.75rem 0', color: 'var(--danger)' }}>₹{r.overstayPenaltyRate}/hr</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminPricing;
