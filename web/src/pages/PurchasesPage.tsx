import { FormEvent, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { api, ApiError } from "../api";

type Product = { id: number; sku: string; name: string };
type Line = { productId: number; sku: string; quantity: number; unitCost: number };
type Order = { id: number; status: string; createdByEmail: string; createdAt: string; lines: Line[] };

export default function PurchasesPage() {
  const [searchParams] = useSearchParams();
  const [orders, setOrders] = useState<Order[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [productId, setProductId] = useState(searchParams.get("productId") ?? "");
  const [quantity, setQuantity] = useState("1");
  const [unitCost, setUnitCost] = useState("5");
  const [error, setError] = useState("");

  async function load() {
    setError("");
    try {
      const [list, catalog] = await Promise.all([
        api<Order[]>("/api/v1/purchase-orders"),
        api<Product[]>("/api/v1/products"),
      ]);
      setOrders(list);
      setProducts(catalog);
      const requested = searchParams.get("productId");
      if (requested && catalog.some((p) => String(p.id) === requested)) {
        setProductId(requested);
      } else if (!productId && catalog[0]) {
        setProductId(String(catalog[0].id));
      }
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function create(event: FormEvent) {
    event.preventDefault();
    try {
      await api("/api/v1/purchase-orders", {
        method: "POST",
        body: JSON.stringify({
          lines: [{ productId: Number(productId), quantity: Number(quantity), unitCost: Number(unitCost) }],
        }),
      });
      await load();
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  async function act(id: number, action: "receive" | "cancel") {
    try {
      await api(`/api/v1/purchase-orders/${id}/${action}`, { method: "POST" });
      await load();
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  return (
    <>
      <h1>Purchase orders</h1>
      {error ? <p className="error">{error}</p> : null}
      <form className="card" onSubmit={create}>
        <h2>New draft</h2>
        <div className="row">
          <div>
            <label>Product</label>
            <select value={productId} onChange={(e) => setProductId(e.target.value)} required>
              {products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.sku}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label>Quantity</label>
            <input type="number" min={1} value={quantity} onChange={(e) => setQuantity(e.target.value)} />
          </div>
          <div>
            <label>Unit cost</label>
            <input type="number" step="0.01" value={unitCost} onChange={(e) => setUnitCost(e.target.value)} />
          </div>
        </div>
        <button type="submit">Create draft</button>
      </form>
      <div className="card">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Status</th>
              <th>Lines</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o) => (
              <tr key={o.id}>
                <td>{o.id}</td>
                <td>{o.status}</td>
                <td>{o.lines.map((l) => `${l.sku} x${l.quantity}`).join(", ")}</td>
                <td>
                  {o.status === "DRAFT" ? (
                    <>
                      <button type="button" onClick={() => void act(o.id, "receive")}>
                        Receive
                      </button>
                      <button className="danger" type="button" onClick={() => void act(o.id, "cancel")}>
                        Cancel
                      </button>
                    </>
                  ) : null}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
