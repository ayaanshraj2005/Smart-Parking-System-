import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../../services/api';
import { MapPin, Car, DollarSign, CheckCircle, ArrowRight, Shield } from 'lucide-react';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';

const LotDetails = () => {
  const { id } = useParams();
  const [lot, setLot] = useState(null);
  const [pricingRules, setPricingRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchLotDetails();
  }, [id]);

  const fetchLotDetails = async () => {
    try {
      setLoading(true);
      setError('');
      const [lotRes, pricingRes] = await Promise.all([
        api.get(`/parking-lots/${id}`),
        api.get(`/parking-lots/${id}/pricing-rules`).catch(() => ({ data: [] }))
      ]);

      setLot(lotRes.data);
      setPricingRules(pricingRes.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load parking lot details.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading parking lot details..." />;
  if (error) return <div className="container"><ErrorBanner message={error} /></div>;
  if (!lot) return null;

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div className="glass-panel" style={{ padding: '2.5rem', marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <span className={`badge ${lot.availableCapacity > 0 ? 'badge-available' : 'badge-maintenance'}`} style={{ marginBottom: '0.75rem' }}>
              {lot.availableCapacity > 0 ? `${lot.availableCapacity} Slots Free` : 'Fully Occupied'}
            </span>
            <h1 style={{ fontSize: '2.2rem', fontWeight: '800', margin: '0.5rem 0' }}>{lot.name}</h1>
            <p style={{ color: 'var(--text-muted)', fontSize: '1.05rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <MapPin size={20} color="var(--primary)" /> {lot.address}, {lot.city}
            </p>
          </div>

          <Link to={`/reserve/${lot.id}`} className="btn-primary" style={{ padding: '0.85rem 1.75rem', fontSize: '1rem', textDecoration: 'none' }}>
            Book Slot Now <ArrowRight size={18} />
          </Link>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem' }}>
        {/* Capacity & Info */}
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: '700', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Car size={20} color="var(--primary)" /> Facility Overview
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
              <span style={{ color: 'var(--text-muted)' }}>Total Slot Capacity</span>
              <strong style={{ fontSize: '1.1rem' }}>{lot.totalCapacity}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
              <span style={{ color: 'var(--text-muted)' }}>Available Capacity</span>
              <strong style={{ fontSize: '1.1rem', color: 'var(--accent)' }}>{lot.availableCapacity}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
              <span style={{ color: 'var(--text-muted)' }}>Operational Status</span>
              <span className="badge badge-available">ACTIVE</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Allocation Strategy</span>
              <strong style={{ color: 'var(--primary)', fontSize: '0.9rem' }}>Min-Heap Priority Queue</strong>
            </div>
          </div>
        </div>

        {/* Pricing Rules */}
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: '700', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <DollarSign size={20} color="var(--accent)" /> Configured Pricing Rates
          </h3>

          {pricingRules.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Standard Hourly Rate: ₹50.00 / hr</p>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', textAlign: 'left', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '0.5rem 0' }}>Vehicle Type</th>
                  <th style={{ padding: '0.5rem 0' }}>Base Rate</th>
                  <th style={{ padding: '0.5rem 0' }}>Peak Multiplier</th>
                </tr>
              </thead>
              <tbody>
                {pricingRules.map((rule) => (
                  <tr key={rule.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                    <td style={{ padding: '0.75rem 0', fontWeight: '600' }}>{rule.vehicleType}</td>
                    <td style={{ padding: '0.75rem 0' }}>₹{rule.baseHourlyRate}/hr</td>
                    <td style={{ padding: '0.75rem 0', color: 'var(--warning)' }}>{rule.peakHourMultiplier}x</td>
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

export default LotDetails;
