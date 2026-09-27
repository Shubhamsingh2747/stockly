import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, ApiError } from "../api";
import { useAuth } from "../auth";

type LowStockProduct = {
  id: number;
  sku: string;
  name: string;
  categoryName: string;
  unitPrice: number;
  stockQuantity: number;
  reorderLevel: number;
};

type RecentMovement = {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  movementType: string;
  quantityDelta: number;
  quantityAfter: number;
  reference: string;
  createdAt: string;
};

type Snapshot = {
  lowStockCount: number;
  openSalesDrafts: number;
  openPurchaseDrafts: number;
  lowStockProducts: LowStockProduct[];
  recentMovements: RecentMovement[];
};

export default function OperationsPage() {
  const { user } = useAuth();
  const admin = user?.role === "ADMIN";
  const canTrade = user?.role === "ADMIN" || user?.role === "USER";
  const [data, setData] = useState<Snapshot | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setError("");
      try {
        setData(await api<Snapshot>("/api/v1/operations"));
      } catch (err) {
        setError((err as ApiError).message);
      }
    }
    void load();
  }, []);

  return (
    <>
      <h1>Operations</h1>
      <p className="muted">
        Low stock, open drafts, and the latest stock movements. Viewer accounts can watch this
        board; users and administrators can start orders from it.
      </p>
      {error ? <p className="error">{error}</p> : null}

      <div className="stat-grid">
        <Link className="stat-card" to="/?lowStock=1">
          <span className="stat-label">Low stock</span>
          <span className="stat-value">{data?.lowStockCount ?? "—"}</span>
        </Link>
        <Link className="stat-card" to="/sales">
          <span className="stat-label">Open sales drafts</span>
          <span className="stat-value">{data?.openSalesDrafts ?? "—"}</span>
        </Link>
        <Link className="stat-card" to="/purchases">
          <span className="stat-label">Open purchase drafts</span>
          <span className="stat-value">{data?.openPurchaseDrafts ?? "—"}</span>
        </Link>
      </div>

      <div className="card">
        <h2>Needs reorder</h2>
        {!data || data.lowStockProducts.length === 0 ? (
          <p className="muted">No products are at or below reorder level.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>SKU</th>
                <th>Name</th>
                <th>Category</th>
                <th>Qty</th>
                <th>Reorder</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {data.lowStockProducts.map((p) => (
                <tr key={p.id}>
                  <td>{p.sku}</td>
                  <td>{p.name}</td>
                  <td>{p.categoryName}</td>
                  <td>{p.stockQuantity}</td>
                  <td>{p.reorderLevel}</td>
                  <td className="col-actions">
                    <div className="actions">
                      {canTrade ? (
                        <>
                          <Link className="btn" to={`/purchases?productId=${p.id}`}>
                            Receive stock
                          </Link>
                          {p.stockQuantity > 0 ? (
                            <Link className="btn secondary" to={`/sales?productId=${p.id}`}>
                              Sell
                            </Link>
                          ) : null}
                        </>
                      ) : null}
                      {admin ? (
                        <Link className="btn-edit" to={`/?edit=${p.id}`}>
                          Edit product
                        </Link>
                      ) : null}
                      <Link className="history-link" to={`/products/${p.id}/movements`}>
                        View history
                      </Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="card">
        <h2>Recent movements</h2>
        {!data || data.recentMovements.length === 0 ? (
          <p className="muted">No stock movements yet.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>When</th>
                <th>SKU</th>
                <th>Type</th>
                <th>Delta</th>
                <th>After</th>
                <th>Ref</th>
              </tr>
            </thead>
            <tbody>
              {data.recentMovements.map((m) => (
                <tr key={m.id}>
                  <td>{new Date(m.createdAt).toLocaleString()}</td>
                  <td>
                    <Link to={`/products/${m.productId}/movements`}>
                      {m.sku} {m.productName}
                    </Link>
                  </td>
                  <td>{m.movementType}</td>
                  <td>{m.quantityDelta}</td>
                  <td>{m.quantityAfter}</td>
                  <td>{m.reference}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}
