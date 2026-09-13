import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { Car, Ticket, Calendar, DollarSign, ArrowRight, ShieldCheck, MapPin, CheckCircle } from 'lucide-react';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const UserDashboard = () => {
  const { user } = useAuth();
  const [activeSession, setActiveSession] = useState(null);
  const [reservations, setReservations] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [sessRes, resRes, vehRes] = await Promise.allSettled([
        api.get('/sessions/active'),
        api.get('/reservations/my'),
        api.get('/users/vehicles')
      ]);

      if (sessRes.status === 'fulfilled' && sessRes.value.data?.length > 0) {
        setActiveSession(sessRes.value.data[0]);
      }
      if (resRes.status === 'fulfilled') {
        setReservations(resRes.value.data || []);
      }
      if (vehRes.status === 'fulfilled') {
        setVehicles(vehRes.value.data || []);
      }
    } catch (err) {
      console.error('Error loading user dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading your dashboard..." />;

  const confirmedReservations = reservations.filter(r => r.status === 'CONFIRMED');

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '800' }}>Welcome, {user?.fullName || 'Driver'}!</h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>Manage your parking passes, active sessions, and registered vehicles</p>
      </div>

      {/* Active Session Alert Banner */}
      {activeSession && (
        <div className="glass-panel" style={{
          padding: '1.5rem',
          marginBottom: '2rem',
          background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(16, 185, 129, 0.2))',
          border: '1px solid rgba(59, 130, 246, 0.4)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem'
        }}>
          <div>
            <div className="badge badge-available" style={{ marginBottom: '0.5rem' }}>
              <Calendar size={14} /> ACTIVE PARKING SESSION IN PROGRESS
            </div>
            <h3 style={{ fontSize: '1.2rem', fontWeight: '700' }}>{activeSession.lotName} — Slot {activeSession.slotNumber}</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginTop: '0.25rem' }}>
              Vehicle: <strong>{activeSession.licensePlate}</strong> | Check-in: {new Date(activeSession.entryTime).toLocaleTimeString()}
            </p>
          </div>
          <Link to="/active-session" className="btn-primary" style={{ textDecoration: 'none' }}>
            Gate Exit Checkout <ArrowRight size={18} />
          </Link>
        </div>
      )}

      {/* Quick Overview Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1.5rem', marginBottom: '2.5rem' }}>
        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: '600' }}>Active Bookings</span>
            <div style={{ background: 'rgba(59, 130, 246, 0.2)', padding: '0.5rem', borderRadius: '10px', color: 'var(--primary)' }}>
              <Ticket size={20} />
            </div>
          </div>
          <div style={{ fontSize: '2rem', fontWeight: '800' }}>{confirmedReservations.length}</div>
          <Link to="/my-reservations" style={{ color: 'var(--primary)', fontSize: '0.85rem', textDecoration: 'none', fontWeight: '600', display: 'inline-block', marginTop: '0.5rem' }}>
            View Bookings & Passes →
          </Link>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: '600' }}>Registered Vehicles</span>
            <div style={{ background: 'rgba(16, 185, 129, 0.2)', padding: '0.5rem', borderRadius: '10px', color: 'var(--accent)' }}>
              <Car size={20} />
            </div>
          </div>
          <div style={{ fontSize: '2rem', fontWeight: '800' }}>{vehicles.length}</div>
          <Link to="/profile" style={{ color: 'var(--accent)', fontSize: '0.85rem', textDecoration: 'none', fontWeight: '600', display: 'inline-block', marginTop: '0.5rem' }}>
            Manage Vehicles →
          </Link>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem', fontWeight: '600' }}>Total Bookings</span>
            <div style={{ background: 'rgba(245, 158, 11, 0.2)', padding: '0.5rem', borderRadius: '10px', color: 'var(--warning)' }}>
              <CheckCircle size={20} />
            </div>
          </div>
          <div style={{ fontSize: '2rem', fontWeight: '800' }}>{reservations.length}</div>
          <Link to="/history" style={{ color: 'var(--warning)', fontSize: '0.85rem', textDecoration: 'none', fontWeight: '600', display: 'inline-block', marginTop: '0.5rem' }}>
            View History →
          </Link>
        </div>
      </div>

      {/* Quick Action Navigation Buttons */}
      <div className="glass-panel" style={{ padding: '2rem' }}>
        <h3 style={{ fontSize: '1.15rem', fontWeight: '700', marginBottom: '1.25rem' }}>Quick Actions</h3>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <Link to="/lots" className="btn-primary" style={{ textDecoration: 'none' }}>
            <MapPin size={18} /> Find & Reserve Parking
          </Link>
          <Link to="/my-reservations" className="btn-secondary" style={{ textDecoration: 'none' }}>
            <Ticket size={18} /> My Digital Tickets
          </Link>
          <Link to="/profile" className="btn-secondary" style={{ textDecoration: 'none' }}>
            <Car size={18} /> Add New Vehicle
          </Link>
        </div>
      </div>
    </div>
  );
};

export default UserDashboard;
