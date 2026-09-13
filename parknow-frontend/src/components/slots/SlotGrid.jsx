import React from 'react';
import { ShieldAlert, Car, Bike, Zap, Truck } from 'lucide-react';

const getVehicleIcon = (type) => {
  switch (type) {
    case 'TWO_WHEELER': return <Bike size={18} />;
    case 'ELECTRIC_VEHICLE': return <Zap size={18} />;
    case 'TRUCK': return <Truck size={18} />;
    default: return <Car size={18} />;
  }
};

const SlotGrid = ({ slots, selectedSlotId, onSelectSlot }) => {
  if (!slots || slots.length === 0) {
    return (
      <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        No parking slots defined for this lot yet.
      </div>
    );
  }

  // Group slots by floor number
  const floors = slots.reduce((acc, slot) => {
    const f = slot.floorNumber || 1;
    if (!acc[f]) acc[f] = [];
    acc[f].push(slot);
    return acc;
  }, {});

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {Object.keys(floors).sort().map((floorNum) => (
        <div key={floorNum} className="glass-panel" style={{ padding: '1.5rem' }}>
          <h4 style={{ marginBottom: '1rem', color: '#9ca3af', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            Floor Level {floorNum}
          </h4>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(130px, 1fr))', gap: '1rem' }}>
            {floors[floorNum].map((slot) => {
              const isAvailable = slot.status === 'AVAILABLE';
              const isSelected = selectedSlotId === slot.id;

              let borderStyle = '1px solid var(--border-color)';
              let bgStyle = 'rgba(15, 23, 42, 0.5)';
              let badgeClass = 'badge-available';

              if (slot.status === 'RESERVED') {
                badgeClass = 'badge-reserved';
              } else if (slot.status === 'OCCUPIED') {
                badgeClass = 'badge-occupied';
              } else if (slot.status === 'MAINTENANCE') {
                badgeClass = 'badge-maintenance';
              }

              if (isSelected) {
                borderStyle = '2px solid #3b82f6';
                bgStyle = 'rgba(59, 130, 246, 0.25)';
              }

              return (
                <button
                  key={slot.id}
                  disabled={!isAvailable}
                  onClick={() => onSelectSlot && onSelectSlot(slot)}
                  style={{
                    border: borderStyle,
                    background: bgStyle,
                    borderRadius: '12px',
                    padding: '1rem 0.75rem',
                    textAlign: 'center',
                    cursor: isAvailable ? 'pointer' : 'not-allowed',
                    opacity: isAvailable ? 1 : 0.6,
                    transition: 'all 0.2s ease',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: '0.4rem',
                  }}
                >
                  <div style={{ color: isSelected ? '#60a5fa' : 'var(--text-main)' }}>
                    {getVehicleIcon(slot.vehicleType)}
                  </div>
                  <span style={{ fontWeight: '700', fontSize: '1rem' }}>{slot.slotNumber}</span>
                  <span className={`badge ${badgeClass}`} style={{ fontSize: '0.65rem', padding: '0.15rem 0.5rem' }}>
                    {slot.status}
                  </span>
                </button>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
};

export default SlotGrid;
