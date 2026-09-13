import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';
import { BarChart3, DollarSign, Building2, Car } from 'lucide-react';

const AdminReports = () => {
  const [revenueReport, setRevenueReport] = useState(null);
  const [occupancyReport, setOccupancyReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    setLoading(true);
    setError('');
    try {
      const [revRes, occRes] = await Promise.all([
        api.get('/admin/revenue'),
        api.get('/admin/occupancy')
      ]);

      setRevenueReport(revRes.data);
      setOccupancyReport(occRes.data);
    } catch (err) {
      setError(err.message || 'Failed to load system reports.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner message="Generating financial and occupancy reports..." />;

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <BarChart3 color="var(--primary)" /> Executive System Reports
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>Financial audit statements and facility utilization breakdown</p>
      </div>

      {error && <ErrorBanner message={error} />}

      {/* Revenue KPI Cards */}
      <div className="grid-cols-3" style={{ marginBottom: '2.5rem' }}>
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: '600' }}>Today's Revenue</span>
          <h2 style={{ fontSize: '2.2rem', fontWeight: '800', color: '#60a5fa', marginTop: '0.4rem' }}>
            ₹{revenueReport?.todaysRevenue?.toFixed(2) || '0.00'}
          </h2>
        </div>

        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: '600' }}>Monthly Revenue</span>
          <h2 style={{ fontSize: '2.2rem', fontWeight: '800', color: '#34d399', marginTop: '0.4rem' }}>
            ₹{revenueReport?.monthlyRevenue?.toFixed(2) || '0.00'}
          </h2>
        </div>

        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: '600' }}>All-Time Total Revenue</span>
          <h2 style={{ fontSize: '2.2rem', fontWeight: '800', color: '#fbbf24', marginTop: '0.4rem' }}>
            ₹{revenueReport?.totalRevenue?.toFixed(2) || '0.00'}
          </h2>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
        {/* Revenue by Facility Table */}
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: '700', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Building2 size={20} color="var(--primary)" /> Revenue Breakdown by Facility
          </h3>

          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '0.75rem 0' }}>Facility Name</th>
                <th style={{ padding: '0.75rem 0', textAlign: 'right' }}>Total Revenue</th>
              </tr>
            </thead>
            <tbody>
              {revenueReport?.revenueByLot?.map((lot) => (
                <tr key={lot.lotId} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '0.75rem 0', fontWeight: '600' }}>{lot.lotName}</td>
                  <td style={{ padding: '0.75rem 0', textAlign: 'right', fontWeight: '800', color: 'var(--accent)' }}>
                    ₹{lot.totalRevenue?.toFixed(2)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Facility Occupancy Breakdown Table */}
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: '700', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Car size={20} color="var(--accent)" /> Occupancy Rate by Facility
          </h3>

          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '0.75rem 0' }}>Facility Name</th>
                <th style={{ padding: '0.75rem 0' }}>Occupied / Total</th>
                <th style={{ padding: '0.75rem 0', textAlign: 'right' }}>Occupancy Rate</th>
              </tr>
            </thead>
            <tbody>
              {occupancyReport?.lotOccupancyList?.map((lot) => (
                <tr key={lot.lotId} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '0.75rem 0', fontWeight: '600' }}>{lot.lotName}</td>
                  <td style={{ padding: '0.75rem 0' }}>{lot.occupiedSlots} / {lot.totalCapacity}</td>
                  <td style={{ padding: '0.75rem 0', textAlign: 'right', fontWeight: '800', color: 'var(--warning)' }}>
                    {lot.occupancyPercentage}%
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminReports;
