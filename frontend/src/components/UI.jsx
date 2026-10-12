export function Loader() {
  return <div className="loader"><div className="spinner" role="status" aria-label="Loading" /></div>;
}

export function Alert({ type = "info", children, onClose }) {
  if (!children) return null;
  return (
    <div className={`alert alert-${type}`} role={type === "error" ? "alert" : "status"}>
      <div className="row-between">
        <span>{children}</span>
        {onClose && <button className="btn btn-sm btn-ghost" onClick={onClose}>Dismiss</button>}
      </div>
    </div>
  );
}

export function StatusBadge({ value }) {
  return <span className={`badge ${value}`}>{value}</span>;
}

export function StatCard({ label, value, tone = "" }) {
  return (
    <div className={`stat ${tone}`}>
      <div className="num">{value ?? "-"}</div>
      <div className="label">{label}</div>
    </div>
  );
}

export function EmptyState({ title, children, action }) {
  return (
    <div className="empty">
      <h3>{title}</h3>
      {children && <p>{children}</p>}
      {action}
    </div>
  );
}

export function Modal({ title, onClose, children, wide = false }) {
  return (
    <div className="overlay" onClick={onClose}>
      <div className={`modal ${wide ? "wide" : ""}`} role="dialog" aria-modal="true" aria-label={title} onClick={(e) => e.stopPropagation()}>
        <div className="row-between mb">
          <h2 style={{ margin: 0 }}>{title}</h2>
          <button className="btn btn-sm btn-ghost" onClick={onClose} aria-label="Close">Close</button>
        </div>
        {children}
      </div>
    </div>
  );
}

export function ScoreBar({ value, level }) {
  return <div className={`bar ${level || ""}`}><span style={{ width: `${Math.max(0, Math.min(100, value))}%` }} /></div>;
}

/** Label + input + validation message in one block. */
export function Field({ label, error, hint, children }) {
  return (
    <div className={`field ${error ? "has-error" : ""}`}>
      <label>{label}</label>
      {children}
      {hint && !error && <span className="hint">{hint}</span>}
      {error && <span className="error-text">{error}</span>}
    </div>
  );
}

export const fmtDate = (d) => (d ? new Date(d).toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" }) : "-");
export const fmtDateTime = (d) => (d ? new Date(d).toLocaleString(undefined, { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" }) : "-");

export const CATEGORIES = ["Wallet", "Phone", "Keys", "Bag", "Laptop", "ID Card", "Documents", "Jewellery", "Clothing", "Electronics", "Umbrella", "Bottle", "Glasses", "Other"];
