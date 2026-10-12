import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { itemApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, Field, Loader, CATEGORIES } from "../../components/UI";
import { useAuth } from "../../context/AuthContext";

const today = () => new Date().toISOString().slice(0, 10);

/** Create form (kind from props) and edit form (kind + id from the URL). */
export default function ReportForm({ kind: kindProp }) {
  const params = useParams();
  const kind = kindProp || params.kind;
  const editId = params.id;
  const isLost = kind === "lost";
  const { user } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    itemName: "", category: "Wallet", color: "", location: "", date: today(), description: "",
    identifyingDetails: "", imageUrl: "", contactInfo: user.phone ? `${user.email} / ${user.phone}` : user.email,
  });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(!!editId);

  useEffect(() => {
    if (!editId) return;
    itemApi.get(kind, editId).then((i) => setForm({
      itemName: i.itemName, category: i.category, color: i.color, location: i.location, date: i.date,
      description: i.description, identifyingDetails: i.identifyingDetails || "", imageUrl: i.imageUrl || "", contactInfo: i.contactInfo || "",
    })).catch((e) => setError(errMsg(e))).finally(() => setLoading(false));
  }, [editId, kind]);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const validate = () => {
    const e = {};
    if (!form.itemName.trim()) e.itemName = "Item name is required";
    if (!form.color.trim()) e.color = "Color is required";
    if (!form.location.trim()) e.location = "Location is required";
    if (!form.date) e.date = "Date is required";
    else if (form.date > today()) e.date = "Date cannot be in the future";
    if (form.description.trim().length < 5) e.description = "Please describe the item (at least 5 characters)";
    if (!form.contactInfo.trim()) e.contactInfo = "Contact information is required";
    return e;
  };

  const submit = async (ev) => {
    ev.preventDefault();
    const errs = validate();
    setErrors(errs);
    if (Object.keys(errs).length) return;
    setBusy(true); setError("");
    try {
      if (editId) await itemApi.update(kind, editId, form);
      else await itemApi.create(kind, form);
      navigate("/my-reports", { state: { flash: editId ? "Report updated." : "Report submitted. SmartFind has checked it against existing reports - see Possible Matches." } });
    } catch (err) {
      setError(errMsg(err));
    } finally { setBusy(false); }
  };

  if (loading) return <Loader />;

  return (
    <>
      <div className="page-head">
        <h1>{editId ? "Edit" : "Report"} {isLost ? "lost" : "found"} item</h1>
        <p className="muted">{isLost ? "Tell us what you lost. The more detail, the better the matching." : "Tell us what you found. Do not include secret details publicly - use the identifying details box."}</p>
      </div>
      <form className="card" onSubmit={submit} noValidate>
        <Alert type="error">{error}</Alert>
        <div className="form-grid">
          <Field label="Item name" error={errors.itemName}><input name="itemName" value={form.itemName} onChange={change} placeholder="e.g. Black Leather Wallet" /></Field>
          <Field label="Category">
            <select name="category" value={form.category} onChange={change}>{CATEGORIES.map((c) => <option key={c}>{c}</option>)}</select>
          </Field>
          <Field label="Color" error={errors.color}><input name="color" value={form.color} onChange={change} placeholder="e.g. Black" /></Field>
          <Field label={isLost ? "Location lost" : "Location found"} error={errors.location}><input name="location" value={form.location} onChange={change} placeholder="e.g. Anurag University Library" /></Field>
          <Field label={isLost ? "Date lost" : "Date found"} error={errors.date}><input name="date" type="date" max={today()} value={form.date} onChange={change} /></Field>
          <Field label="Contact information" error={errors.contactInfo} hint="Only shown to you, admins, and the matched person after approval"><input name="contactInfo" value={form.contactInfo} onChange={change} /></Field>
        </div>
        <Field label="Description" error={errors.description}><textarea name="description" value={form.description} onChange={change} placeholder="e.g. Black leather wallet with a student ID card inside" /></Field>
        <Field label="Additional identifying details" hint="Private. Used by the admin to verify claims (scratches, stickers, serial number...)"><textarea name="identifyingDetails" value={form.identifyingDetails} onChange={change} /></Field>
        <Field label="Image URL (optional)" hint="Paste a link to a photo"><input name="imageUrl" value={form.imageUrl} onChange={change} placeholder="https://..." /></Field>
        <div className="row">
          <button className="btn" disabled={busy}>{busy ? "Saving..." : editId ? "Save changes" : "Submit report"}</button>
          <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>Cancel</button>
        </div>
      </form>
    </>
  );
}
