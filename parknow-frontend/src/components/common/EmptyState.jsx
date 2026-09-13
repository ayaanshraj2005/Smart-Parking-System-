import React from 'react';
import { Inbox } from 'lucide-react';
import { Link } from 'react-router-dom';

const EmptyState = ({ title = 'No items found', description = 'There are currently no records to display.', icon: Icon = Inbox, actionLink, actionText }) => {
  return (
    <div className="glass-panel" style={{
      padding: '3rem 2rem',
      textAlign: 'center',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      margin: '1.5rem 0'
    }}>
      <div style={{
        background: 'rgba(255, 255, 255, 0.05)',
        padding: '1.25rem',
        borderRadius: '50%',
        color: 'var(--text-muted)',
        marginBottom: '1rem'
      }}>
        <Icon size={40} />
      </div>

      <h3 style={{ fontSize: '1.2rem', fontWeight: '700', marginBottom: '0.5rem' }}>{title}</h3>
      <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', maxWidth: '400px', marginBottom: actionLink ? '1.5rem' : '0' }}>
        {description}
      </p>

      {actionLink && actionText && (
        <Link to={actionLink} className="btn-primary" style={{ textDecoration: 'none' }}>
          {actionText}
        </Link>
      )}
    </div>
  );
};

export default EmptyState;
