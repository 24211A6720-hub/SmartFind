import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { errMsg } from "../../api/axiosClient";
import { Alert, Field } from "../../components/UI";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    const errs = {};
    if (!/^\S+@\S+\.\S+$/.test(form.email)) errs.email = "Enter a valid email";
    if (!form.password) errs.password = "Password is required";
    setErrors(errs);
    if (Object.keys(errs).length) return;

    setBusy(true); setError("");
    try {
      const user = await login(form.email, form.password);
      navigate(user.role === "ADMIN" ? "/admin" : "/dashboard");
    } catch (err) {
      setError(errMsg(err));
    } finally { setBusy(false); }
  };

  return (
    <div className="auth-wrap">
      <div className="card auth-card">
        <Link to="/" className="brand" style={{ color: "var(--ink)" }}><span className="brand-mark" />SmartFind</Link>
        <h2 style={{ marginTop: "1rem" }}>Welcome back</h2>
        <Alert type="error">{error}</Alert>
        <form onSubmit={submit} noValidate>
          <Field label="Email" error={errors.email}><input name="email" type="email" value={form.email} onChange={change} autoComplete="email" /></Field>
          <Field label="Password" error={errors.password}><input name="password" type="password" value={form.password} onChange={change} autoComplete="current-password" /></Field>
          <button className="btn" style={{ width: "100%" }} disabled={busy}>{busy ? "Signing in..." : "Login"}</button>
        </form>
        <p className="small muted" style={{ marginTop: "1rem" }}>New here? <Link to="/register">Create an account</Link></p>
        <div className="demo-box">
          <strong>Development accounts</strong> (change before real use)<br />
          Admin: admin@smartfind.com / Admin@123<br />
          User: alice@smartfind.com / User@1234
        </div>
      </div>
    </div>
  );
}
