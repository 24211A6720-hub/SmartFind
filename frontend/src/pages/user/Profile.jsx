import { useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { userApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, Field, fmtDate } from "../../components/UI";

export default function Profile() {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({ name: user.name, phone: user.phone || "", newPassword: "" });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [busy, setBusy] = useState(false);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (ev) => {
    ev.preventDefault();
    const errs = {};
    if (!form.name.trim()) errs.name = "Name is required";
    if (!/^[0-9+\-\s]{7,15}$/.test(form.phone)) errs.phone = "Enter a valid phone number";
    if (form.newPassword && !/^(?=.*[A-Za-z])(?=.*\d).{8,}$/.test(form.newPassword)) errs.newPassword = "At least 8 characters with a letter and a number";
    setErrors(errs);
    if (Object.keys(errs).length) return;
    setBusy(true); setError(""); setSuccess("");
    try {
      updateUser(await userApi.update(user.id, form));
      setForm({ ...form, newPassword: "" });
      setSuccess("Profile updated.");
    } catch (e) { setError(errMsg(e)); } finally { setBusy(false); }
  };

  return (
    <>
      <div className="page-head"><h1>Profile</h1><p className="muted">Member since {fmtDate(user.createdAt)}</p></div>
      <form className="card" style={{ maxWidth: 560 }} onSubmit={submit} noValidate>
        <Alert type="error">{error}</Alert>
        <Alert type="success">{success}</Alert>
        <Field label="Email"><input value={user.email} disabled /></Field>
        <Field label="Role"><input value={user.role} disabled /></Field>
        <Field label="Name" error={errors.name}><input name="name" value={form.name} onChange={change} /></Field>
        <Field label="Phone" error={errors.phone}><input name="phone" value={form.phone} onChange={change} /></Field>
        <Field label="New password" error={errors.newPassword} hint="Leave empty to keep your current password"><input name="newPassword" type="password" value={form.newPassword} onChange={change} /></Field>
        <button className="btn" disabled={busy}>{busy ? "Saving..." : "Save changes"}</button>
      </form>
    </>
  );
}
