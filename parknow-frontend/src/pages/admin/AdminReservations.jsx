import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';
import EmptyState from '../../components/common/EmptyState';
import ConfirmationModal from '../../components/common/ConfirmationModal';
import { Ticket, Filter, ChevronLeft, ChevronRight, XCircle } from 'lucide-react';

const AdminReservations = () => {
  const [pagedData, setPagedData] = useState(null);
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Cancel modal state
  const [selectedCancelId, setSelectedCancelId] = useState(null);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    fetchReservations(page, statusFilter);
  }, [page, statusFilter]);

  const fetchReservations = async (pageNo, status) => {
    setLoading(true);
    setError('');
    try {
      let url = `/admin/reservations?page=${pageNo}&size=10`;
      if (status) url += `&status=${status}`;
      const res = await api.get(url);
      setPagedData(res.data);
    } catch (err) {
      setError(err.message || 'Failed to fetch reservations');
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
      fetchReservations(page, statusFilter);
    } catch (err) {
      setError(err.message || 'Failed to cancel reservation');
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading reservation records..." />;

  const reservations = pagedData?.content || [];

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Ticket color="var(--primary)" /> System Reservations Audit
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>Paginated audit log of all system reservations across facilities</p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <Filter size={18} color="var(--text-muted)" />
          <select
            className="form-input"
            value={statusFilter}
            onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
            style={{ width: '180px' }}
          >
            <option value="">All Statuses</option>
            <option value="CONFIRMED">CONFIRMED</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </div>
      </div>

      {error && <ErrorBanner message={error} onClose={() => setError('')} />}

      {reservations.length === 0 ? (
        <EmptyState title="No Reservations Found" description="No reservation records match the selected filter." icon={Ticket} />
      ) : (
        <div className="glass-panel" style={{ overflow: 'hidden', marginBottom: '1.5rem' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ background: 'rgba(255,255,255,0.05)', borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '1rem' }}>Ticket Code</th>
                <th style={{ padding: '1rem' }}>Facility / Slot</th>
                <th style={{ padding: '1rem' }}>License Plate</th>
                <th style={{ padding: '1rem' }}>Start Time</th>
                <th style={{ padding: '1rem' }}>Status</th>
                <th style={{ padding: '1rem', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {reservations.map((r) => (
                <tr key={r.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '1rem', fontWeight: '700', color: 'var(--primary)' }}>{r.ticketCode}</td>
                  <td style={{ padding: '1rem' }}>{r.lotName} (Slot {r.slotNumber})</td>
                  <td style={{ padding: '1rem' }}>{r.licensePlate} ({r.vehicleType})</td>
                  <td style={{ padding: '1rem', color: 'var(--text-muted)' }}>{new Date(r.startTime).toLocaleString()}</td>
                  <td style={{ padding: '1rem' }}>
                    <span className={`badge ${
                      r.status === 'CONFIRMED' ? 'badge-reserved' :
                      r.status === 'ACTIVE' ? 'badge-occupied' :
                      r.status === 'COMPLETED' ? 'badge-available' : 'badge-maintenance'
                    }`}>
                      {r.status}
                    </span>
                  </td>
                  <td style={{ padding: '1rem', textAlign: 'right' }}>
                    {(r.status === 'CONFIRMED' || r.status === 'PENDING') && (
                      <button
                        onClick={() => setSelectedCancelId(r.id)}
                        className="btn-danger"
                        style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }}
                      >
                        <XCircle size={14} /> Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination Controls */}
      {pagedData && pagedData.totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem' }}>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
            Page {pagedData.pageNumber + 1} of {pagedData.totalPages} ({pagedData.totalElements} Total Records)
          </span>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button
              disabled={pagedData.pageNumber === 0}
              onClick={() => setPage(page - 1)}
              className="btn-secondary"
              style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}
            >
              <ChevronLeft size={16} /> Prev
            </button>
            <button
              disabled={pagedData.isLast}
              onClick={() => setPage(page + 1)}
              className="btn-secondary"
              style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}
            >
              Next <ChevronRight size={16} />
            </button>
          </div>
        </div>
      )}

      {/* Confirmation Modal */}
      <ConfirmationModal
        isOpen={Boolean(selectedCancelId)}
        title="Admin Cancel Reservation"
        message="Are you sure you want to cancel this reservation as Administrator? The slot will be released."
        confirmText="Yes, Cancel Booking"
        onConfirm={handleConfirmCancel}
        onCancel={() => setSelectedCancelId(null)}
        isDanger={true}
        isLoading={cancelling}
      />
    </div>
  );
};

export default AdminReservations;
