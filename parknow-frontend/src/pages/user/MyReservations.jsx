import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import DigitalTicketCard from '../../components/tickets/DigitalTicketCard';
import ConfirmationModal from '../../components/common/ConfirmationModal';
import EmptyState from '../../components/common/EmptyState';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';
import { Ticket } from 'lucide-react';

const MyReservations = () => {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedCancelId, setSelectedCancelId] = useState(null);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    fetchReservations();
  }, []);

  const fetchReservations = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/reservations/my');
      setReservations(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load reservations');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmCancel = async () => {
    if (!selectedCancelId) return;
    setCancelling(true);
    try {
      await api.delete(`/reservations/${selectedCancelId}`);
      setSelectedCancelId(null);
      fetchReservations();
    } catch (err) {
      setError(err.message || 'Failed to cancel reservation');
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading your digital passes..." />;

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <Ticket color="var(--primary)" /> My Digital Parking Passes
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>View your active, completed, and upcoming parking reservations</p>
      </div>

      {error && <ErrorBanner message={error} onClose={() => setError('')} />}

      {reservations.length === 0 ? (
        <EmptyState 
          title="No Digital Parking Passes" 
          description="You currently have no active or historical parking reservations." 
          icon={Ticket}
          actionLink="/lots" 
          actionText="Find & Reserve Parking" 
        />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {reservations.map((res) => (
            <DigitalTicketCard
              key={res.id}
              reservation={res}
              onCancel={(id) => setSelectedCancelId(id)}
            />
          ))}
        </div>
      )}

      {/* Confirmation Modal */}
      <ConfirmationModal
        isOpen={Boolean(selectedCancelId)}
        title="Cancel Reservation"
        message="Are you sure you want to cancel this reservation? The allocated slot will be released back to the available pool."
        confirmText="Yes, Cancel Booking"
        onConfirm={handleConfirmCancel}
        onCancel={() => setSelectedCancelId(null)}
        isDanger={true}
        isLoading={cancelling}
      />
    </div>
  );
};

export default MyReservations;
