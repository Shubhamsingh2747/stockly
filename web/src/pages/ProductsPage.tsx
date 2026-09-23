import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, ApiError, getUser } from "../api";

type Category = { id: number; name: string };
type Product = {
  id: number;
  sku: string;
  name: string;
  categoryId: number;
  categoryName: string;
  unitPrice: number;
  stockQuantity: number;
  reorderLevel: number;
  lowStock: boolean;
};

export default function ProductsPage() {
  const admin = getUser()?.role === "ADMIN";
  const [categories, setCategories] = useState<Category[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [sku, setSku] = useState("");
  const [category, setCategory] = useState("");
  const [lowStock, setLowStock] = useState(false);
  const [error, setError] = useState("");
  const [newCategory, setNewCategory] = useState("");
  const [form, setForm] = useState({
    sku: "",
    name: "",
    categoryId: "",
    unitPrice: "10",
    stockQuantity: "0",
    reorderLevel: "5",
  });
  const [editingId, setEditingId] = useState<number | null>(null);

  async function load() {
    setError("");
    try {
      const query = new URLSearchParams();
      if (sku) query.set("sku", sku);
      if (category) query.set("category", category);
      if (lowStock) query.set("lowStock", "true");
      const qs = query.toString();
      const [cats, items] = await Promise.all([
        api<Category[]>("/api/v1/categories"),
        api<Product[]>(`/api/v1/products${qs ? `?${qs}` : ""}`),
      ]);
      setCategories(cats);
      setProducts(items);
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function saveCategory(event: FormEvent) {
    event.preventDefault();
    try {
      await api("/api/v1/categories", { method: "POST", body: JSON.stringify({ name: newCategory }) });
      setNewCategory("");
      await load();
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  async function saveProduct(event: FormEvent) {
    event.preventDefault();
    const payload = {
      sku: form.sku,
      name: form.name,
      categoryId: Number(form.categoryId),
      unitPrice: Number(form.unitPrice),
      stockQuantity: Number(form.stockQuantity),
      reorderLevel: Number(form.reorderLevel),
    };
    try {
      if (editingId) {
        await api(`/api/v1/products/${editingId}`, { method: "PUT", body: JSON.stringify(payload) });
      } else {
        await api("/api/v1/products", { method: "POST", body: JSON.stringify(payload) });
      }
      setEditingId(null);
      setForm({ sku: "", name: "", categoryId: "", unitPrice: "10", stockQuantity: "0", reorderLevel: "5" });
      await load();
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  return (
    <>
      <h1>Products</h1>
      {error ? <p className="error">{error}</p> : null}
      <div className="card">
        <div className="row">
          <div>
            <label>SKU contains</label>
            <input value={sku} onChange={(e) => setSku(e.target.value)} />
          </div>
          <div>
            <label>Category</label>
            <select value={category} onChange={(e) => setCategory(e.target.value)}>
              <option value="">All</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
        </div>
        <label>
          <input type="checkbox" checked={lowStock} onChange={(e) => setLowStock(e.target.checked)} /> Low stock only
        </label>
        <button type="button" onClick={() => void load()}>
          Filter
        </button>
      </div>

      {admin ? (
        <>
          <form className="card" onSubmit={saveCategory}>
            <h2>New category</h2>
            <input value={newCategory} onChange={(e) => setNewCategory(e.target.value)} required />
            <button type="submit">Add category</button>
          </form>
          <form className="card" onSubmit={saveProduct}>
            <h2>{editingId ? "Edit product" : "New product"}</h2>
            <div className="row">
              <div>
                <label>SKU</label>
                <input value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} required />
              </div>
              <div>
                <label>Name</label>
                <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
              </div>
              <div>
                <label>Category</label>
                <select
                  value={form.categoryId}
                  onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                  required
                >
                  <option value="">Select</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label>Price</label>
                <input
                  type="number"
                  step="0.01"
                  value={form.unitPrice}
                  onChange={(e) => setForm({ ...form, unitPrice: e.target.value })}
                />
              </div>
              <div>
                <label>Stock</label>
                <input
                  type="number"
                  value={form.stockQuantity}
                  onChange={(e) => setForm({ ...form, stockQuantity: e.target.value })}
                />
              </div>
              <div>
                <label>Reorder level</label>
                <input
                  type="number"
                  value={form.reorderLevel}
                  onChange={(e) => setForm({ ...form, reorderLevel: e.target.value })}
                />
              </div>
            </div>
            <button type="submit">{editingId ? "Save" : "Create"}</button>
          </form>
        </>
      ) : (
        <p className="muted">Product create/edit is admin-only.</p>
      )}

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>SKU</th>
              <th>Name</th>
              <th>Category</th>
              <th>Qty</th>
              <th>Price</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id}>
                <td>{p.sku}</td>
                <td>
                  {p.name} {p.lowStock ? <span className="badge">low</span> : null}
                </td>
                <td>{p.categoryName}</td>
                <td>{p.stockQuantity}</td>
                <td>{p.unitPrice}</td>
                <td>
                  <Link to={`/products/${p.id}/movements`}>History</Link>
                  {admin ? (
                    <>
                      {" "}
                      <button
                        className="secondary"
                        type="button"
                        onClick={() => {
                          setEditingId(p.id);
                          setForm({
                            sku: p.sku,
                            name: p.name,
                            categoryId: String(p.categoryId),
                            unitPrice: String(p.unitPrice),
                            stockQuantity: String(p.stockQuantity),
                            reorderLevel: String(p.reorderLevel),
                          });
                        }}
                      >
                        Edit
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
