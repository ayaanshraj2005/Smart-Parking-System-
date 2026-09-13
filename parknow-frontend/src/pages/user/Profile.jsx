import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import ConfirmationModal from '../../components/common/ConfirmationModal';
import ErrorBanner from '../../components/common/ErrorBanner';
import { User, Mail, Phone, CheckCircle2, Car, Plus, Trash2 } from 'lucide-react';

const Profile = () => {
  const { user, refreshProfile } = useAuth();
  const [fullName, setFullName] = useState(user?.fullName || '');
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber || '');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Vehicles State
  const [vehicles, setVehicles] = useState([]);
  const [licensePlate, setLicensePlate] = useState('');
  const [vehicleType, setVehicleType] = useState('CAR');
  const [addingVehicle, setAddingVehicle] = useState(false);
  const [deleteVehicleId, setDeleteVehicleId] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    fetchVehicles();
  }, []);

  const fetchVehicles = async () => {
    try {
      const res = await api.get('/users/vehicles');
      setVehicles(res.data || []);
    } catch (err) {
      console.error('Failed to load vehicles:', err);
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');
    try {
      const res = await api.put('/users/me', { fullName, phoneNumber });
      setMessage('Profile updated successfully!');
      if (refreshProfile) refreshProfile();
    } catch (err) {
      setError(err.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  const handleAddVehicle = async (e) => {
    e.preventDefault();
    if (!licensePlate.trim()) return;
    setAddingVehicle(true);
    setError('');
    try {
      await api.post('/users/vehicles', { licensePlate, vehicleType });
      setLicensePlate('');
      setMessage('Vehicle added successfully!');
      fetchVehicles();
    } catch (err) {
      setError(err.message || 'Failed to add vehicle');
    } finally {
      setAddingVehicle(false);
    }
  };

  const handleConfirmDeleteVehicle = async () => {
    if (!deleteVehicleId) return;
    setDeleting(true);
    try {
      await api.delete(`/users/vehicles/${deleteVehicleId}`);
      setDeleteVehicleId(null);
      setMessage('Vehicle removed successfully!');
      fetchVehicles();
    } catch (err) {
      setError(err.message || 'Failed to delete vehicle');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
        {/* User Profile Info */}
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
            <div style={{ background: 'linear-gradient(135deg, #3b82f6, #10b981)', padding: '1rem', borderRadius: '50%' }}>
              <User size={36} color="#fff" />
            </div>
            <div>
              <h2 style={{ fontSize: '1.5rem', fontWeight: '800' }}>{user?.fullName}</h2>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>{user?.email}</p>
              <div style={{ marginTop: '0.4rem', display: 'flex', gap: '0.4rem' }}>
                {user?.roles?.map((r) => (
                  <span key={r} className="badge badge-reserved" style={{ fontSize: '0.7rem' }}>{r}</span>
                ))}
              </div>
            </div>
          </div>

          {message && (
            <div style={{ background: 'rgba(16, 185, 129, 0.15)', border: '1px solid rgba(16, 185, 129, 0.3)', color: '#34d399', padding: '0.75rem 1rem', borderRadius: '10px', fontSize: '0.85rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <CheckCircle2 size={16} /> {message}
            </div>
          )}

          {error && <ErrorBanner message={error} onClose={() => setError('')} />}

          <form onSubmit={handleUpdateProfile} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.4rem', color: 'var(--text-muted)' }}>Full Name</label>
              <input
                type="text"
                required
                className="form-input"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.4rem', color: 'var(--text-muted)' }}>Email Address (Read Only)</label>
              <input
                type="email"
                disabled
                className="form-input"
                value={user?.email || ''}
                style={{ opacity: 0.7, cursor: 'not-allowed' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.4rem', color: 'var(--text-muted)' }}>Phone Number</label>
              <input
                type="tel"
                className="form-input"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
              />
            </div>

            <button type="submit" disabled={loading} className="btn-primary" style={{ marginTop: '0.5rem', justifyContent: 'center' }}>
              {loading ? 'Saving Changes...' : 'Save Profile Changes'}
            </button>
          </form>
        </div>

        {/* Vehicles Management */}
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Car size={22} color="var(--primary)" /> Registered Vehicles ({vehicles.length})
          </h3>

          {/* Add Vehicle Form */}
          <form onSubmit={handleAddVehicle} style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '1.25rem', borderRadius: '12px', marginBottom: '1.5rem', border: '1px solid var(--border-color)' }}>
            <h4 style={{ fontSize: '0.95rem', fontWeight: '600', marginBottom: '0.75rem' }}>Add New Vehicle</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <input
                type="text"
                required
                placeholder="License Plate (e.g. KA-01-AB-1234)"
                className="form-input"
                value={licensePlate}
                onChange={(e) => setLicensePlate(e.target.value)}
              />

              <select
                className="form-input"
                value={vehicleType}
                onChange={(e) => setVehicleType(e.target.value)}
              >
                <option value="CAR">CAR (Four Wheeler)</option>
                <option value="SUV">SUV (Large Vehicle)</option>
                <option value="TWO_WHEELER">TWO_WHEELER (Bike / Scooter)</option>
                <option value="ELECTRIC_VEHICLE">ELECTRIC_VEHICLE (EV)</option>
                <option value="TRUCK">TRUCK</option>
              </select>

              <button type="submit" disabled={addingVehicle} className="btn-primary" style={{ justifyContent: 'center', fontSize: '0.85rem' }}>
                <Plus size={16} /> {addingVehicle ? 'Registering...' : 'Add Vehicle'}
              </button>
            </div>
          </form>

          {/* Vehicle List */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {vehicles.map((v) => (
              <div key={v.id} style={{
                background: 'rgba(15, 23, 42, 0.6)',
                border: '1px solid var(--border-color)',
                padding: '0.85rem 1rem',
                borderRadius: '10px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
              }}>
                <div>
                  <div style={{ fontWeight: '700', fontSize: '1rem' }}>{v.licensePlate}</div>
                  <span className="badge badge-reserved" style={{ fontSize: '0.7rem', marginTop: '0.2rem' }}>{v.vehicleType}</span>
                </div>
                <button
                  type="button"
                  onClick={() => setDeleteVehicleId(v.id)}
                  style={{ background: 'none', border: 'none', color: 'var(--danger)', cursor: 'pointer', padding: '0.4rem' }}
                >
                  <Trash2 size={18} />
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Confirmation Modal */}
      <ConfirmationModal
        isOpen={Boolean(deleteVehicleId)}
        title="Delete Vehicle"
        message="Are you sure you want to remove this vehicle from your account?"
        confirmText="Yes, Delete Vehicle"
        onConfirm={handleConfirmDeleteVehicle}
        onCancel={() => setDeleteVehicleId(null)}
        isDanger={true}
        isLoading={deleting}
      />
    </div>
  );
};

export default Profile;
