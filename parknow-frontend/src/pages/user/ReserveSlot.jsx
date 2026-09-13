import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../services/api';
import SlotGrid from '../../components/slots/SlotGrid';
import { Car, Calendar, Clock, AlertCircle, CheckCircle2, ShieldCheck } from 'lucide-react';

const vehicleTypes = [
  { label: 'Standard Car', value: 'CAR' },
  { label: 'SUV / Crossover', value: 'SUV' },
  { label: 'Two Wheeler / Bike', value: 'TWO_WHEELER' },
  { label: 'EV / Electric Vehicle', value: 'ELECTRIC_VEHICLE' },
  { label: 'Truck / Large', value: 'TRUCK' },
];

const ReserveSlot = () => {
  const { lotId } = useParams();
  const navigate = useNavigate();

  const [lot, setLot] = useState(null);
  const [slots, setSlots] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState('');
  const [selectedVehicleType, setSelectedVehicleType] = useState('CAR');
  const [selectedSlot, setSelectedSlot] = useState(null);

  // Time picker state
  const now = new Date();
  const defaultStart = new Date(now.getTime() + 10 * 60000).toISOString().slice(0, 16);
  const defaultEnd = new Date(now.getTime() + 130 * 60000).toISOString().slice(0, 16);

  const [startTime, setStartTime] = useState(defaultStart);
  const [endTime, setEndTime] = useState(defaultEnd);

  // New vehicle form state
  const [newPlate, setNewPlate] = useState('');
  const [showAddVehicle, setShowAddVehicle] = useState(false);

  const [loading, setLoading] = useState(true);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchLotAndSlots();
    fetchUserVehicles();
  }, [lotId, selectedVehicleType]);

  const fetchLotAndSlots = async () => {
    setLoading(true);
    try {
      const lotRes = await api.get(`/parking-lots/${lotId}`);
      if (lotRes.success) setLot(lotRes.data);

      const slotsRes = await api.get(`/parking-slots/lot/${lotId}`);
      if (slotsRes.success) setSlots(slotsRes.data);
    } catch (err) {
      setError(err.message || 'Failed to load parking lot details');
    } finally {
      setLoading(false);
    }
  };

  const fetchUserVehicles = async () => {
    try {
      const res = await api.get('/users/vehicles');
      if (res.success && res.data.length > 0) {
        setVehicles(res.data);
        setSelectedVehicleId(res.data[0].id);
        setSelectedVehicleType(res.data[0].vehicleType);
      }
    } catch (err) {
      console.error('Failed to load user vehicles:', err.message);
    }
  };

  const handleAddVehicle = async (e) => {
    e.preventDefault();
    if (!newPlate) return;
    try {
      const res = await api.post('/users/vehicles', {
        licensePlate: newPlate,
        vehicleType: selectedVehicleType,
      });
      if (res.success) {
        setVehicles([...vehicles, res.data]);
        setSelectedVehicleId(res.data.id);
        setNewPlate('');
        setShowAddVehicle(false);
      }
    } catch (err) {
      setError(err.message || 'Failed to add vehicle');
    }
  };

  const handleBookSlot = async () => {
    if (!selectedVehicleId) {
      setError('Please register and select a vehicle to proceed with reservation.');
      return;
    }
    setError('');
    setBookingLoading(true);

    try {
      const payload = {
        lotId: parseInt(lotId),
        vehicleId: parseInt(selectedVehicleId),
        vehicleType: selectedVehicleType,
        startTime: new Date(startTime).toISOString(),
        endTime: new Date(endTime).toISOString(),
      };

      const res = await api.post('/reservations', payload);
      if (res.success && res.data?.ticketCode) {
        navigate(`/reservation-ticket/${res.data.ticketCode}`);
      } else {
        navigate('/my-reservations');
      }
    } catch (err) {
      setError(err.message || 'Reservation failed.');
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) return <div className="container" style={{ textAlign: 'center', padding: '4rem' }}>Loading lot configuration...</div>;

  return (
    <div className="container">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '800' }}>{lot?.name}</h1>
        <p style={{ color: 'var(--text-muted)' }}>{lot?.address}, {lot?.city}</p>
      </div>

      {error && (
        <div style={{ background: 'rgba(239, 68, 68, 0.15)', border: '1px solid rgba(239, 68, 68, 0.3)', color: '#f87171', padding: '1rem', borderRadius: '12px', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={18} /> {error}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: '2rem' }}>
        {/* Left Column: Interactive Slot Topology Grid */}
        <div>
          <div className="glass-panel" style={{ padding: '1.5rem', marginBottom: '1.5rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '1rem' }}>Filter Slot Matrix by Vehicle Type</h3>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              {vehicleTypes.map((vt) => (
                <button
                  key={vt.value}
                  onClick={() => setSelectedVehicleType(vt.value)}
                  className={selectedVehicleType === vt.value ? 'btn-primary' : 'btn-secondary'}
                  style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
                >
                  {vt.label}
                </button>
              ))}
            </div>
          </div>

          <h3 style={{ fontSize: '1.2rem', fontWeight: '700', marginBottom: '1rem' }}>Available Slots Topology</h3>
          <SlotGrid
            slots={slots.filter((s) => s.vehicleType === selectedVehicleType)}
            selectedSlotId={selectedSlot?.id}
            onSelectSlot={(slot) => setSelectedSlot(slot)}
          />
        </div>

        {/* Right Column: Reservation Configurator Panel */}
        <div>
          <div className="glass-panel" style={{ padding: '2rem', position: 'sticky', top: '100px' }}>
            <h3 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
              Booking Summary
            </h3>

            {/* Vehicle Selection */}
            <div style={{ marginBottom: '1.25rem' }}>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.4rem', color: 'var(--text-muted)' }}>
                Select Vehicle
              </label>
              {vehicles.length > 0 ? (
                <select
                  value={selectedVehicleId}
                  onChange={(e) => {
                    setSelectedVehicleId(e.target.value);
                    const v = vehicles.find((veh) => veh.id === parseInt(e.target.value));
                    if (v) setSelectedVehicleType(v.vehicleType);
                  }}
                  className="form-input"
                  style={{ background: '#0f172a' }}
                >
                  {vehicles.map((v) => (
                    <option key={v.id} value={v.id}>
                      {v.licensePlate} ({v.vehicleType})
                    </option>
                  ))}
                </select>
              ) : (
                <p style={{ fontSize: '0.85rem', color: '#f87171' }}>No registered vehicle found.</p>
              )}

              {!showAddVehicle ? (
                <button onClick={() => setShowAddVehicle(true)} className="btn-secondary" style={{ width: '100%', marginTop: '0.5rem', padding: '0.4rem', fontSize: '0.8rem' }}>
                  + Add Vehicle License Plate
                </button>
              ) : (
                <form onSubmit={handleAddVehicle} style={{ marginTop: '0.75rem', display: 'flex', gap: '0.5rem' }}>
                  <input
                    type="text"
                    required
                    placeholder="e.g. NY-789-XY"
                    className="form-input"
                    value={newPlate}
                    onChange={(e) => setNewPlate(e.target.value)}
                  />
                  <button type="submit" className="btn-primary" style={{ padding: '0.4rem 0.8rem' }}>Save</button>
                </form>
              )}
            </div>

            {/* Time Window Pickers */}
            <div style={{ marginBottom: '1.25rem' }}>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.4rem', color: 'var(--text-muted)' }}>Start Time</label>
              <input
                type="datetime-local"
                className="form-input"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
              />
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: '600', marginBottom: '0.4rem', color: 'var(--text-muted)' }}>End Time</label>
              <input
                type="datetime-local"
                className="form-input"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
              />
            </div>

            <div style={{ background: 'rgba(59, 130, 246, 0.1)', border: '1px solid rgba(59, 130, 246, 0.2)', padding: '1rem', borderRadius: '12px', marginBottom: '1.5rem' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Optimal Allocation Algorithm</div>
              <div style={{ fontSize: '0.85rem', fontWeight: '600', color: '#60a5fa', marginTop: '0.2rem' }}>
                PriorityQueue Min-Heap Allocator will assign the nearest available floor slot automatically.
              </div>
            </div>

            <button
              onClick={handleBookSlot}
              disabled={bookingLoading}
              className="btn-primary"
              style={{ width: '100%', justifyContent: 'center', padding: '0.9rem', fontSize: '1rem' }}
            >
              {bookingLoading ? 'Processing Lock...' : 'Confirm & Generate Ticket'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReserveSlot;
