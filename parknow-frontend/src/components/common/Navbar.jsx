import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Car, ShieldCheck, Ticket, Calendar, LogOut, User, DollarSign, LayoutDashboard, Building2, Layers, Users, BarChart3, Settings } from 'lucide-react';

const Navbar = () => {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [showAdminMenu, setShowAdminMenu] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isPathActive = (path) => location.pathname === path;

  return (
    <header className="glass-nav">
      <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1rem 1.5rem' }}>
        {/* Brand Logo */}
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', textDecoration: 'none', color: '#fff' }}>
          <div style={{ background: 'linear-gradient(135deg, #3b82f6, #10b981)', padding: '0.6rem', borderRadius: '12px', display: 'flex' }}>
            <Car size={24} color="#fff" />
          </div>
          <div>
            <span style={{ fontSize: '1.4rem', fontWeight: '800', letterSpacing: '-0.5px' }}>Park<span style={{ color: '#3b82f6' }}>Now</span></span>
            <span style={{ fontSize: '0.7rem', display: 'block', color: 'var(--text-muted)', fontWeight: '600', marginTop: '-4px' }}>SMART PARKING PLATFORM</span>
          </div>
        </Link>

        {/* Navigation Links */}
        <nav style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          <Link to="/lots" style={{ color: isPathActive('/lots') ? 'var(--primary)' : 'var(--text-main)', textDecoration: 'none', fontWeight: '600', fontSize: '0.9rem' }}>
            Find Parking
          </Link>

          {user && (
            <>
              <Link to="/dashboard" style={{ color: isPathActive('/dashboard') ? 'var(--primary)' : 'var(--text-main)', textDecoration: 'none', fontWeight: '500', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <LayoutDashboard size={16} /> Dashboard
              </Link>

              <Link to="/my-reservations" style={{ color: isPathActive('/my-reservations') ? 'var(--primary)' : 'var(--text-main)', textDecoration: 'none', fontWeight: '500', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <Ticket size={16} /> My Bookings
              </Link>

              <Link to="/active-session" style={{ color: isPathActive('/active-session') ? 'var(--primary)' : 'var(--text-main)', textDecoration: 'none', fontWeight: '500', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <Calendar size={16} /> Active Session
              </Link>

              <Link to="/payments" style={{ color: isPathActive('/payments') ? 'var(--primary)' : 'var(--text-main)', textDecoration: 'none', fontWeight: '500', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <DollarSign size={16} /> Payments
              </Link>
            </>
          )}

          {/* Admin Navigation Dropdown */}
          {isAdmin && (
            <div style={{ position: 'relative' }}>
              <button 
                onClick={() => setShowAdminMenu(!showAdminMenu)}
                className="badge badge-reserved"
                style={{ cursor: 'pointer', border: 'none', padding: '0.4rem 0.8rem', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}
              >
                <ShieldCheck size={16} /> Admin Portal
              </button>

              {showAdminMenu && (
                <div 
                  className="glass-panel" 
                  onClick={() => setShowAdminMenu(false)}
                  style={{
                    position: 'absolute',
                    top: 'calc(100% + 0.75rem)',
                    right: 0,
                    width: '210px',
                    padding: '0.5rem',
                    zIndex: 1000,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.25rem',
                    boxShadow: '0 10px 30px rgba(0,0,0,0.5)'
                  }}
                >
                  <Link to="/admin" style={{ padding: '0.5rem 0.75rem', color: '#fff', textDecoration: 'none', fontSize: '0.85rem', borderRadius: '6px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <LayoutDashboard size={14} /> Admin Overview
                  </Link>
                  <Link to="/admin/lots" style={{ padding: '0.5rem 0.75rem', color: '#fff', textDecoration: 'none', fontSize: '0.85rem', borderRadius: '6px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Building2 size={14} /> Parking Facilities
                  </Link>
                  <Link to="/admin/slots" style={{ padding: '0.5rem 0.75rem', color: '#fff', textDecoration: 'none', fontSize: '0.85rem', borderRadius: '6px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Layers size={14} /> Slot Management
                  </Link>
                  <Link to="/admin/reservations" style={{ padding: '0.5rem 0.75rem', color: '#fff', textDecoration: 'none', fontSize: '0.85rem', borderRadius: '6px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Ticket size={14} /> Reservations
                  </Link>
                  <Link to="/admin/active-sessions" style={{ padding: '0.5rem 0.75rem', color: '#fff', textDecoration: 'none', fontSize: '0.85rem', borderRadius: '6px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Calendar size={14} /> Active Sessions
                  </Link>
                  <Link to="/admin/users" style={{ padding: '0.5rem 0.75rem', color: '#fff', textDecoration: 'none', fontSize: '0.85rem', borderRadius: '6px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Users size={14} /> User Accounts
                  </Link>
                  <Link to="/admin/pricing" style={{ padding: '0.5rem 0.75rem', color: '#fff', textDecoration: 'none', fontSize: '0.85rem', borderRadius: '6px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Settings size={14} /> Pricing Rules
                  </Link>
                  <Link to="/admin/reports" style={{ padding: '0.5rem 0.75rem', color: '#fff', textDecoration: 'none', fontSize: '0.85rem', borderRadius: '6px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <BarChart3 size={14} /> Reports
                  </Link>
                </div>
              )}
            </div>
          )}
        </nav>

        {/* User Account Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <Link to="/profile" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#fff', textDecoration: 'none' }}>
                <div style={{ background: 'rgba(255,255,255,0.1)', padding: '0.4rem', borderRadius: '50%', display: 'flex' }}>
                  <User size={16} />
                </div>
                <span style={{ fontWeight: '600', fontSize: '0.85rem' }}>{user.fullName}</span>
              </Link>
              <button onClick={handleLogout} className="btn-secondary" style={{ padding: '0.45rem 0.85rem', fontSize: '0.8rem' }}>
                <LogOut size={14} /> Logout
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <Link to="/login" className="btn-secondary" style={{ textDecoration: 'none', padding: '0.45rem 0.9rem', fontSize: '0.85rem' }}>
                Login
              </Link>
              <Link to="/register" className="btn-primary" style={{ textDecoration: 'none', padding: '0.45rem 1.1rem', fontSize: '0.85rem' }}>
                Register
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;
