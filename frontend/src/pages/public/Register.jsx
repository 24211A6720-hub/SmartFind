import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { errMsg } from "../../api/axiosClient";
import { Alert, Field } from "../../components/UI";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", phone: "", password: "", confirm: "" });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = "Name is required";
    if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = "Enter a valid email";
    if (!/^[0-9+\-\s]{7,15}$/.test(form.phone)) e.phone = "Enter a valid phone number";
    if (!/^(?=.*[A-Za-z])(?=.*\d).{8,}$/.test(form.password)) e.password = "At least 8 characters with a letter and a number";
    if (form.password !== form.confirm) e.confirm = "Passwords do not match";
    return e;
  };

  const submit = async (ev) => {
    ev.preventDefault();
    const errs = validate();
    setErrors(errs);
    if (Object.keys(errs).length) return;
    setBusy(true); setError("");
    try {
      await register({ name: form.name, email: form.email, phone: form.phone, password: form.password });
      navigate("/dashboard");
    } catch (err) {
      setError(errMsg(err));
    } finally { setBusy(false); }
  };

  return (
    <div className="auth-wrap">
      <div className="card auth-card">
        <Link to="/" className="brand" style={{ color: "var(--ink)" }}><span className="brand-mark" />SmartFind</Link>
        <h2 style={{ marginTop: "1rem" }}>Create your account</h2>
        <Alert type="error">{error}</Alert>
        <form onSubmit={submit} noValidate>
          <Field label="Full name" error={errors.name}><input name="name" value={form.name} onChange={change} /></Field>
          <Field label="Email" error={errors.email}><input name="email" type="email" value={form.email} onChange={change} /></Field>
          <Field label="Phone" error={errors.phone}><input name="phone" value={form.phone} onChange={change} /></Field>
          <Field label="Password" error={errors.password} hint="At least 8 characters, with a letter and a number"><input name="password" type="password" value={form.password} onChange={change} /></Field>
          <Field label="Confirm password" error={errors.confirm}><input name="confirm" type="password" value={form.confirm} onChange={change} /></Field>
          <button className="btn" style={{ width: "100%" }} disabled={busy}>{busy ? "Creating..." : "Register"}</button>
        </form>
        <p className="small muted" style={{ marginTop: "1rem" }}>Already registered? <Link to="/login">Login</Link></p>
      </div>
    </div>
  );
}
