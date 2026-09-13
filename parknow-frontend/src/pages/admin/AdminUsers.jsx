import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorBanner from '../../components/common/ErrorBanner';
import EmptyState from '../../components/common/EmptyState';
import { Users, Shield, Car, ChevronLeft, ChevronRight } from 'lucide-react';

const AdminUsers = () => {
  const [pagedData, setPagedData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchUsers(page);
  }, [page]);

  const fetchUsers = async (pageNo) => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get(`/admin/users?page=${pageNo}&size=10`);
      setPagedData(res.data);
    } catch (err) {
      setError(err.message || 'Failed to load user management list.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading registered users list..." />;

  const users = pagedData?.content || [];

  return (
    <div className="container animate-fade-in" style={{ paddingBottom: '3rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <Users color="var(--primary)" /> Registered User Accounts
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>Audit user profiles, assigned roles, and registered vehicle counts</p>
      </div>

      {error && <ErrorBanner message={error} onClose={() => setError('')} />}

      {users.length === 0 ? (
        <EmptyState title="No Registered Users" description="No registered users found in database." icon={Users} />
      ) : (
        <div className="glass-panel" style={{ overflow: 'hidden', marginBottom: '1.5rem' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ background: 'rgba(255,255,255,0.05)', borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '1rem' }}>User Full Name</th>
                <th style={{ padding: '1rem' }}>Email Address</th>
                <th style={{ padding: '1rem' }}>Assigned Roles</th>
                <th style={{ padding: '1rem' }}>Registered Vehicles</th>
                <th style={{ padding: '1rem' }}>Registration Date</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '1rem', fontWeight: '700' }}>{u.fullName}</td>
                  <td style={{ padding: '1rem', color: 'var(--primary)' }}>{u.email}</td>
                  <td style={{ padding: '1rem' }}>
                    <div style={{ display: 'flex', gap: '0.35rem', flexWrap: 'wrap' }}>
                      {u.roles?.map((r) => (
                        <span key={r} className={`badge ${r.includes('ADMIN') ? 'badge-maintenance' : 'badge-reserved'}`}>
                          {r}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td style={{ padding: '1rem', fontWeight: '700' }}>
                    <Car size={16} style={{ display: 'inline', verticalAlign: 'middle', marginRight: '0.4rem' }} />
                    {u.vehicleCount} Vehicles
                  </td>
                  <td style={{ padding: '1rem', color: 'var(--text-muted)' }}>
                    {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : 'N/A'}
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
            Page {pagedData.pageNumber + 1} of {pagedData.totalPages} ({pagedData.totalElements} Users Total)
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
    </div>
  );
};

export default AdminUsers;
