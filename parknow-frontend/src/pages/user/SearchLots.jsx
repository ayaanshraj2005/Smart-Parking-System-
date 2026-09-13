import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';
import { Search, MapPin, Car, ShieldCheck, ArrowRight } from 'lucide-react';

const SearchLots = () => {
  const [lots, setLots] = useState([]);
  const [city, setCity] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLots();
  }, []);

  const fetchLots = async (searchCity = '') => {
    setLoading(true);
    try {
      const endpoint = searchCity ? `/parking-lots?city=${encodeURIComponent(searchCity)}` : '/parking-lots';
      const res = await api.get(endpoint);
      if (res.success) {
        setLots(res.data);
      }
    } catch (err) {
      console.error('Failed to load lots:', err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchLots(city);
  };

  return (
    <div className="container">
      {/* Hero Section */}
      <div className="glass-panel" style={{ padding: '3rem 2rem', textAlign: 'center', marginBottom: '2.5rem', background: 'linear-gradient(135deg, rgba(30, 58, 138, 0.4) 0%, rgba(16, 185, 129, 0.15) 100%)' }}>
        <h1 style={{ fontSize: '2.5rem', fontWeight: '800', marginBottom: '0.75rem', tracking: '-1px' }}>
          Find & Reserve Your <span style={{ color: '#60a5fa' }}>Parking Spot</span>
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem', maxWidth: '600px', margin: '0 auto 2rem' }}>
          Guaranteed parking with PriorityQueue intelligent allocation, digital passes, and instant dynamic checkout.
        </p>

        <form onSubmit={handleSearch} style={{ maxWidth: '540px', margin: '0 auto', display: 'flex', gap: '0.75rem' }}>
          <div style={{ position: 'relative', flex: 1 }}>
            <input
              type="text"
              className="form-input"
              placeholder="Search by city (e.g. Metropolis)..."
              value={city}
              onChange={(e) => setCity(e.target.value)}
              style={{ paddingLeft: '2.5rem', paddingRight: '1rem', height: '50px', fontSize: '1rem' }}
            />
            <MapPin size={20} color="var(--text-muted)" style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)' }} />
          </div>
          <button type="submit" className="btn-primary" style={{ height: '50px', padding: '0 1.5rem' }}>
            <Search size={20} /> Search
          </button>
        </form>
      </div>

      {/* Parking Lots List */}
      <h2 style={{ fontSize: '1.4rem', fontWeight: '700', marginBottom: '1.25rem' }}>
        Available Parking Locations ({lots.length})
      </h2>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>Loading parking lots...</div>
      ) : lots.length === 0 ? (
        <div className="glass-panel" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
          No active parking lots found matching your search location.
        </div>
      ) : (
        <div className="grid-cols-3">
          {lots.map((lot) => {
            const occPercent = Math.round(((lot.totalCapacity - lot.availableCapacity) / lot.totalCapacity) * 100);
            return (
              <div key={lot.id} className="glass-panel animate-fade-in" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                    <h3 style={{ fontSize: '1.2rem', fontWeight: '700' }}>{lot.name}</h3>
                    <span className="badge badge-available">
                      <ShieldCheck size={14} /> Verified
                    </span>
                  </div>

                  <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '1.25rem' }}>
                    <MapPin size={16} /> {lot.address}, {lot.city}
                  </p>

                  <div style={{ background: 'rgba(15, 23, 42, 0.6)', padding: '1rem', borderRadius: '12px', marginBottom: '1.5rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.4rem' }}>
                      <span style={{ color: 'var(--text-muted)' }}>Available Capacity</span>
                      <strong style={{ color: lot.availableCapacity > 0 ? '#34d399' : '#f87171' }}>
                        {lot.availableCapacity} / {lot.totalCapacity} Slots
                      </strong>
                    </div>
                    <div style={{ background: 'rgba(255,255,255,0.1)', height: '8px', borderRadius: '4px', overflow: 'hidden' }}>
                      <div style={{ width: `${100 - occPercent}%`, background: lot.availableCapacity > 0 ? '#34d399' : '#ef4444', height: '100%', borderRadius: '4px', transition: 'width 0.3s ease' }} />
                    </div>
                  </div>
                </div>

                <Link to={`/reserve/${lot.id}`} className="btn-primary" style={{ width: '100%', justifyContent: 'center', textDecoration: 'none' }}>
                  Reserve Parking Slot <ArrowRight size={18} />
                </Link>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default SearchLots;
