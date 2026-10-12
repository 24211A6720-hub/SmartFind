import { Routes, Route, Navigate } from "react-router-dom";
import Layout from "./components/Layout";
import ProtectedRoute from "./routes/ProtectedRoute";

import Landing from "./pages/public/Landing";
import Login from "./pages/public/Login";
import Register from "./pages/public/Register";

import Dashboard from "./pages/user/Dashboard";
import ReportLost from "./pages/user/ReportLost";
import ReportFound from "./pages/user/ReportFound";
import ReportForm from "./pages/user/ReportForm";
import SearchItems from "./pages/user/SearchItems";
import ItemDetails from "./pages/user/ItemDetails";
import PossibleMatches from "./pages/user/PossibleMatches";
import MyReports from "./pages/user/MyReports";
import MyClaims from "./pages/user/MyClaims";
import Notifications from "./pages/user/Notifications";
import Profile from "./pages/user/Profile";

import AdminDashboard from "./pages/admin/AdminDashboard";
import ManageUsers from "./pages/admin/ManageUsers";
import ManageLost from "./pages/admin/ManageLost";
import ManageFound from "./pages/admin/ManageFound";
import ManageClaims from "./pages/admin/ManageClaims";
import ManageMatches from "./pages/admin/ManageMatches";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/report/lost" element={<ReportLost />} />
          <Route path="/report/found" element={<ReportFound />} />
          <Route path="/items/:kind/:id" element={<ItemDetails />} />
          <Route path="/items/:kind/:id/edit" element={<ReportForm />} />
          <Route path="/search" element={<SearchItems />} />
          <Route path="/matches" element={<PossibleMatches />} />
          <Route path="/my-reports" element={<MyReports />} />
          <Route path="/my-claims" element={<MyClaims />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/profile" element={<Profile />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute adminOnly />}>
        <Route element={<Layout />}>
          <Route path="/admin" element={<AdminDashboard />} />
          <Route path="/admin/users" element={<ManageUsers />} />
          <Route path="/admin/lost" element={<ManageLost />} />
          <Route path="/admin/found" element={<ManageFound />} />
          <Route path="/admin/claims" element={<ManageClaims />} />
          <Route path="/admin/matches" element={<ManageMatches />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
