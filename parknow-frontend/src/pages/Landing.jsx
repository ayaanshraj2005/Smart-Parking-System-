import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { Car, ShieldCheck, Zap, Award, ArrowRight, MapPin, CheckCircle, Clock } from 'lucide-react';
import LoadingSpinner from '../components/common/LoadingSpinner';

const Landing = () => {
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchActiveLots();
  }, []);

  const fetchActiveLots = async () => {
    try {
      setLoading(true);
      const res = await api.get('/parking-lots');
      setLots(res.data || []);
    } catch (err) {
      console.error('Failed to fetch parking lots:', err);
    } finally {
      setLoading(false);
    }
  };

  const totalCapacity = lots.reduce((acc, lot) => acc + (lot.totalCapacity || 0), 0);
  const availableCapacity = lots.reduce((acc, lot) => acc + (lot.availableCapacity || 0), 0);

  return (
    <div className="container" style={{ paddingBottom: '4rem' }}>
      {/* Hero Banner Section */}
      <section style={{
        textAlign: 'center',
        padding: '4rem 1rem 3rem 1rem',
        maxWidth: '850px',
        margin: '0 auto'
      }}>
        <div className="badge badge-reserved" style={{ marginBottom: '1.5rem', padding: '0.5rem 1rem', fontSize: '0.85rem' }}>
          <Zap size={16} /> Enterprise Smart Parking Engine 2.0
        </div>

        <h1 style={{
          fontSize: '3.2rem',
          fontWeight: '800',
          lineHeight: '1.2',
          marginBottom: '1.5rem',
          background: 'linear-gradient(135deg, #ffffff 0%, #9ca3af 100%)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent'
        }}>
          Intelligent Parking Reservations & Gate Automation
        </h1>

        <p style={{
          color: 'var(--text-muted)',
          fontSize: '1.15rem',
          lineHeight: '1.6',
          marginBottom: '2.5rem'
        }}>
          Reserve compatible parking slots in real time with min-heap priority allocation, dynamic billing, high-concurrency locking, and scannable digital passes.
        </p>

        <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          <Link to="/lots" className="btn-primary" style={{ padding: '0.9rem 2rem', fontSize: '1.05rem', textDecoration: 'none' }}>
            Find & Reserve Slot <ArrowRight size={18} />
          </Link>
          <Link to="/register" className="btn-secondary" style={{ padding: '0.9rem 2rem', fontSize: '1.05rem', textDecoration: 'none' }}>
            Create Account
          </Link>
        </div>
      </section>

      {/* Live Backend Statistics */}
      <section style={{ margin: '3rem 0' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '1.25rem', color: 'var(--text-main)', textAlign: 'center' }}>
          Live System Capability (Backend Integrated)
        </h2>

        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          gap: '1.5rem'
        }}>
          <div className="glass-panel" style={{ padding: '1.75rem', textAlign: 'center' }}>
            <div style={{ color: 'var(--primary)', marginBottom: '0.5rem' }}><MapPin size={28} /></div>
            <div style={{ fontSize: '2.2rem', fontWeight: '800' }}>{lots.length}</div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: '600' }}>Active Parking Garages</div>
          </div>

          <div className="glass-panel" style={{ padding: '1.75rem', textAlign: 'center' }}>
            <div style={{ color: 'var(--accent)', marginBottom: '0.5rem' }}><CheckCircle size={28} /></div>
            <div style={{ fontSize: '2.2rem', fontWeight: '800' }}>{availableCapacity}</div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: '600' }}>Real-time Available Slots</div>
          </div>

          <div className="glass-panel" style={{ padding: '1.75rem', textAlign: 'center' }}>
            <div style={{ color: 'var(--warning)', marginBottom: '0.5rem' }}><Car size={28} /></div>
            <div style={{ fontSize: '2.2rem', fontWeight: '800' }}>{totalCapacity}</div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: '600' }}>Total Slot Capacity</div>
          </div>

          <div className="glass-panel" style={{ padding: '1.75rem', textAlign: 'center' }}>
            <div style={{ color: '#a855f7', marginBottom: '0.5rem' }}><ShieldCheck size={28} /></div>
            <div style={{ fontSize: '2.2rem', fontWeight: '800' }}>100%</div>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: '600' }}>Pessimistic Double-Booking Lock</div>
          </div>
        </div>
      </section>

      {/* Featured Parking Facilities */}
      <section style={{ marginTop: '4rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <div>
            <h2 style={{ fontSize: '1.5rem', fontWeight: '700' }}>Featured Parking Garages</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Select a location to check slot availability and reserve</p>
          </div>
          <Link to="/lots" style={{ color: 'var(--primary)', textDecoration: 'none', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            View All <ArrowRight size={16} />
          </Link>
        </div>

        {loading ? (
          <LoadingSpinner message="Fetching live parking facilities from server..." />
        ) : (
          <div className="grid-cols-3">
            {lots.slice(0, 3).map((lot) => (
              <div key={lot.id} className="glass-panel animate-fade-in" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                    <h3 style={{ fontSize: '1.2rem', fontWeight: '700' }}>{lot.name}</h3>
                    <span className={`badge ${lot.availableCapacity > 0 ? 'badge-available' : 'badge-maintenance'}`}>
                      {lot.availableCapacity > 0 ? `${lot.availableCapacity} Free` : 'Full'}
                    </span>
                  </div>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '1.25rem' }}>
                    <MapPin size={16} /> {lot.address}, {lot.city}
                  </p>
                </div>

                <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '1rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <div>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Capacity</span>
                    <span style={{ fontWeight: '700', fontSize: '0.95rem' }}>{lot.availableCapacity} / {lot.totalCapacity}</span>
                  </div>
                  <Link to={`/reserve/${lot.id}`} className="btn-primary" style={{ padding: '0.5rem 1rem', fontSize: '0.85rem', textDecoration: 'none' }}>
                    Reserve Slot
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
};

export default Landing;
