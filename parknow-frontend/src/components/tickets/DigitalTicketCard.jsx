import React from 'react';
import { Ticket, MapPin, Calendar, Clock, Car, QrCode, CheckCircle2 } from 'lucide-react';

const DigitalTicketCard = ({ reservation, onCancel }) => {
  if (!reservation) return null;

  return (
    <div className="glass-panel animate-fade-in" style={{ padding: '2rem', border: '1px solid rgba(59, 130, 246, 0.3)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', borderBottom: '1px dashed var(--border-color)', paddingBottom: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{ background: 'rgba(59, 130, 246, 0.2)', padding: '0.6rem', borderRadius: '10px', color: '#60a5fa' }}>
            <Ticket size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.2rem', fontWeight: '700' }}>Digital Parking Ticket</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>CODE: <strong style={{ color: '#60a5fa' }}>{reservation.ticketCode}</strong></p>
          </div>
        </div>
        <span className={`badge ${reservation.status === 'CONFIRMED' ? 'badge-available' : reservation.status === 'ACTIVE' ? 'badge-reserved' : 'badge-maintenance'}`}>
          {reservation.status}
        </span>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem', marginBottom: '1.5rem' }}>
        <div>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <MapPin size={14} /> Location
          </span>
          <p style={{ fontWeight: '600', fontSize: '1rem', marginTop: '0.2rem' }}>{reservation.lotName}</p>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>{reservation.lotAddress}</p>
        </div>

        <div>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <Car size={14} /> Slot & Vehicle
          </span>
          <p style={{ fontWeight: '700', fontSize: '1.1rem', color: '#34d399', marginTop: '0.2rem' }}>
            Slot {reservation.slotNumber} (Floor {reservation.floorNumber})
          </p>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Plate: <strong>{reservation.licensePlate}</strong> ({reservation.vehicleType})</p>
        </div>

        <div>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <Calendar size={14} /> Reserved Time Window
          </span>
          <p style={{ fontWeight: '600', fontSize: '0.9rem', marginTop: '0.2rem' }}>
            {new Date(reservation.startTime).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
          </p>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
            to {new Date(reservation.endTime).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
          </p>
        </div>

        <div>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>Estimated Amount</span>
          <p style={{ fontWeight: '800', fontSize: '1.4rem', color: '#60a5fa', marginTop: '0.2rem' }}>
            ${reservation.estimatedAmount?.toFixed(2)}
          </p>
        </div>
      </div>

      <div style={{ background: 'rgba(15, 23, 42, 0.7)', borderRadius: '12px', padding: '1rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <QrCode size={40} color="#60a5fa" />
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Scan ticket code at security entry gate to start parking session</span>
        </div>
        {reservation.status === 'CONFIRMED' && onCancel && (
          <button onClick={() => onCancel(reservation.id)} className="btn-danger">
            Cancel Booking
          </button>
        )}
      </div>
    </div>
  );
};

export default DigitalTicketCard;
