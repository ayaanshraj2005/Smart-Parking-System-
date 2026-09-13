import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

// Public Pages
import Landing from '../pages/Landing';
import Login from '../pages/user/Login';
import Register from '../pages/user/Register';

// User Pages
import UserDashboard from '../pages/user/UserDashboard';
import SearchLots from '../pages/user/SearchLots';
import LotDetails from '../pages/user/LotDetails';
import ReserveSlot from '../pages/user/ReserveSlot';
import ReservationConfirmation from '../pages/user/ReservationConfirmation';
import MyReservations from '../pages/user/MyReservations';
import ActiveSession from '../pages/user/ActiveSession';
import ParkingHistory from '../pages/user/ParkingHistory';
import Payments from '../pages/user/Payments';
import Profile from '../pages/user/Profile';

// Admin Pages
import AdminDashboard from '../pages/admin/AdminDashboard';
import AdminLots from '../pages/admin/AdminLots';
import AdminSlots from '../pages/admin/AdminSlots';
import AdminReservations from '../pages/admin/AdminReservations';
import AdminActiveSessions from '../pages/admin/AdminActiveSessions';
import AdminUsers from '../pages/admin/AdminUsers';
import AdminPricing from '../pages/admin/AdminPricing';
import AdminReports from '../pages/admin/AdminReports';

import { ProtectedRoute, AdminRoute } from './Guards';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/lots" element={<SearchLots />} />
      <Route path="/lots/:id" element={<LotDetails />} />

      {/* User Protected Routes */}
      <Route path="/dashboard" element={<ProtectedRoute><UserDashboard /></ProtectedRoute>} />
      <Route path="/reserve/:lotId" element={<ProtectedRoute><ReserveSlot /></ProtectedRoute>} />
      <Route path="/reservation-ticket/:ticketCode" element={<ProtectedRoute><ReservationConfirmation /></ProtectedRoute>} />
      <Route path="/my-reservations" element={<ProtectedRoute><MyReservations /></ProtectedRoute>} />
      <Route path="/active-session" element={<ProtectedRoute><ActiveSession /></ProtectedRoute>} />
      <Route path="/history" element={<ProtectedRoute><ParkingHistory /></ProtectedRoute>} />
      <Route path="/payments" element={<ProtectedRoute><Payments /></ProtectedRoute>} />
      <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />

      {/* Admin Protected Routes */}
      <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
      <Route path="/admin/lots" element={<AdminRoute><AdminLots /></AdminRoute>} />
      <Route path="/admin/slots" element={<AdminRoute><AdminSlots /></AdminRoute>} />
      <Route path="/admin/reservations" element={<AdminRoute><AdminReservations /></AdminRoute>} />
      <Route path="/admin/active-sessions" element={<AdminRoute><AdminActiveSessions /></AdminRoute>} />
      <Route path="/admin/users" element={<AdminRoute><AdminUsers /></AdminRoute>} />
      <Route path="/admin/pricing" element={<AdminRoute><AdminPricing /></AdminRoute>} />
      <Route path="/admin/reports" element={<AdminRoute><AdminReports /></AdminRoute>} />

      {/* Fallback Catch-all Route */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
