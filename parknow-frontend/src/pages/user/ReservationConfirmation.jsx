import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../../services/api';
import DigitalTicketCard from '../../components/tickets/DigitalTicketCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';
import { ArrowLeft, CheckCircle } from 'lucide-react';

const ReservationConfirmation = () => {
  const { ticketCode } = useParams();
  const [reservation, setReservation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchReservation();
  }, [ticketCode]);

  const fetchReservation = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/reservations/my');
      const found = (res.data || []).find(r => r.ticketCode === ticketCode);
      if (found) {
        setReservation(found);
      } else {
        setError('Reservation ticket not found.');
      }
    } catch (err) {
      setError(err.message || 'Failed to retrieve reservation pass.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner message="Generating digital reservation pass..." />;
  if (error) return <div className="container"><ErrorBanner message={error} /></div>;
  if (!reservation) return null;

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem', maxWidth: '650px' }}>
      <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
        <div style={{ background: 'rgba(16, 185, 129, 0.2)', padding: '1rem', borderRadius: '50%', display: 'inline-flex', marginBottom: '1rem', color: 'var(--accent)' }}>
          <CheckCircle size={48} />
        </div>
        <h1 style={{ fontSize: '2rem', fontWeight: '800' }}>Reservation Confirmed!</h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginTop: '0.25rem' }}>
          Your digital parking pass has been generated and slot reserved.
        </p>
      </div>

      <DigitalTicketCard reservation={reservation} />

      <div style={{ marginTop: '2rem', display: 'flex', justifyContent: 'center', gap: '1rem' }}>
        <Link to="/my-reservations" className="btn-secondary" style={{ textDecoration: 'none' }}>
          <ArrowLeft size={18} /> View All Bookings
        </Link>
        <Link to="/active-session" className="btn-primary" style={{ textDecoration: 'none' }}>
          Gate Check-in Portal →
        </Link>
      </div>
    </div>
  );
};

export default ReservationConfirmation;
